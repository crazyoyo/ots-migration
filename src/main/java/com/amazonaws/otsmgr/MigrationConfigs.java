package com.amazonaws.otsmgr;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ots.migration.config")
public class MigrationConfigs {

    private String sourceEndPoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String instanceName;
    private String tableName;
    private String tunnelName;
    private String targetEndpoint;


    public void setSourceEndPoint(String sourceEndPoint) {
        this.sourceEndPoint = sourceEndPoint;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTunnelName(String tunnelName) {
        this.tunnelName = tunnelName;
    }

    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }


    public String getSourceEndPoint() {
        return sourceEndPoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTunnelName() {
        return tunnelName;
    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

}
