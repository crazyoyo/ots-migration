package com.amazonaws.otsmgr.utils;

import com.amazonaws.otsmgr.beans.MigrationColumn;
import com.amazonaws.otsmgr.beans.MigrationKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.utils.StringUtils;

import java.util.HashMap;
import java.util.List;

public class DynamoDBUtil {

    private static final Logger log = LoggerFactory.getLogger(DynamoDBUtil.class);

    private static DynamoDbClient ddb;
    private static String tableName;
    private static String partitionKey;
    private static String SORT_KEY_SEPERATOR = "_";

    public static void initClient(String _tableName) {

        tableName = _tableName;

        ddb = DynamoDbClient.builder().build();

        DescribeTableRequest desc_request = DescribeTableRequest.builder()
                .tableName(tableName)
                .build();

        TableDescription tableInfo = ddb.describeTable(desc_request).table();

        if(tableInfo.keySchema() != null) {
            for (KeySchemaElement element : tableInfo.keySchema()) {
                if(KeyType.HASH == (element.keyType())) {
                    partitionKey = element.attributeName();
                }
            }
        }
    }

    public static void deleteTableItem(List<MigrationKey> keys) {
        HashMap<String, AttributeValue> keyToDelete = buildKeyItems(keys);

        DeleteItemRequest deleteReq = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(keyToDelete)
                .build();

        try {
            ddb.deleteItem(deleteReq);
        } catch (DynamoDbException e) {
            log.error("Error delete to DynamoDB: " + e.getMessage());
        }
    }

    public static void insertTableItem(List<MigrationKey> keys,
                                       List<MigrationColumn> columns) {
        HashMap<String, AttributeValue> itemValues = buildKeyItems(keys);

        for (MigrationColumn c : columns) {
            switch (c.getType()) {
                case STRING:
                    itemValues.put(c.getName(), AttributeValue.builder().s(c.getValue()).build());
                    break;

                case INTEGER:
                    itemValues.put(c.getName(), AttributeValue.builder().n(c.getValue()).build());
                    break;
            }
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        try {
            ddb.putItem(request);
            log.info(tableName +" was successfully inserted");

        } catch (ResourceNotFoundException e) {
            log.error("Error insert to DynamoDB: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
        } catch (DynamoDbException e) {
            log.error("Error insert to DynamoDB: " + e.getMessage());
        }
    }

    public static void updateTableItem(List<MigrationKey> keys,
                                       List<MigrationColumn> columns){

        HashMap<String,AttributeValue> itemKey = buildKeyItems(keys);

        HashMap<String,AttributeValueUpdate> values = new HashMap<>();
        // Update the column specified by name with updatedValue
        for (MigrationColumn c : columns) {
            switch (c.getType()) {
                case STRING:
                    values.put(c.getName(), AttributeValueUpdate.builder()
                            .value(AttributeValue.builder().s(c.getValue()).build())
                            .action(AttributeAction.PUT)
                            .build());
                    break;

                case INTEGER:
                    values.put(c.getName(), AttributeValueUpdate.builder()
                            .value(AttributeValue.builder().n(c.getValue()).build())
                            .action(AttributeAction.PUT)
                            .build());
                    break;
            }
        }

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(itemKey)
                .attributeUpdates(values)
                .build();

        try {
            ddb.updateItem(request);
        } catch (ResourceNotFoundException e) {
            log.error("Error update to DynamoDB: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
        } catch (DynamoDbException e) {
            log.error("Error update to DynamoDB: " + e.getMessage());
        }
    }

    private static HashMap<String,AttributeValue> buildKeyItems(List<MigrationKey> keys) {
        HashMap<String, AttributeValue> itemKey = new HashMap<>();
        String sortKey = "";
        String sortValue = "";
        for (MigrationKey k : keys) {
            if(partitionKey.equalsIgnoreCase(k.getName())) {
                switch (k.getType()) {
                    case STRING:
                        itemKey.put(k.getName(), AttributeValue.builder().s(k.getValue()).build());
                        break;

                    case INTEGER:
                        itemKey.put(k.getName(), AttributeValue.builder().n(k.getValue()).build());
                        break;
                }
            }
            else {
                sortKey = sortKey + SORT_KEY_SEPERATOR + k.getName();
                sortValue = sortValue + SORT_KEY_SEPERATOR + k.getValue();
            }
        }
        if(StringUtils.isNotBlank(sortKey)) {
            itemKey.put(StringUtils.replacePrefixIgnoreCase(sortKey, SORT_KEY_SEPERATOR, ""),
                    AttributeValue.builder().s(StringUtils.replacePrefixIgnoreCase(sortValue, SORT_KEY_SEPERATOR, "")).build());
        }
        return itemKey;
    }
}
