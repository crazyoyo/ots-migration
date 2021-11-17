package com.amazonaws.otsmgr;

import com.alicloud.openservices.tablestore.TunnelClient;
import com.alicloud.openservices.tablestore.model.PrimaryKeyColumn;
import com.alicloud.openservices.tablestore.model.StreamRecord;
import com.alicloud.openservices.tablestore.model.tunnel.*;
import com.alicloud.openservices.tablestore.tunnel.worker.IChannelProcessor;
import com.alicloud.openservices.tablestore.tunnel.worker.ProcessRecordsInput;
import com.alicloud.openservices.tablestore.tunnel.worker.TunnelWorker;
import com.alicloud.openservices.tablestore.tunnel.worker.TunnelWorkerConfig;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.StringUtils;
import de.siegmar.fastcsv.writer.CsvWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
//@EnableAsync
public class OtsMgrApplication implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OtsMgrApplication.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final List<String> headerList = new ArrayList<>();

    private static MigrationConfigs config;
    private static TunnelClient tunnelClient;
    private static AmazonS3 s3;

    public static void main(String[] args) {
        log.info("OTS migration starts.");
        new SpringApplicationBuilder()
                .sources(OtsMgrApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(ApplicationArguments args) {
        ApplicationContext applicationContext = SpringUtil.getApplicationContext();
        config = applicationContext.getBean(MigrationConfigs.class);
        tunnelClient = new TunnelClient(config.getEndPoint(), config.getAccessKeyId(), config.getAccessKeySecret(), config.getInstanceName());
        s3 = S3Util.getS3Client(config.getAkAWS(), config.getSkAWS());
        dealwithheader();
        work(getTunel());
    }

    private String getTunel() {
        String tunnelId = null;
	    try {
            DescribeTunnelRequest drequest = new DescribeTunnelRequest(config.getTableName(), config.getTunnelName());
            DescribeTunnelResponse dresp = tunnelClient.describeTunnel(drequest);
            tunnelId = dresp.getTunnelInfo().getTunnelId();
        } catch (BeanCreationException be) {
            CreateTunnelRequest crequest = new CreateTunnelRequest(config.getTableName(), config.getTunnelName(), TunnelType.Stream);
            CreateTunnelResponse cresp = tunnelClient.createTunnel(crequest);
            //tunnelId用于后续TunnelWorker的初始化，该值也可以通过ListTunnel或者DescribeTunnel获取。
            tunnelId = cresp.getTunnelId();
        }
        log.info("Tunnel Created, Id: " + tunnelId);
        return tunnelId;
    }

    private void work(String tunnelId) {
        //TunnelWorkerConfig默认会启动读数据和处理数据的线程池。
        //如果使用的是单台机器，当需要启动多个TunnelWorker时，建议共用一个TunnelWorkerConfig。TunnelWorkerConfig中包括更多的高级参数。
        TunnelWorkerConfig workerConfig = new TunnelWorkerConfig(new OTSProcessor(config));
        //配置TunnelWorker，并启动自动化的数据处理任务。
        TunnelWorker worker = new TunnelWorker(tunnelId, tunnelClient, workerConfig);
        try {
            worker.connectAndWorking();
        } catch (Exception e) {
            log.error("start OTS tunnel failed.", e);
            worker.shutdown();
            tunnelClient.shutdown();
        }
    }

    private void dealwithheader() {
        for(String i : config.getHeaders().split(",")) {
            headerList.add(i);
        }
    }

    //用户自定义数据消费Callback，即实现IChannelProcessor接口（process和shutdown）。
    static class OTSProcessor implements IChannelProcessor {
        private MigrationConfigs config;

        public OTSProcessor(MigrationConfigs config) {
            this.config = config;
        }

        @Override
        public void process(ProcessRecordsInput input) {
            StringWriter sw = new StringWriter();
            CsvWriter writer = CsvWriter.builder().build(sw);
            //ProcessRecordsInput中包含有拉取到的数据。
            log.info("Default record processor, would print records count");
			//NextToken用于Tunnel Client的翻页。
            log.info(String.format("Process %d records, NextToken: %s", input.getRecords().size(), input.getNextToken()));

            try {
				for(StreamRecord r : input.getRecords()) {
                    log.info("Current record is: " + r.toString());
					ArrayList<String> row = new ArrayList();

					// deal with other key and column headers
					Map<String, String> columns = new HashMap();
                    for(PrimaryKeyColumn k : r.getPrimaryKey().getPrimaryKeyColumns()) {
                        columns.put(k.getName(), k.getValue().toString());
                    }
                    r.getColumns().forEach((c) -> columns.put(c.getColumn().getName(), c.getColumn().getValue().toString()));

                    // deal with other key and column values
                    for(String h : headerList) {
                        if(columns.get(h) == null) row.add("");
                        else row.add(columns.get(h));
                    }
                    writer.writeRow(row);
				}
                log.info("csv file is : " + sw.toString());
                uploadtoS3(sw, config);
            } catch (Exception e) {
                log.error("convert csv file failed.", e);
            }
        }

        @Override
        public void shutdown() {
            log.info("Mock shutdown");
        }

        public void uploadtoS3(StringWriter sw, MigrationConfigs config) {
            if(!sw.getBuffer().isEmpty()) {
                String keySuffix = config.getTableName() + sdf.format(new Date());
                s3.putObject(config.getBucketName(), config.getCdcPrefix() + keySuffix, sw.toString());
            }
        }
    }
}