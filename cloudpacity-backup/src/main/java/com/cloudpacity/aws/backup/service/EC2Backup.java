package com.cloudpacity.aws.backup.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.cloudpacity.aws.backup.CPBackupEnv;
import com.cloudpacity.aws.backup.pojo.BackupRequest;
import com.cloudpacity.aws.backup.pojo.EC2BackupDO;
import com.cloudpacity.aws.common.dao.EC2BackupDAO;
import com.cloudpacity.aws.common.entity.AWSImageEntity;
import com.cloudpacity.aws.common.entity.AWSInstanceEntity;
import com.cloudpacity.aws.common.error.CPRuntimeException;
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
public class EC2Backup
{
    protected AWSCredentials awsCredentials;
    protected CPLogger logger;
    protected CPBackupEnv backupEnv;
    
    public static final String RETURN_CODE_COMPLETE = "Complete";
    public static final String RETURN_CODE_RECURSIVE = "Recursive"; // call via Dynamo Trigger for recursive call
    public static final String RETURN_CODE_ERROR = "Error";
    
    
    public EC2Backup(CPLogger cpLogger, AWSCredentials awsCredentials)
    {
   		this.awsCredentials =  awsCredentials;
		this.logger = cpLogger;
		this.backupEnv = new CPBackupEnv();
    }

    public String invoke(BackupRequest backupRequest)
    {
    	String returnMessage = "OK";
    	
   		try {
	    	validateBackupRequest(backupRequest);
	    	
	        AWSInstanceEntity awsInstance = new AWSInstanceEntity(awsCredentials, CPBackupEnv.getRegionName(), logger,this.backupEnv);
	        List<Instance> candidateInstanceList = awsInstance.getInstancesForTag(backupRequest.getFilter1TagName(), backupRequest.getFilter1TagValue());
	        List<Instance> instanceList = filterOnBackupEnabledTag(candidateInstanceList);
	        
	        if(instanceList == null || instanceList.size() == 0)
	            logger.log("No instances given for image creation!");
	        
	    	// SHUTDOWN 
	   		if (BackupRequest.SHUTDOWN_STATE.equalsIgnoreCase(backupRequest.getState())) {
	   			
	            logger.log("Processing shutdown");
	            
	            List<String> instanceRestartList = backupShutdown(backupRequest, awsInstance, instanceList);
	            
	            if(backupRequest.getIteration().intValue() < 2)
	                backupRequest.setInstanceToStartList(instanceRestartList);
	            if(areInstancesReadyForBackup(instanceList))
	            {
	                logger.log("All instances have been stopped");
	                backupRequest.setState(BackupRequest.BACKUP_STATE);
	            }
	        }
	   		
	   		// BACKUP
	   		if (BackupRequest.BACKUP_STATE.equalsIgnoreCase(backupRequest.getState())) {
	            logger.log("Processing backup");
	            createBackupImages(backupRequest, awsInstance, instanceList);
		   		backupRequest.setState(BackupRequest.STARTUP_STATE);
	        }
	   		
	   		// STARTUP
	   		if (BackupRequest.STARTUP_STATE.equalsIgnoreCase(backupRequest.getState())) {
	   			
	   			logger.log("Processing startup");
	   			List<Instance> instancesToStart = restartBackupInstances(backupRequest, awsInstance, instanceList);
	        
		   		if (allInstancesInState(instancesToStart, AWSInstanceEntity.RUNNING_STATE)) {
		   			logger.log("ALL INSTANCES BACKED UP - INSTANCES In ORIGINAL STATE");
		   			return RETURN_CODE_COMPLETE;
		   		}
	   		}
	   		
	        checkTimeout(backupRequest.getRequestStartTime(), backupRequest.getIteration().intValue(), backupRequest);
	        Thread.sleep(backupRequest.getActionPauseSec().intValue() * 1000);
	        writeToDyanmoTrigger(backupRequest);
	        return returnMessage;
        
		}
		
	   	catch (Throwable e){
	   		logger.log("Exception in EC2Service.backup!");
	   		logger.log(logger.getDebugMessages());
	   		logger.log(e.getMessage());
	   		logger.log(ExceptionUtils.getStackTrace(e));
	   		return e.getMessage();
	   } 
    }

    private void writeToDyanmoTrigger(BackupRequest backupRequest)
        throws ExecutionException, InterruptedException
    {
        logger.log("Wrting to table: CloudpacityEC2Backup");
        int nextIteration = backupRequest.getIteration().intValue() + 1;
        EC2BackupDO ec2backupDO = new EC2BackupDO();
        ec2backupDO.setId((new StringBuilder(String.valueOf(backupRequest.getOriginatingLambdaRequestId()))).append("-").append(nextIteration).toString());
        ec2backupDO.setState(backupRequest.getState());
        ec2backupDO.setOriginatingLambdaRequestId(backupRequest.getOriginatingLambdaRequestId());
        ec2backupDO.setCurrentLambdaRequestId(backupRequest.getCurrentLambdaRequestId());
        ec2backupDO.setRequestStartTimeString(backupRequest.getRequestStartTimeString());
        ec2backupDO.setIteration(nextIteration);
        ec2backupDO.setFilter1TagName(backupRequest.getFilter1TagName());
        ec2backupDO.setFilter1TagValue(backupRequest.getFilter1TagValue());
        ec2backupDO.setState(backupRequest.getState());
        ec2backupDO.setInstancesToStart(backupRequest.getInstancesToStart());
        ec2backupDO.setMaxRecursiveCalls(backupRequest.getMaxRecursiveCalls().intValue());
        ec2backupDO.setMaxRunMinutes(backupRequest.getMaxRunMinutes().intValue());
        ec2backupDO.setActionPauseSec(backupRequest.getActionPauseSec().intValue());
        ec2backupDO.setActionPauseSec(backupRequest.getActionPauseSec().intValue());
        ec2backupDO.setTagsToInclude(backupRequest.getInstanceTagsToInclude());
        EC2BackupDAO ec2backupDAO = new EC2BackupDAO();
        ec2backupDAO.putItem(ec2backupDO);
        logger.log("file written to DynamoDB: CloudpacityEC2Backup");
    }

	private List<Instance> restartBackupInstances(BackupRequest backupRequest, AWSInstanceEntity awsInstance,
			List<Instance> instanceList) {
		
		List<Instance> instancesToStart = new ArrayList<Instance>();
		
		List<String> instanceIdsToStart = backupRequest.getInstancesToStartList();
		
		if (instanceIdsToStart.isEmpty()) {
			logger.log("No Instances to start, they were all stopped prior to the backup");
		}
		else{
			logger.log("The following instances will be started: " + backupRequest.getInstancesToStart());
			instancesToStart = awsInstance.getInstancesForIds(instanceIdsToStart);
		}
		
		for (Instance instance : instancesToStart) {
			
			String instanceName = AWSInstanceEntity.getTagValueFromList(this.backupEnv.getNameTag(), instance.getTags(),"");
			if (StringUtils.isEmpty(instanceName)) {
				logger.log("STARTING instance " + instance.getInstanceId() + " no Name tag found!");
			}
			else {
				logger.log("STARTING instance " + instanceName);
			}
			
			 startInstance(awsInstance,  instanceList, CPBackupEnv.getRegionName(), instance.getInstanceId(), instanceName
					, AWSInstanceEntity.getTagValueFromList(backupRequest.getInstanceDependencyTag(), instance.getTags(), "")
					,  AWSInstanceEntity.getInstanceDependencyIdArray(instance.getTags(),backupRequest.getInstanceDependencyTag()));
		}
		return instancesToStart;
	}

	/**
	 * 
	 * @param candidateInstanceList
	 * @return
	 */
    private List<Instance> filterOnBackupEnabledTag(List<Instance> candidateInstanceList)
    {
        List<Instance> instanceList = new ArrayList<Instance>();
        
        for(Instance candidateInstance: candidateInstanceList)
        {
            String backupStrategy = AWSInstanceEntity.getTagValueFromList(CPBackupEnv.getBackupStrategyTag(), candidateInstance.getTags(),"");
            String nameTag = AWSInstanceEntity.getTagValueFromList(this.backupEnv.getNameTag(), candidateInstance.getTags(),"");
           
            if(AWSInstanceEntity.BACKUP_STRATEGY_AMI_CONST.equalsIgnoreCase(backupStrategy) ||
               AWSInstanceEntity.BACKUP_STRATEGY_SNAPSHOT_STOPPED_CONST.equalsIgnoreCase(backupStrategy)||
               AWSInstanceEntity.BACKUP_STRATEGY_SNAPSHOT_RUNNING_CONST.equalsIgnoreCase(backupStrategy))
           //    BackupRequest.BACKUP_STRATEGY_SNAPSHOT_STOPPED_CONST.equalsIgnoreCase(backupStrategyTag) )
            {
                instanceList.add(candidateInstance);
                
                if(StringUtils.isEmpty(nameTag))
                    logger.log("Instance: '" + nameTag + "' id: '" + candidateInstance.getInstanceId() + "' will be backed up.");
                else
                    logger.log("Instance id: '" + candidateInstance.getInstanceId() + "' will be backed up.");
            }
            else {
            	logger.log("ERROR: instance '" + nameTag + "' does not have a valid backup strategy tag.  Tag '" + CPBackupEnv.getBackupStrategyTag() + "' value: '"+ backupStrategy + "'");
            }
        }

        return instanceList;
    }

    /**
     * 
     * @param awsInstance
     * @param instanceList
     * @param region
     * @param instanceId
     * @param instanceName
     * @param instanceDependencies
     * @param dependentInstanceIdArray
     */
	private void startInstance(AWSInstanceEntity awsInstance, List<Instance> instanceList, String region,
			String instanceId, String instanceName, String instanceDependencies, List<String> dependentInstanceIdArray) {
		
        if(StringUtils.isEmpty(instanceDependencies))
        {
            logger.log("Instance: '" + instanceName + "' id: '" + instanceId + "' is stopped and will be started.  No dependencies found.");
            awsInstance.startInstance(instanceId);
        } else
        if(allDependentInstancesInState(instanceName, dependentInstanceIdArray, "running", instanceList, region))
        {
            logger.log("Instance: '" + instanceName + "' id: '" + instanceId + "' is stopped and will be started.  Instances it depends on are running.");
            awsInstance.startInstance(instanceId);
        } else
        {
            logger.log("Instance: '" + instanceName + "' id: '" + instanceId + "' is stopped and will need to wait until dependent instances " + instanceDependencies + " are started.");
        }
    }

    private void stopInstance(AWSInstanceEntity awsInstance, List<Instance> instanceList, String region, String instanceId, String instanceName, Map<String, List<String>> dependencyMap)
    {
    	List<String> dependentInstanceIdArray = dependencyMap.get(instanceId);
        String dependentInstances = ArrayUtils.toString(dependentInstanceIdArray);
        if(dependentInstanceIdArray == null || dependentInstanceIdArray.size() == 0)
        {
            logger.log("STOPPING Instance: " + instanceName + " id: " + instanceId + " No dependencies found.");
            awsInstance.stopInstance(instanceId);
        } else
        if(allDependentInstancesInState(instanceName, dependentInstanceIdArray, "stopped", instanceList, region))
        {
            logger.log("STOPPING Instance: '" + instanceName + "' id: '" + instanceId + "'   Instances ithat depend on it are stopped.");
            awsInstance.stopInstance(instanceId);
        } else
        {
            logger.log("WAITING Instance: '" + instanceName + "' id: '" + instanceId + "' is running and will need to wait until dependent instances  '" + dependentInstances + "' are stopped.");
        }
    }

    private boolean allDependentInstancesInState(String instanceName, List<String> dependentInstanceIdArray, String desiredInstanceState, List<Instance> instanceList, String region)
    {
        if(dependentInstanceIdArray == null || dependentInstanceIdArray.size() == 0 || StringUtils.isEmpty(desiredInstanceState))
            return true;
        
        for(String dependentInstanceId: dependentInstanceIdArray)
        {
            String trimmedInstanceId = dependentInstanceId.trim();
            Instance dependentInstance = getInstanceForId(instanceList, trimmedInstanceId);
            if(dependentInstance == null)
            {
                logger.log("Instance: " + instanceName + " has dependent instance: '" + trimmedInstanceId + "' which was not found in the list of instances to, checking its state");
                AWSInstanceEntity awsInstance = new AWSInstanceEntity(awsCredentials, region, logger,this.backupEnv);
                try
                {
                    dependentInstance = awsInstance.getInstanceForId(trimmedInstanceId);
                }
                catch(Exception e)
                {
                    throw new CPRuntimeException("Error!!  Instance: " + instanceName + " has dependent instance: '" + trimmedInstanceId + "' was not found when looking up it's state");
                }
                if(dependentInstance != null)
                {
                    if(!desiredInstanceState.equalsIgnoreCase(dependentInstance.getState().getName()))
                        throw new CPRuntimeException((new StringBuilder("Error!!  Instance: ")).append(instanceName).append(" has dependent instance: '").append(trimmedInstanceId).append("' was not found in the list of instances that match the search parameters AND it was not in the state: ").append(desiredInstanceState).toString());
                } else
                {
                    throw new CPRuntimeException("Error!!  Depedent Instance: " + instanceName + " has dependent instance: '" + trimmedInstanceId + "' was not found when looking up it's state");
                }
            } else
            if(!desiredInstanceState.equalsIgnoreCase(dependentInstance.getState().getName()))
                return false;
        }

        return true;
    }

    private Instance getInstanceForId(List<Instance> instanceList, String instanceId)
    {
        if(StringUtils.isEmpty(instanceId))
            return null;
        for(Instance instance: instanceList)
        {
            if(instanceId.equalsIgnoreCase(instance.getInstanceId()))
                return instance;
        }

        return null;
    }

    private void checkTimeout(ZonedDateTime startDateTime, int iteration, BackupRequest backupRequest)
    {
        ZoneId zoneCDT = ZoneId.of(CPBackupEnv.getDefaultTimeZone());
        OffsetDateTime timeoutDateTime = startDateTime.plusMinutes(backupRequest.getMaxRunMinutes().intValue()).toOffsetDateTime();
        ZonedDateTime nowZonedTime = ZonedDateTime.now(zoneCDT);
        OffsetDateTime nowDateTime = nowZonedTime.toOffsetDateTime();
        
        logger.log("Timeout datetime (start plus " + backupRequest.getMaxRunMinutes() + " min): " + timeoutDateTime + "  current: " + nowDateTime);
      
        if(iteration > backupRequest.getMaxRecursiveCalls().intValue())
            throw new CPRuntimeException("ERROR The maximum number of recursive calls '" + backupRequest.getMaxRecursiveCalls() + "' was exceeded");
        if(startDateTime.plusMinutes(backupRequest.getMaxRunMinutes().intValue()).toOffsetDateTime().isBefore(nowDateTime))
            throw new CPRuntimeException("ERROR The maximum run time of " + backupRequest.getMaxRunMinutes() + " minutes has been exceeded!");
        else
            return;
    }

    /**
     * 
     * @param backupRequest
     * @param awsInstance
     * @param instanceList
     * @throws InterruptedException
     */
	private void createBackupImages(BackupRequest backupRequest, AWSInstanceEntity awsInstance,
			List<Instance> instanceList) throws InterruptedException 
    {
        if(areInstancesReadyForBackup(instanceList))     {
        	
            logger.log("Instances Stopped, Starting Backups");
            AWSImageEntity msiAMI = new AWSImageEntity(awsCredentials, this.backupEnv.getRegionName(), logger,this.backupEnv);
            List<String> imageIdList = msiAMI.createBackup(instanceList, backupRequest.getTagsToIncludeList(), CPBackupEnv.getBackupStrategyTag()
            		                                       , backupRequest.getTimestampTag(), backupRequest.getDeviceTag(), backupRequest.getInstanceIdTag(), backupRequest.getImageIdTag());
            
            logger.log("IMAGE Creation Started");
            msiAMI.waitForState(imageIdList, "pending", "available", 240);
            logger.log("RESTARTING INSTANCES - AMIs all in pending or available state");
            backupRequest.setState("backup");
        }
    }

	/**
	 * 
	 * @param instanceList
	 * @return
	 */
    private boolean areInstancesReadyForBackup(List<Instance> instanceList)   {
    	
        boolean allInstancesInCorrectState = true;
        
        AWSInstanceEntity instanceEntity = new AWSInstanceEntity(this.awsCredentials, CPBackupEnv.getRegionName(), this.logger, this.backupEnv);

        for(Instance instance:instanceList)   {
            String currentInstanceState = instance.getState().getName();
            if(AWSInstanceEntity.STOPPED_STATE.equalsIgnoreCase(currentInstanceState) ||
               instanceEntity.snapshotNoRebootBackup(instance, CPBackupEnv.getBackupStrategyTag()))   {
            	
                List<Tag> tags = instance.getTags();
                String instanceName = AWSInstanceEntity.getTagValueFromList(this.backupEnv.getNameTag(), tags, "");
                logger.log("Instance: '" + instanceName + "' id: '" + instance.getInstanceId() + "' is in the CORRECT state: " + currentInstanceState);
            } 
            else  {
                return false;
            }
        }

        return allInstancesInCorrectState;

    }
    
    private boolean allInstancesInState(List<Instance> instanceList, String desiredInstanceState)
    {
        boolean allInstancesInCorrectState = true;
        for(Instance instance:instanceList)
        {
            String currentInstanceState = instance.getState().getName();
            if(desiredInstanceState.equalsIgnoreCase(currentInstanceState))
            {
                List<Tag> tags = instance.getTags();
                String instanceName = AWSInstanceEntity.getTagValueFromList(this.backupEnv.getNameTag(), tags, "");
                logger.log("Instance: '" + instanceName + "' id: '" + instance.getInstanceId() + "' is in the CORRECT state: " + currentInstanceState);
            } else
            {
                return false;
            }
        }

        return allInstancesInCorrectState;

    }

	private Map<String, List<String>> populateDepedencyMap(List<Instance> instancesToStartStop, BackupRequest backuRequest){

   		Map<String, List<String>> dependencyMap = new HashMap<String, List<String>>();
   		
   		for (Instance instance : instancesToStartStop)
        {
            if(instance != null)
            {
   				List<String> dependentInstanceIdArray = AWSInstanceEntity.getInstanceDependencyIdArray(instance.getTags(),backuRequest.getInstanceDependencyTag());
   				
   				for(String instanceId : dependentInstanceIdArray)
                {
					if (dependencyMap.containsKey(instanceId)){
						dependencyMap.get(instanceId).add(instance.getInstanceId());
					}
					else {
                    	List<String> newList = new ArrayList<String>();
                        newList.add(instance.getInstanceId());
                        dependencyMap.put(instanceId, newList);
                    }
                }

            }
        }

        return dependencyMap;
    }

   	private List<String> backupShutdown(BackupRequest backupRequest, AWSInstanceEntity awsInstance,
   				List<Instance> instanceList) {
   		
        logger.log("stopping instances before backup");
        List<String> instancesToStart = new ArrayList<String>();
        AWSInstanceEntity instanceEntity = new AWSInstanceEntity(this.awsCredentials, CPBackupEnv.getRegionName(), this.logger, this.backupEnv);

        
        for(Instance instance: instanceList)
        {
            String instanceName = AWSInstanceEntity.getTagValueFromList(this.backupEnv.getNameTag(), instance.getTags(),"");
            
            // check the ShutdownBeforeBackup tag to see if the instance should be stopped before taking the backup
            if (instanceEntity.amiBackup(instance, CPBackupEnv.getBackupStrategyTag())) {
	            if(!AWSInstanceEntity.STOPPED_STATE.equalsIgnoreCase(instance.getState().getName()))
	            {
	                stopInstance(awsInstance, instanceList, CPBackupEnv.getRegionName(), instance.getInstanceId(), instanceName, populateDepedencyMap(instanceList, backupRequest));
	                instancesToStart.add(instance.getInstanceId());
	                logger.log("Instance to be restarted: " + instanceName + " id: " + instance.getInstanceId());
	            } 
	            else
	            {
	            	logger.log("Instance: " + instanceName + " id: " + instance.getInstanceId() + " is already stopped.");
	            }
            }
            else {
            	logger.log("Instance: " + instanceName + " id: " + instance.getInstanceId() + " no shutdown required for backup.");

            }
        }

        return instancesToStart;
    }
   	


    private void validateBackupRequest(BackupRequest backupRequest)
    {
        Validate.notNull(backupRequest, "The BackupRequest was null!", new Object[0]);
        Validate.notEmpty(backupRequest.getFilter1TagName(), "The Filter 1 Name tag is empty!  You must enter 1 name value pair.", new Object[0]);
        Validate.notEmpty(backupRequest.getFilter1TagValue(), "The Filter 1 Value tag is empty! You must enter 1 name value pair.", new Object[0]);
        Validate.notNull(backupRequest.getMaxRunMinutes(), "The Max Run Minutes is null!.", new Object[0]);
        Validate.notNull(backupRequest.getActionPauseSec(), "The pause between actions (PauseSecs) is null!.", new Object[0]);
        Validate.notNull(backupRequest.getMaxRecursiveCalls(), "The MaxRecursiveCalls is null!.", new Object[0]);
    }


}
