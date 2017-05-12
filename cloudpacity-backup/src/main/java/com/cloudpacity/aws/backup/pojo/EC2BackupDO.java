package com.cloudpacity.aws.backup.pojo;

import java.io.Serializable;
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
public class EC2BackupDO
    implements Serializable
{
    private static final long serialVersionUID = 0xc54146805f8a649dL;
    private String id;
    private String originatingLambdaRequestId;
    private String currentLambdaRequestId;
    private int iteration;
    private String requestStartTimeString;
    private String state;
    private String instancesToStart;
    private int actionPauseSec;
    private int maxRecursiveCalls;
    private int maxRunMinutes;
    private String instanceDependencyTag;
    private String timestampTag;
    private String instanceIdTag;
    private String deviceTag;
    private String imageIdTag;
    private String tagsToInclude;
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
    public EC2BackupDO()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
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

    public int getIteration()
    {
        return iteration;
    }

    public void setIteration(int iteration)
    {
        this.iteration = iteration;
    }

    public String getIterationString()
    {
		try {
			return Integer.toString(this.iteration);
		}
		catch(NumberFormatException nfe) {
			return "";
		}
	}

    public String getRequestStartTimeString()
    {
        return requestStartTimeString;
    }

    public void setRequestStartTimeString(String requestStartTimeString)
    {
        this.requestStartTimeString = requestStartTimeString;
    }

    public int getActionPauseSec()
    {
        return actionPauseSec;
    }

    public String getActionPauseSecString()
    {
		try {
			return Integer.toString(this.actionPauseSec);
		}
		catch(NumberFormatException nfe) {
			return "";
		}
    }

    public void setActionPauseSec(int actionPauseSec)
    {
        this.actionPauseSec = actionPauseSec;
    }

    public int getMaxRecursiveCalls()
    {
        return maxRecursiveCalls;
    }

    public void setMaxRecursiveCalls(int maxRecursiveCalls)
    {
        this.maxRecursiveCalls = maxRecursiveCalls;
    }

    public String getMaxRecursiveCallsString()
    {
		try {
			return Integer.toString(this.maxRecursiveCalls);
		}
		catch(NumberFormatException nfe) {
			return "";
		}
    }

    public int getMaxRunMinutes()
    {
        return maxRunMinutes;
    }

    public void setMaxRunMinutes(int maxRunMinutes)
    {
        this.maxRunMinutes = maxRunMinutes;
    }

    public String getMaxRunMinString()
    {
		try {
			return Integer.toString(this.maxRunMinutes);
		}
		catch(NumberFormatException nfe) {
			return "";
		}
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

    public String getTagsToInclude()
    {
        return tagsToInclude;
    }

    public void setTagsToInclude(String tagsToInclude)
    {
        this.tagsToInclude = tagsToInclude;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
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

    public String getBackupStrategyTag() {
		return backupStrategyTag;
	}

	public void setBackupStrategyTag(String backupStrategyTag) {
		this.backupStrategyTag = backupStrategyTag;
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

    public String getInstancesToStart()
    {
        return instancesToStart;
    }

    public void setInstancesToStart(String instancesToStart)
    {
        this.instancesToStart = instancesToStart;
    }

    public void setInstanceToStartList(List instancesToStartList)
    {
        String tmpIds = "";
        if(instancesToStartList != null)
        {
            for(Iterator iterator = instancesToStartList.iterator(); iterator.hasNext();)
            {
                String instanceId = (String)iterator.next();
                if(StringUtils.isEmpty(tmpIds))
                    tmpIds = instanceId;
                else
                    tmpIds = (new StringBuilder(String.valueOf(tmpIds))).append(",").append(instanceId).toString();
            }

            setInstancesToStart(tmpIds);
        }
    }

    public List getInstancesToStartList()
    {
        List dependentIdList = new ArrayList();
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


}
