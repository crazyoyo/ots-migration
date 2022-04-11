package com.amazonaws.otsmgr;

import com.alicloud.openservices.tablestore.TunnelClient;
import com.alicloud.openservices.tablestore.model.PrimaryKeyColumn;
import com.alicloud.openservices.tablestore.model.RecordColumn;
import com.alicloud.openservices.tablestore.model.StreamRecord;
import com.alicloud.openservices.tablestore.model.tunnel.*;
import com.alicloud.openservices.tablestore.tunnel.worker.IChannelProcessor;
import com.alicloud.openservices.tablestore.tunnel.worker.ProcessRecordsInput;
import com.alicloud.openservices.tablestore.tunnel.worker.TunnelWorker;
import com.alicloud.openservices.tablestore.tunnel.worker.TunnelWorkerConfig;
import com.amazonaws.otsmgr.beans.MigrationColumn;
import com.amazonaws.otsmgr.beans.MigrationKey;
import com.amazonaws.otsmgr.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import java.util.*;

@SpringBootApplication
//@EnableAsync
public class OtsMgrApplication implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OtsMgrApplication.class);
    private static TunnelClient tunnelClient;

    @Autowired
    private MigrationConfigs config;

    public static void main(String[] args) {
        log.info("OTS migration starts.");
        new SpringApplicationBuilder()
                .sources(OtsMgrApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(ApplicationArguments args) {

//        if(args.getOptionValues("config") == null) {
//            throw new RuntimeException("Please input configuration file path, such as --config=./migration.config");
//        }

        tunnelClient = new TunnelClient(config.getSourceEndPoint(), config.getAccessKeyId(), config.getAccessKeySecret(), config.getInstanceName());
        DynamoDBUtil.initClient(config.getTableName());
        work(getTableStoreTunel());
    }

    private String getTableStoreTunel() {
        String tunnelId = null;
	    try {
            DescribeTunnelRequest drequest = new DescribeTunnelRequest(config.getTableName(), config.getTunnelName());
            DescribeTunnelResponse dresp = tunnelClient.describeTunnel(drequest);
            tunnelId = dresp.getTunnelInfo().getTunnelId();
        } catch (Exception be) {
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

    //用户自定义数据消费Callback，即实现IChannelProcessor接口（process和shutdown）。
    static class OTSProcessor implements IChannelProcessor {
        private MigrationConfigs config;

        public OTSProcessor(MigrationConfigs config) {
            this.config = config;
        }

        @Override
        public void process(ProcessRecordsInput input) {
            //ProcessRecordsInput中包含有拉取到的数据。
            log.info("Default record processor, would print records count");
			//NextToken用于Tunnel Client的翻页。
            log.info(String.format("Process %d records, NextToken: %s", input.getRecords().size(), input.getNextToken()));


            for(StreamRecord r : input.getRecords()) {
                try {
                    operateInDynanoDB(r);
                }
                catch (Exception e) {
                    log.error("send to DynamoDB failed with record is " + r.toString(), e);
                }
            }
        }

        @Override
        public void shutdown() {
            log.info("Mock shutdown");
        }

        private void operateInDynanoDB(StreamRecord r) {
            switch (r.getRecordType()) {
                case PUT:
                case UPDATE:
                    putInDynamoDB(r);
                    break;

                case DELETE:
                    deleteInDynamoDB(r);
                    break;
            }
        }

        private void deleteInDynamoDB(StreamRecord r) {
            List<MigrationKey> keys = new ArrayList<>();
            for(PrimaryKeyColumn k : r.getPrimaryKey().getPrimaryKeyColumns()) {
                MigrationKey c = new MigrationKey();
                c.setName(k.getName());
                c.setType(k.getValue().getType());
                c.setValue(k.getValue().toString());
                keys.add(c);
            }

            DynamoDBUtil.deleteTableItem(keys);
        }

        private void putInDynamoDB(StreamRecord r) {
            List<MigrationKey> keys = new ArrayList<>();
            for(PrimaryKeyColumn k : r.getPrimaryKey().getPrimaryKeyColumns()) {
                MigrationKey c = new MigrationKey();
                c.setName(k.getName());
                c.setType(k.getValue().getType());
                c.setValue(k.getValue().toString());
                keys.add(c);
            }

            List<MigrationColumn> columns = new ArrayList<>();
            for(RecordColumn k : r.getColumns()) {
                MigrationColumn c = new MigrationColumn();
                c.setName(k.getColumn().getName());
                c.setType(k.getColumn().getValue().getType());
                c.setValue(k.getColumn().getValue().toString());
                columns.add(c);
            }

            if(r.getRecordType() == StreamRecord.RecordType.PUT) {
                DynamoDBUtil.insertTableItem(keys, columns);
            }

            else if(r.getRecordType() == StreamRecord.RecordType.UPDATE) {
                DynamoDBUtil.updateTableItem(keys, columns);
            }
        }
    }
}