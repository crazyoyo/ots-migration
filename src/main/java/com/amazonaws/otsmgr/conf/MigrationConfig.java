package com.amazonaws.otsmgr.conf;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "ots.migration.config")
public class MigrationConfig {

    private String sourceEndPoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String instanceName;
    private List<String> tableNames;
    private String ddbEndpoint;
    private String targetRegion;
    private String s3BuckeName;
    private String migrationTarget;
    private String targetDynamodbType;
    private String migrationType;
    private boolean restart;

    private String tablePKs;
    private String tableColumns;
    private String[] _tablePKs;
    private String[] _tableColumns;
    private String[] _allTableColumns;

    @PostConstruct
    private void init() {
        _tableColumns = Arrays.stream(tableColumns.split(","))
                .toArray(String[]::new);

        _tablePKs = Arrays.stream(tablePKs.split(","))
                .toArray(String[]::new);

        _allTableColumns = ArrayUtils.addAll(_tablePKs, _tableColumns);
    }

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

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public void setDdbEndpoint(String ddbEndpoint) {
        this.ddbEndpoint = ddbEndpoint;
    }
    public void setTargetRegion(String targetRegion) {
        this.targetRegion = targetRegion;
    }
    public void setS3BuckeName(String s3BuckeName) {
        this.s3BuckeName = s3BuckeName;
    }

    public void setMigrationTarget(String migrationTarget) {
        this.migrationTarget = migrationTarget;
    }

    public void setTargetDynamodbType(String targetDynamodbType) {
        this.targetDynamodbType = targetDynamodbType;
    }

    public void setMigrationType(String migrationType) {
        this.migrationType = migrationType;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    public void setTablePKs(String tablePKs) {
        this.tablePKs = tablePKs;
    }

    public void setTableColumns(String tableColumns) {
        this.tableColumns = tableColumns;
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

    public List<String> getTableNames() {
        return tableNames;
    }

    public String getDdbEndpoint() {
        return ddbEndpoint;
    }

    public String getTargetRegion() {
        return targetRegion;
    }

    public String getS3BuckeName() {
        return s3BuckeName;
    }

    public String[] getTablePKs() {
        return _tablePKs;
    }

    public String[] getTableColumns() {
        return _tableColumns;
    }

    public String[] getAllTableColumns() {
        return _allTableColumns;
    }

    public String getMigrationTarget() {
        return migrationTarget;
    }

    public String getTargetDynamodbType() {
        return targetDynamodbType;
    }

    public String getMigrationType() {
        return migrationType;
    }

    public boolean isRestart() {
        return restart;
    }
}
