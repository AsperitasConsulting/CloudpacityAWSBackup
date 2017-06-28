# <p style="text-align: center;">Cloudpacity Backup Documentation</p>

## Overview
========

The Cloudpacity Backup facility provides an AWS Lambda based set of
functions that will backup AWS EC2 instances in three different ways:

> **AMI** – requires stop/start of running instances
>
> **SnapshotRunning**– takes snapshots of all EBS volumes without
> rebooting the instance.
>
> **Snapshot** – takes snapshots of all EBS volumes with a reboot of the
> instance.

## Functional Summary
==================

**Selection of Instances:** The instances to be backed up need to have
the tag BackupStrategy specified, otherwise the instance will not be
considered for backup. Additionally, a filter tag (name/value pair) can
be specified to filter the instances looked at (Filter1TagName &
Filter1TagValue).

**Determine Order of Start/Stop:** The ***InstanceDependencies*** tag is
an optional tag that identifies the ids of other instances that an
instance has dependencies on. For startup, this indicates which
instances should be started BEFORE this instance is started.  In the
case of shutdown, it represents instances that should be shut down AFTER
this instance is stopped.  If an instance is running and it’s date is
not the current date and it’s time is not with the time range, the
instance will be shut down.

**Tag & Default Overrides:** Since many organizations have standards on
tag naming, environment variables can be passed to change the tags that
are used for the day, begin time and end time. Environment variable can
also be passed in to override the default maximum run time, maximum
recursive calls, pause between recursive calls and default time zone.
See below for details

**Multiple Backup Jobs:** Multiple backup jobs (Lambda functions) can be
created utilizing the CloudFormation scripts
CloudpacityBackupJobCreation-CF.yaml AFTER the basic backup service has
been created with the CloudpacityBackupServiceCreation-CF.yaml script

## Installation
============

1. Upload the project jar file into an AWS S3 bucket of your choosing. It can be found in the project root directory (cloudpacity-backup-&lt;version&gt;.jar): <https://github.com/AsperitasConsulting/CloudpacityAWSBackup/tree/master/cloudpacity-backup>

2. Download the AWS CloudFormation script in the scripts directory: <https://github.com/AsperitasConsulting/CloudpacityAWSBackup/blob/master/cloudpacity-backup/scripts/CloudpacityBackupServiceCreation-CF.yaml>

3. Run AWS CloudFormation script CloudpacityBackupServiceCreation-CF.yaml and enter the following
parameters:

***BackupFilterTagName*** – the name of the tag that will be used as a
filter for the instances to be backed up

***BackupFilterTagValue*** – the value of the tag that will be used as a
filter for the instances to be backed up

***BackupLambdaFunctionName*** – The name you give the Lambda function for
the Backup job

***BackupRoleName*** – the name of the role that will be created for the
Backup Lambda function

***BackupTriggerLambdaFunctionName*** – The name you give to the Lambda
function that will be recursively called if the Backup Lambda function
goes beyond the 5 minute limit.

***InstanceTagsToInclude*** – A common delimited list of instance tags that
should be applied to the AMI and/or Snapshots. Note: Timestamp and
instance id tags will be added by default and the name of the
AMI/Snapshot will be a concatenation of the instance name plus the
timestamp. Snapshots get the device placed at the end of their name to
provide uniqueness.

***NotificationEmail*** – the email to be notified when the job completes

***S3CodeBucket*** – the bucket where the Backup jar is located

***S3CodeKey*** – the filename of the jar in the bucket e.g. cloudpacity-backup-&lt;version&gt;.jar

***Schedule*** – the schedule on which the Backup job should be run. Format
rate(x minutes), rate(x hours), rate(x days), cron(&lt;mins&gt;
&lt;hours&gt; &lt;day of month&gt; &lt;month&gt; &lt;day of week&gt;
&lt;year&gt;) e.g. cron(0 12 ? \* SUN-SAT \*) represents everyday at
noon

## Architecture
============

**Backup Architecture**

![Backup Architecture](https://raw.githubusercontent.com/AsperitasConsulting/CloudpacityAWSBackup/master/cloudpacity-backup/doc/CloudpacityBackupDiagrams.png?token=AZWkngTcCic2LnPnrqSBNH6mNBbRJ9boks5ZXQ2FwA%3D%3D )


## EC2 Instance Tags Required
==========================

***BackupStrategy*** (required)- determines the method to be used when
backing up the instance. The instance will not be backed up if the tag
is not specified or if it has an invalid value.

> **AMI** – requires stop/start of running instances
>
> **SnapshotRunning**– takes snapshots of all EBS volumes without
> rebooting the instance.
>
> **Snapshot** – takes snapshots of all EBS volumes with a reboot of the
> instance.

***BackupRetentionDays*** (optional)- this will override the default
retention days for the AMIs and/or snapshot created. The default it 30.

***InstanceDependencies*** (optional)- a comma delimited list of
instance ids for the instances that this instance depends on.  For
startup, this indicates which instances should be started BEFORE this
instance is started.  In the case of shutdown, it represents instances
that should be shut down AFTER this instance is stopped.  If an instance
is running and its date is not the current date and it’s time is not
with the time range, the instance will be shut down.

## Lambda Environment Variables
============================

The following environment variables can be specified for the Lambda
function to override default behavior.

***PauseSecs*** – the seconds to pause between recursive calls

***MaxRecursiveCalls*** – the maximum number of recursive calls

***MaxRunMinutes*** – the maximum total run time

***TimeZone*** – the default time zone e.g UTC, CDT, PST, etc

***Filter1TagName*** – the name of the tag used to filter instances
being stopped/started

***Filter1TagValue*** – the value of the tag used to filter instances to
stopped/started

***BackupStrategyTag*** – override the default tag name used to specify
the backup strategy – default BackupStrategy

***DeviceTag*** - override the default tag name to use to specify the
device id on snapshots – default Device

***ImageIdTag*** - override the default tag name to use to specify
instance dependencies – default InstanceDependencies

***InstanceDependenciesTag*** - override the default tag name to use to
specify the image id on snapshots – default ImageId

***InstanceIdTag*** – override the default tag used to specify the
instanceId – default InstanceId

***TimestampTag*** – override the default tag used to specify the
creation timestamp – default CreationTimestamp