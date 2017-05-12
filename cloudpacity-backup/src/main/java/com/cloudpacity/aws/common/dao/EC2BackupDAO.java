package com.cloudpacity.aws.common.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.cloudpacity.aws.backup.CPBackupEnv;
import com.cloudpacity.aws.backup.pojo.EC2BackupDO;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
/**
 * 
 * Copyright 2015 Cloudpacity
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Scott Wheeler
 *
 */
public class EC2BackupDAO
{
    public static final String TABLE_NAME = "CloudpacityEC2Backup";
    private AmazonDynamoDB dynamoDBClient;
    
    public static final String PURGE_TIMESTAMP_COL_NAME = "purgeTimestamp";
    public static final String ID_COL_NAME = "Id";
    public static final String ORIG_REQ_ID_COL_NAME = "OrigReqId";
    public static final String CURR_REQ_ID_COL_NAME = "CurrReqId";
    public static final String ITERATION_COL_NAME = "Iteration";
    public static final String REQ_START_TIME_COL_NAME = "RequestStartTime";
    public static final String INSTANCES_TO_START_COL_NAME = "InstancesToStart";
    public static final String STATE_COL_NAME = "State";
    public static final String ACTION_PAUSE_SEC_NAME = "actionPauseSec";
    public static final String MAX_RECURSIVE_CALLS_NAME = "maxRecursiveCalls";
    public static final String MAX_RUN_MIN_NAME = "maxRunMinutes";
    public static final String INSTANCE_DEPENDENCY_TAG_NAME = "instanceDependencyTag";
    public static final String TIMESTAMP_TAG_NAME = "timestampTag";
    public static final String INSTANCE_ID_TAG_NAME = "instanceIdTag";
    public static final String DEVICE_TAG_NAME = "deviceTag";
    public static final String IMAGE_ID_TAG_NAME = "imageIdTag";
    public static final String TAGS_TO_INCLUDE_NAME = "tagsToInclude";
//    public static final String ENABLE_BACKUP_TAG_NAME = "enableBackupTag";
    public static final String BACKUP_STRATEGY_TAG_COL_NAME = "backupStrategyTag";
    public static final String FILTER1_TAG_NAME_COL_NAME = "Filter1TagName";
    public static final String FILTER1_TAG_VALUE_COL_NAME = "Filter1TagValue";
    public static final String FILTER2_TAG_NAME_COL_NAME = "Filter2TagName";
    public static final String FILTER2_TAG_VALUE_COL_NAME = "Filter2TagValue";
    public static final String FILTER3_TAG_NAME_COL_NAME = "Filter3TagName";
    public static final String FILTER3_TAG_VALUE_COL_NAME = "Filter3TagValue";
    public static final String FILTER4_TAG_NAME_COL_NAME = "Filter4TagName";
    public static final String FILTER4_TAG_VALUE_COL_NAME = "Filter4TagValue";
    public static final String FILTER5_TAG_NAME_COL_NAME = "Filter5TagName";
    public static final String FILTER5_TAG_VALUE_COL_NAME = "Filter5TagValue";
    public static final String FILTER6_TAG_NAME_COL_NAME = "Filter6TagName";
    public static final String FILTER6_TAG_VALUE_COL_NAME = "Filter6TagValue";
    
    
    public EC2BackupDAO()
    {
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("key", new AttributeValue("value"));
		
		this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withRegion(CPBackupEnv.getRegion())
                .build();
    }

    public GetItemResult getItemByKey(String keyValue)
    {
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("key", new AttributeValue("value"));

		return dynamoDBClient.getItem(TABLE_NAME, key);
    }

    public PutItemResult putItem(EC2BackupDO ec2backupDO)
    {
        Validate.notEmpty(ec2backupDO.getId(), "The EC2BackupDO Id is empty!", new Object[0]);
        Validate.notEmpty(ec2backupDO.getCurrentLambdaRequestId(), "The EC2BackupDO current lambda req id is empty!", new Object[0]);
        Validate.notEmpty(ec2backupDO.getOriginatingLambdaRequestId(), "The EC2BackupDO originating Lambda Req id is empty!", new Object[0]);
        Validate.notEmpty(ec2backupDO.getIterationString(), "The EC2BackupDO iteration is empty!", new Object[0]);
        Validate.notEmpty(ec2backupDO.getFilter1TagName(), "The EC2BackupDO tag name  is empty!", new Object[0]);
        Validate.notEmpty(ec2backupDO.getFilter1TagValue(), "The EC2BackupDO tag value is empty!", new Object[0]);
        Validate.notEmpty(ec2backupDO.getRequestStartTimeString(), "The EC2BackupDO request start time value is empty!", new Object[0]);
        Validate.notEmpty(ec2backupDO.getState(), "The EC2BackupDO state value is empty!", new Object[0]);
        PutItemRequest putItemRequest = new PutItemRequest();
        
        Map<String, AttributeValue> values = new HashMap<String, AttributeValue>();
        putValues(values, ID_COL_NAME, ec2backupDO.getId());
        putValues(values, CURR_REQ_ID_COL_NAME, ec2backupDO.getCurrentLambdaRequestId());
        putValues(values, ORIG_REQ_ID_COL_NAME, ec2backupDO.getOriginatingLambdaRequestId());
        putValues(values, ITERATION_COL_NAME, ec2backupDO.getIterationString());
        putValues(values, FILTER1_TAG_NAME_COL_NAME, ec2backupDO.getFilter1TagName());
        putValues(values, FILTER1_TAG_VALUE_COL_NAME, ec2backupDO.getFilter1TagValue());
        putValues(values, REQ_START_TIME_COL_NAME, ec2backupDO.getRequestStartTimeString());
        putValues(values, STATE_COL_NAME, ec2backupDO.getState());
        putValues(values, ACTION_PAUSE_SEC_NAME, ec2backupDO.getActionPauseSecString());
        putValues(values, MAX_RECURSIVE_CALLS_NAME, ec2backupDO.getMaxRecursiveCallsString());
        putValues(values, MAX_RUN_MIN_NAME, ec2backupDO.getMaxRunMinString());
        putValues(values, INSTANCE_DEPENDENCY_TAG_NAME, ec2backupDO.getInstanceDependencyTag());
        putValues(values, TIMESTAMP_TAG_NAME, ec2backupDO.getTimestampTag());
        putValues(values, INSTANCE_ID_TAG_NAME, ec2backupDO.getInstanceIdTag());
        putValues(values, DEVICE_TAG_NAME, ec2backupDO.getDeviceTag());
        putValues(values, IMAGE_ID_TAG_NAME, ec2backupDO.getImageIdTag());
        putValues(values, TAGS_TO_INCLUDE_NAME, ec2backupDO.getTagsToInclude());
        putValues(values, INSTANCES_TO_START_COL_NAME, ec2backupDO.getInstancesToStart());
//        putValues(values, ENABLE_BACKUP_TAG_NAME, ec2backupDO.getEnableBackupTag());
        putValues(values, BACKUP_STRATEGY_TAG_COL_NAME, ec2backupDO.getBackupStrategyTag());
        putValues(values, PURGE_TIMESTAMP_COL_NAME, getPurgeTimestampString());
        
        putItemRequest.setItem(values);
        putItemRequest.setTableName(TABLE_NAME);
        return dynamoDBClient.putItem(putItemRequest);
    }

    private void putValues(Map<String, AttributeValue> values, String name, String value)
    {
		Validate.notEmpty(name, "The column name given is empty!");
		
		if (!StringUtils.isEmpty(value)) {
			values.put(name, new AttributeValue(value));				
		}
    }
 

    private String getPurgeTimestampString() {
    	//                                                                      mill  sec  min  hour
    	long purgeTimestamp = System.currentTimeMillis() + (CPBackupEnv.getDatabaseRetentionDays() * 1000 * 60 * 60 * 24);
    	return purgeTimestamp + "";
    }
}
