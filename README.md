# OTS-Migration
SpringBoot application for Aliyun TableStore migrate to AWS DynamoDB, Support both full data transfer and incremental data transfer.

# Configuration
edit file /src/main/resources/application.properties with Aliyun Table configure and AWS Dynamo configure, eg:
`
#TableStore configs
ots.migration.config.sourceEndPoint=https://xxxxxx.cn-hangzhou.ots.aliyuncs.com
ots.migration.config.accessKeyId=xxxxxxxxxxxxxxxx
ots.migration.config.accessKeySecret=xxxxxxxxxxxxxxxxxxxxxx
ots.migration.config.instanceName=xxxxx
ots.migration.config.tableName=xxxxx
ots.migration.config.tunnelName=xxxx

#DynamoDB configs
ots.migration.config.targetEndpoint=dynamodb.cn-northwest-1.amazonaws.com.cn
`

# Compile and Deploy
`mvn clean install`

# Run
`java -jar target/ots-mgr-0.0.1-SNAPSHOT.jar`
