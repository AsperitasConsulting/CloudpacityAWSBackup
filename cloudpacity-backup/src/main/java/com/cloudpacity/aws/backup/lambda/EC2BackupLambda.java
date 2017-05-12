package com.cloudpacity.aws.backup.lambda;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.cloudpacity.aws.backup.CPBackupEnv;
import com.cloudpacity.aws.backup.pojo.BackupRequest;
import com.cloudpacity.aws.backup.service.EC2Backup;
import com.cloudpacity.aws.common.CPCommonEnv;
import com.cloudpacity.aws.common.dao.EC2BackupDAO;
import com.cloudpacity.aws.common.entity.AWSSNSEntity;
import com.cloudpacity.aws.common.util.CPLogger;
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
public class EC2BackupLambda  implements RequestHandler<BackupRequest, String> {

    protected AWSCredentials awsCredentials;
    protected CPLogger logger;
    
    public EC2BackupLambda()    {
        awsCredentials = (new EnvironmentVariableCredentialsProvider()).getCredentials();
    }

    public String handleRequest(BackupRequest request, Context context)
    {
    	try {
	        logger = new CPLogger(context.getLogger());
	        logger.log("BEGIN BACKUP PROCESS");
	        logger.logSummary("Begin Backup at: " + CPCommonEnv.getFormattedDate());
	        
	        if(request == null)
	            request = new BackupRequest();
	        
	        BackupRequest backupRequest = populateBackupRequest(request, context);
	        logger.log(backupRequest.toString());
	        
	        EC2Backup ec2Backup = new EC2Backup(logger, awsCredentials);
	        String returnCode = ec2Backup.invoke(backupRequest);
	        
	        if (EC2Backup.RETURN_CODE_COMPLETE.equalsIgnoreCase(returnCode)) {
	        	AWSSNSEntity.postMessage(logger.getCompoundMessages());
	        }
	        
	        return returnCode;
		}
	   	catch (Throwable e){
	   		logger.log("Exception in EC2backup.handleRequest!");
	   		logger.log(logger.getDebugMessages());
	   		logger.log(e.getMessage());
	   		logger.log(ExceptionUtils.getStackTrace(e));
	   		return e.getMessage();
	   }  
    }

    public String handleDynamoRequest(DynamodbEvent dynamoEvent, Context context)
    {
    	this.logger = new CPLogger( context.getLogger());
        BackupRequest request = new BackupRequest();
        StreamRecord streamRecord = null;
        
        try {
	        logger.log("Begin DYNAMODB Triggered Backup function");

	    	for (DynamodbStreamRecord record: dynamoEvent.getRecords()) {
	    		
	    		if(CPBackupEnv.DYNAMODB_INSERT_EVENT.equalsIgnoreCase( record.getEventName())){
	                streamRecord = record.getDynamodb();
		    		if (streamRecord != null) {
		    			request = marshallBackupReq(streamRecord);
		    			break;
		    		}
	    		}
	        }
	        BackupRequest backupRequest = populateBackupRequest(request, context);
	        logger.log(backupRequest.toString());
	        EC2Backup ec2Backup = new EC2Backup(logger, awsCredentials);
	        
	        return ec2Backup.invoke(backupRequest);
	   	}
	   	catch (Throwable e){
	   		logger.log("Exception in EC2BackupLambda.handleDynamoRequest!");
	   		logger.log(logger.getDebugMessages());
	   		logger.log(e.getMessage());
	   		logger.log(ExceptionUtils.getStackTrace(e));
	   		return e.getMessage();
	   }
    }

    private BackupRequest marshallBackupReq(StreamRecord streamRecord)
    {
        validateDynamoEvent(streamRecord);
        Map<String, AttributeValue> columnMap = streamRecord.getNewImage();
        BackupRequest backupReq = new BackupRequest();
        backupReq.setCurrentLambdaRequestId(getDynamoColStringValue(columnMap, EC2BackupDAO.CURR_REQ_ID_COL_NAME));
        backupReq.setOriginatingLambdaRequestId(getDynamoColStringValue(columnMap, EC2BackupDAO.ORIG_REQ_ID_COL_NAME));
        backupReq.setInstancesToStart(getDynamoColStringValue(columnMap, EC2BackupDAO.INSTANCES_TO_START_COL_NAME));
        backupReq.setIterationString(getDynamoColStringValue(columnMap, EC2BackupDAO.ITERATION_COL_NAME));
        backupReq.setRequestStartTimeString(getDynamoColStringValue(columnMap, EC2BackupDAO.REQ_START_TIME_COL_NAME));
        backupReq.setFilter1TagName(getDynamoColStringValue(columnMap, EC2BackupDAO.FILTER1_TAG_NAME_COL_NAME));
        backupReq.setFilter1TagValue(getDynamoColStringValue(columnMap, EC2BackupDAO.FILTER1_TAG_VALUE_COL_NAME));
        backupReq.setState(getDynamoColStringValue(columnMap, EC2BackupDAO.STATE_COL_NAME));
        backupReq.setActionPauseSecString(getDynamoColStringValue(columnMap, EC2BackupDAO.ACTION_PAUSE_SEC_NAME));
        backupReq.setDeviceTag(getDynamoColStringValue(columnMap, EC2BackupDAO.DEVICE_TAG_NAME));
        backupReq.setInstanceIdTag(getDynamoColStringValue(columnMap, EC2BackupDAO.INSTANCE_ID_TAG_NAME));
        backupReq.setImageIdTag(getDynamoColStringValue(columnMap, EC2BackupDAO.IMAGE_ID_TAG_NAME));
        backupReq.setMaxRecursiveCallsString(getDynamoColStringValue(columnMap,  EC2BackupDAO.MAX_RECURSIVE_CALLS_NAME));
        backupReq.setInstanceDependencyTag(getDynamoColStringValue(columnMap, EC2BackupDAO.INSTANCE_DEPENDENCY_TAG_NAME));
        backupReq.setMaxRunMinutesString(getDynamoColStringValue(columnMap, EC2BackupDAO.MAX_RUN_MIN_NAME));
        backupReq.setInstanceTagsToInclude(getDynamoColStringValue(columnMap, EC2BackupDAO.TAGS_TO_INCLUDE_NAME));
        backupReq.setTimestampTag(getDynamoColStringValue(columnMap, EC2BackupDAO.TIMESTAMP_TAG_NAME));
//        backupReq.setEnableBackupTag(getDynamoColStringValue(columnMap, EC2BackupDAO.ENABLE_BACKUP_TAG_NAME));
        backupReq.setBackupStrategyTag(getDynamoColStringValue(columnMap, EC2BackupDAO.BACKUP_STRATEGY_TAG_COL_NAME));
        return backupReq;
    }

    private void validateDynamoEvent(StreamRecord record)
    {
        Validate.notNull(record, "The Dynamodb record was null!", new Object[0]);
        Validate.notNull(record.getKeys(), "The key map in the dynamodb record was null!", new Object[0]);
        Validate.notNull(record.getNewImage(), "The record new image was null!", new Object[0]);
    }

    private String getDynamoColStringValue(Map<String, AttributeValue> columnMap, String colName) {
    	
        AttributeValue value = (AttributeValue)columnMap.get(colName);
        if(value != null)
            return value.getS();
        else
            return null;
    }

    private BackupRequest populateBackupRequest(BackupRequest request, Context context)
    {
        Validate.notNull(request, "The backup request is null!", new Object[0]);
        Validate.notNull(context, "The Lambda context is null!", new Object[0]);
        BackupRequest backupRequest = new BackupRequest();
        ZoneId zoneCDT = ZoneId.of(CPBackupEnv.getDefaultTimeZone());
        ZonedDateTime iterationStartDateTime = ZonedDateTime.now(zoneCDT);
        
    	// Request State
        if(StringUtils.isEmpty(request.getState()))
            backupRequest.setState(BackupRequest.SHUTDOWN_STATE);
        else
            backupRequest.setState(request.getState());
        
    	// Current Lambda Req Id
        backupRequest.setCurrentLambdaRequestId(context.getAwsRequestId());
        
        // Request Start Time
        if(StringUtils.isEmpty(request.getRequestStartTimeString()))
            backupRequest.setRequestStartTime(iterationStartDateTime);
        else
            backupRequest.setRequestStartTime(request.getRequestStartTime());
        
        // Originating Lambda Req Id
        if(StringUtils.isEmpty(request.getOriginatingLambdaRequestId()))
            backupRequest.setOriginatingLambdaRequestId(context.getAwsRequestId());
        else
            backupRequest.setOriginatingLambdaRequestId(request.getOriginatingLambdaRequestId());
        
        // Iteration  
        if(request.getIteration() == null || request.getIteration().intValue() < 1)
            backupRequest.setIteration(Integer.valueOf(1));
        else
            backupRequest.setIteration(request.getIteration());
        
        // Pause Between Actions (secs)
        if(request.getActionPauseSec() == null || request.getActionPauseSec().intValue() < 10)
            backupRequest.setActionPauseSec(Integer.valueOf(CPBackupEnv.getActionPauseSec()));
        else
            backupRequest.setActionPauseSec(request.getActionPauseSec());
        
        // Max number of recursive Calls
        if(request.getMaxRecursiveCalls() == null || request.getMaxRecursiveCalls().intValue() < 2)
            backupRequest.setMaxRecursiveCalls(Integer.valueOf(CPBackupEnv.getMaxRecursiveCalls()));
        else
            backupRequest.setMaxRecursiveCalls(request.getMaxRecursiveCalls());
        
        // Max run time
        if(request.getMaxRunMinutes() == null || request.getMaxRunMinutes().intValue() < 5)
            backupRequest.setMaxRunMinutes(Integer.valueOf(CPBackupEnv.getMaxRunMinutes()));
        else
            backupRequest.setMaxRunMinutes(request.getMaxRunMinutes());
        
        // Tags to Include Tag
        if(StringUtils.isEmpty(request.getInstanceTagsToInclude()))
            backupRequest.setInstanceTagsToInclude(CPBackupEnv.getInstanceTagsToInclude());
        else
            backupRequest.setInstanceTagsToInclude(request.getInstanceTagsToInclude());
        
        // Creation Timestamp Tag
        if(StringUtils.isEmpty(request.getTimestampTag()))
            backupRequest.setTimestampTag(CPBackupEnv.getCreationTimestampTag());
        else
            backupRequest.setTimestampTag(request.getTimestampTag());
        
        // Instance Id Tag
        if(StringUtils.isEmpty(request.getInstanceIdTag()))
            backupRequest.setInstanceIdTag(CPBackupEnv.getInstanceIdTag());
        else
            backupRequest.setInstanceIdTag(request.getInstanceIdTag());
        
        // Instance Dependency Tag
        if(StringUtils.isEmpty(request.getInstanceIdTag()))
            backupRequest.setInstanceDependencyTag(CPBackupEnv.getInstanceDependencyTag());
        else
            backupRequest.setInstanceDependencyTag(request.getInstanceDependencyTag());
        
        // Device Tag
        if(StringUtils.isEmpty(request.getDeviceTag()))
            backupRequest.setDeviceTag(CPBackupEnv.getDeviceTag());
        else
            backupRequest.setDeviceTag(request.getDeviceTag());
        
        // Image Id Tag
        if(StringUtils.isEmpty(request.getImageIdTag()))
            backupRequest.setImageIdTag(CPBackupEnv.getImageIdTag());
        else
            backupRequest.setImageIdTag(request.getImageIdTag());
        
//        // Backup Enabled Tag
//        if(StringUtils.isEmpty(request.getEnableBackupTag()))
//            backupRequest.setEnableBackupTag(CPBackupEnv.getEnableBackupTag());
//        else
//            backupRequest.setEnableBackupTag(request.getEnableBackupTag());
        
      // Backup Strategy Tag
      if(StringUtils.isEmpty(request.getBackupStrategyTag()))
          backupRequest.setBackupStrategyTag(CPBackupEnv.getBackupStrategyTag());
      else
          backupRequest.setBackupStrategyTag(request.getBackupStrategyTag());
        
        // Instances To Start
        if(StringUtils.isEmpty(request.getInstancesToStart()))
            backupRequest.setInstancesToStart("");
        else
            backupRequest.setInstancesToStart(request.getInstancesToStart());
        
        // Filter 1 tag name      
        if(StringUtils.isEmpty(request.getFilter1TagName()))
            backupRequest.setFilter1TagName(CPBackupEnv.getFilter1TagName());
        else
            backupRequest.setFilter1TagName(request.getFilter1TagName());
        
        // Filter 1 tag value  
        if(StringUtils.isEmpty(request.getFilter1TagValue()))
            backupRequest.setFilter1TagValue(CPBackupEnv.getFilter1TagValue());
        else
            backupRequest.setFilter1TagValue(request.getFilter1TagValue());
        
        // Filter 2 tag name  
        if(StringUtils.isEmpty(request.getFilter2TagName()))
            backupRequest.setFilter2TagName(CPBackupEnv.getFilter2TagName());
        else
            backupRequest.setFilter2TagName(request.getFilter2TagName());
        
        // Filter 2 tag value  
        if(StringUtils.isEmpty(request.getFilter2TagValue()))
            backupRequest.setFilter2TagValue(CPBackupEnv.getFilter2TagValue());
        else
            backupRequest.setFilter2TagValue(request.getFilter1TagValue());
        
        return backupRequest;
    }


}
