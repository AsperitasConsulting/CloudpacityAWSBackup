package com.cloudpacity.aws.backup.pojo;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
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
public class BackupRequest
{
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH.mm.ss.SSS-z";
    public static final String DEFAULT_TIME_ZONE = "America/Chicago";
    public static final String SHUTDOWN_STATE = "shutdown";
    public static final String BACKUP_STATE = "backup";
    public static final String STARTUP_STATE = "backup";

    
    
    private ZonedDateTime requestStartTime;
    private String originatingLambdaRequestId;
    private String currentLambdaRequestId;
    private String state;
    private Integer iteration;
    private Integer actionPauseSec;
    private Integer maxRecursiveCalls;
    private Integer maxRunMinutes;
    private String instanceDependencyTag;
    private String timestampTag;
    private String instanceIdTag;
    private String deviceTag;
    private String imageIdTag;
    private String instancesToStart;
//	private String shutdownFlagTag;    // the tag to indicate whether the instance should be shutdown before backup
//    private String enableBackupTag;
    private String backupStrategyTag;
    private String filter1TagName;
    private String filter1TagValue;
    private String filter2TagName;
    private String filter2TagValue;
    private String filter3TagName;
    private String filter3TagValue;
    private String filter4TagName;
    private String filter4TagValue;
    private String filter5TagName;
    private String filter5TagValue;
    private String filter6TagName;
    private String filter6TagValue;
    private String instanceTagsToInclude;

    public BackupRequest()
    {
    }

    public String getOriginatingLambdaRequestId()
    {
        return originatingLambdaRequestId;
    }

    public void setOriginatingLambdaRequestId(String originatingLambdaRequestId)
    {
        this.originatingLambdaRequestId = originatingLambdaRequestId;
    }

    public String getCurrentLambdaRequestId()
    {
        return currentLambdaRequestId;
    }

    public void setCurrentLambdaRequestId(String currentLambdaRequestId)
    {
        this.currentLambdaRequestId = currentLambdaRequestId;
    }

//    public String getShutdownFlagTag() {
//		return shutdownFlagTag;
//	}
//
//	public void setShutdownFlagTag(String shutdownFlagTag) {
//		this.shutdownFlagTag = shutdownFlagTag;
//	}

    public String getState()
    {
        return state;
    }

    public String getBackupStrategyTag() {
		return backupStrategyTag;
	}

	public void setBackupStrategyTag(String backupStrategyTag) {
		this.backupStrategyTag = backupStrategyTag;
	}

	public void setState(String state)
    {
        this.state = state;
    }

    public ZonedDateTime getRequestStartTime()
    {
        return requestStartTime;
    }

    public void setRequestStartTime(ZonedDateTime requestStartTime)
    {
        this.requestStartTime = requestStartTime;
    }

    public String getRequestStartTimeString()
    {
        if(getRequestStartTime() != null)
        {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH.mm.ss.SSS-z");
            return requestStartTime.format(dateFormatter);
        } else
        {
            return "";
        }
    }

    public void setRequestStartTimeString(String dateString)
    {
        if(dateString != null)
        {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH.mm.ss.SSS-z");
            LocalDateTime datetime = LocalDateTime.parse(dateString, dateFormatter);
            ZoneId zone = ZoneId.of("America/Chicago");
            requestStartTime = ZonedDateTime.of(datetime, zone);
        }
    }

	public void setTagsToIncludeList(List<String> tagsToIncludeList) {
		String tmpTags = "";
		if (tagsToIncludeList != null) {
			for (String tag : tagsToIncludeList) {
				if (StringUtils.isEmpty(tmpTags)) {
					tmpTags = tag;
				} else {
					tmpTags = tmpTags + "," + tag;
				}
			}
			setInstanceTagsToInclude(tmpTags);
		}
    }

    public List<String> getTagsToIncludeList()
    {
		List<String> tagsToIncludeList = new ArrayList<String>();
		if (StringUtils.isEmpty(this.instanceTagsToInclude)) {
			return tagsToIncludeList;
		}
		String[] idArray = StringUtils.split(this.instanceTagsToInclude ,",");
		
		for (String tag : idArray){
			if (StringUtils.isNotEmpty(tag)) {
				tagsToIncludeList.add(tag.trim());
			}
		}
		return tagsToIncludeList;
    }

    public String getInstanceTagsToInclude()
    {
        return instanceTagsToInclude;
    }

    public void setInstanceTagsToInclude(String instanceTagsToInclude)
    {
        this.instanceTagsToInclude = instanceTagsToInclude;
    }

    public Integer getIteration()
    {
        return iteration;
    }

    public void setIteration(Integer iteration)
    {
        this.iteration = iteration;
    }

    public void setIterationString(String iterationString)
    {
        if(iterationString != null)
            iteration = new Integer(iterationString);
    }

    public String getInstancesToStart()
    {
        return instancesToStart;
    }

    public Integer getActionPauseSec()
    {
        return actionPauseSec;
    }

    public void setActionPauseSec(Integer actionPauseSec)
    {
        this.actionPauseSec = actionPauseSec;
    }

    public void setActionPauseSecString(String actionPauseSecString)
    {
        if(actionPauseSecString != null)
            actionPauseSec = new Integer(actionPauseSecString);
    }

    public Integer getMaxRecursiveCalls()
    {
        return maxRecursiveCalls;
    }

    public void setMaxRecursiveCalls(Integer maxRecursiveCalls)
    {
        this.maxRecursiveCalls = maxRecursiveCalls;
    }

    public void setMaxRecursiveCallsString(String maxRecursiveCallsString)
    {
        if(maxRecursiveCallsString != null)
            maxRecursiveCalls = new Integer(maxRecursiveCallsString);
    }

    public Integer getMaxRunMinutes()
    {
        return maxRunMinutes;
    }

    public void setMaxRunMinutes(Integer maxRunMinutes)
    {
        this.maxRunMinutes = maxRunMinutes;
    }

    public void setMaxRunMinutesString(String maxRunMinutesString)
    {
        if(maxRunMinutesString != null)
            maxRunMinutes = new Integer(maxRunMinutesString);
    }

    public String getInstanceDependencyTag()
    {
        return instanceDependencyTag;
    }

    public void setInstanceDependencyTag(String instanceDependencyTag)
    {
        this.instanceDependencyTag = instanceDependencyTag;
    }

    public String getTimestampTag()
    {
        return timestampTag;
    }

    public void setTimestampTag(String timestampTag)
    {
        this.timestampTag = timestampTag;
    }

    public String getInstanceIdTag()
    {
        return instanceIdTag;
    }

    public void setInstanceIdTag(String instanceIdTag)
    {
        this.instanceIdTag = instanceIdTag;
    }

    public String getDeviceTag()
    {
        return deviceTag;
    }

    public void setDeviceTag(String deviceTag)
    {
        this.deviceTag = deviceTag;
    }

    public String getImageIdTag()
    {
        return imageIdTag;
    }

    public void setImageIdTag(String imageIdTag)
    {
        this.imageIdTag = imageIdTag;
    }

//    public String getEnableBackupTag()
//    {
//        return enableBackupTag;
//    }
//
//    public void setEnableBackupTag(String enableBackupTag)
//    {
//        this.enableBackupTag = enableBackupTag;
//    }

    public String getFilter1TagName()
    {
        return filter1TagName;
    }

    public void setFilter1TagName(String filter1TagName)
    {
        this.filter1TagName = filter1TagName;
    }

    public String getFilter1TagValue()
    {
        return filter1TagValue;
    }

    public void setFilter1TagValue(String filter1TagValue)
    {
        this.filter1TagValue = filter1TagValue;
    }

    public String getFilter2TagName()
    {
        return filter2TagName;
    }

    public void setFilter2TagName(String filter2TagName)
    {
        this.filter2TagName = filter2TagName;
    }

    public String getFilter2TagValue()
    {
        return filter2TagValue;
    }

    public void setFilter2TagValue(String filter2TagValue)
    {
        this.filter2TagValue = filter2TagValue;
    }

    public String getFilter3TagName()
    {
        return filter3TagName;
    }

    public void setFilter3TagName(String filter3TagName)
    {
        this.filter3TagName = filter3TagName;
    }

    public String getFilter3TagValue()
    {
        return filter3TagValue;
    }

    public void setFilter3TagValue(String filter3TagValue)
    {
        this.filter3TagValue = filter3TagValue;
    }

    public String getFilter4TagName()
    {
        return filter4TagName;
    }

    public void setFilter4TagName(String filter4TagName)
    {
        this.filter4TagName = filter4TagName;
    }

    public String getFilter4TagValue()
    {
        return filter4TagValue;
    }

    public void setFilter4TagValue(String filter4TagValue)
    {
        this.filter4TagValue = filter4TagValue;
    }

    public String getFilter5TagName()
    {
        return filter5TagName;
    }

    public void setFilter5TagName(String filter5TagName)
    {
        this.filter5TagName = filter5TagName;
    }

    public String getFilter5TagValue()
    {
        return filter5TagValue;
    }

    public void setFilter5TagValue(String filter5TagValue)
    {
        this.filter5TagValue = filter5TagValue;
    }

    public String getFilter6TagName()
    {
        return filter6TagName;
    }

    public void setFilter6TagName(String filter6TagName)
    {
        this.filter6TagName = filter6TagName;
    }

    public String getFilter6TagValue()
    {
        return filter6TagValue;
    }

    public void setFilter6TagValue(String filter6TagValue)
    {
        this.filter6TagValue = filter6TagValue;
    }

    public void setInstancesToStart(String instancesToStart)
    {
        this.instancesToStart = instancesToStart;
    }

    public void setInstanceToStartList(List<String> instancesToStartList)
    {
        String tmpIds = "";
        if(instancesToStartList != null)
        {
            for(String instanceId: instancesToStartList)
            {
                if(StringUtils.isEmpty(tmpIds))
                    tmpIds = instanceId;
                else
                    tmpIds = (new StringBuilder(String.valueOf(tmpIds))).append(",").append(instanceId).toString();
            }

            setInstancesToStart(tmpIds);
        }
    }

    public List<String> getInstancesToStartList()
    {
        List<String> dependentIdList = new ArrayList<String>();
        if(StringUtils.isEmpty(instancesToStart))
            return dependentIdList;
        String idArray[] = StringUtils.split(instancesToStart, ",");
        String as[];
        int j = (as = idArray).length;
        for(int i = 0; i < j; i++)
        {
            String instanceId = as[i];
            if(StringUtils.isNotEmpty(instanceId))
                dependentIdList.add(instanceId.trim());
        }

        return dependentIdList;
    }

    public String toString()
    {
        String output = (new StringBuilder("requestStartTime: ")).append(getRequestStartTimeString()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; originatingLambdaRequestId: ").append(getOriginatingLambdaRequestId()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; currentLambdaRequestId: ").append(getCurrentLambdaRequestId()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; iteration: ").append(getIteration()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; actionPauseSec: ").append(getActionPauseSec()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; maxRecursiveCalls: ").append(getMaxRecursiveCalls()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; maxRunMinutes: ").append(getMaxRunMinutes()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; backupStrategy: ").append(getBackupStrategyTag()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; instanceDependencyTag: ").append(getInstanceDependencyTag()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter1TagName: ").append(getFilter1TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter1TagValue: ").append(getFilter1TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter2TagName: ").append(getFilter2TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter2TagValue: ").append(getFilter2TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter3TagName: ").append(getFilter3TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter3TagValue: ").append(getFilter3TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter4TagName: ").append(getFilter4TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter4TagValue: ").append(getFilter4TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter5TagName: ").append(getFilter5TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter5TagValue: ").append(getFilter5TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter6TagName: ").append(getFilter6TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter6TagValue: ").append(getFilter6TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; instanceTagsToInclude: ").append(getInstanceTagsToInclude()).toString();
                return output;
    }


}
