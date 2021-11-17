package com.amazonaws.otsmgr;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ots.migration.config")
public class MigrationConfigs {

    private String headers;
    private String endPoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String instanceName;
    private String tableName;
    private String tunnelName;
    private String bucketName;
    private String cdcPrefix;
    private String akAWS;
    private String skAWS;

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTunnelName() {
        return tunnelName;
    }

    public void setTunnelName(String tunnelName) {
        this.tunnelName = tunnelName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getCdcPrefix() {
        return cdcPrefix;
    }

    public void setCdcPrefix(String cdcPrefix) {
        this.cdcPrefix = cdcPrefix;
    }

    public String getAkAWS() {
        return akAWS;
    }

    public void setAkAWS(String akAWS) {
        this.akAWS = akAWS;
    }

    public String getSkAWS() {
        return skAWS;
    }

    public void setSkAWS(String skAWS) {
        this.skAWS = skAWS;
    }
}
