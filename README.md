<img align="right" src="https://github.com/gradleutil/gradle-aws-plugin/raw/master/docs/%E9%83%BD%E5%85%83%E3%83%80%E3%82%A4%E3%82%B9%E3%82%B1.png">

Gradle AWS Plugin
=================

都元ダイスケ Daisuke Miyamoto ([@dai0304](https://github.com/dai0304)), the original maintainer of this project, passed away on February 17, 2020.
RIP :cherry_blossom: :rosette: :hibiscus:

<hr style="clear:both;"/>
Gradle plugin to manage AWS resources.

![build status](https://github.com/gradleutil/gradle-aws-plugin/workflows/Build%20master%20branch/badge.svg)

[![Join the chat at https://gitter.im/gradle-aws-plugin/Lobby](https://badges.gitter.im/gradle-aws-plugin/Lobby.svg)](https://gitter.im/gradle-aws-plugin/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


Current Features / Supported AWS Products
-----------------------------------------

* S3
  * Create bucket
  * Delete bucket
  * Upload object(s)
  * Delete object(s)
  * File sync
  * Set bucket policy
  * Set website configuration
* EC2
  * Run instance
  * Start instance
  * Stop instance
  * Terminate instance
  * Import key
  * Create security group
  * Delete security group
  * Authorize security group ingress permissions
  * Authorize security group egress permissions
  * Revoke security group ingress permissions
  * Revoke security group egress permissions
  * Wait for specific status on instance
* RDS
  * Create DB instance
  * Delete DB instance
  * Modify DB instance
  * Migrate (create or modify) DB instance
  * Reboot DB instance
  * Wait for specific status on DB instance
* Route53
  * Create hosted zone
  * Delete hosted zone
  * Change record set
* Elastic Beanstalk
  * Create or delete applications
  * Create or terminate environments
  * Create or delete configuration templates
  * Create or delete application versions
  * Wait for specific status on environment
* CloudFormation
  * Migrate (create or update) stack
  * Delete stack
  * Wait for specific status on stack
* Lambda
  * Create function
  * Update function code
  * Update function configuration
  * Migrate (create or update) function
  * Invoke function
  * Delete function
  * Publish function version
  * Create alias
  * Update alias
* IAM
  * Create role
  * Attach role policy
* ELB
  * (TBD)
* SQS
  * Send messages
  * Delete messages
  * Read messages
* SNS
  * Publish message
* SSM
  * Put parameters

Requirements
------------

* Java 8+
* Gradle 6.8+

Usage
-----

Add this to your `build.gradle`:

```groovy
buildscript {
  repositories {
    mavenCentral()
    maven { url "https://plugins.gradle.org/m2/" }
  }
  dependencies {
    classpath "gradleutil.aws:gradle-aws-plugin:0.42"
  }
}

apply plugin: 'gradleutil.aws'

aws {
  profileName = 'credentials-profile-name-in-your-profile-configuration-file (~/.aws/credentials)'
  region = 'ap-northeast-1'
}
```

These credentials are used to make API accesses by default. The format of the credentials file is described in the [Amazon AWS Docs](http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html#credentials-file-format).

### S3 Create bucket

```groovy
apply plugin: 'gradleutil.aws.s3'

task createBucket(type: CreateBucketTask) {
	bucketName myBucketName

	// one of http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region values, us-east-1 by default
	region regionName
	// create bucket only if it does not exist, otherwise skip
	ifNotExists true
}
```

Look at [S3 example 1](samples/01-s3-upload-simple) for more information.

### S3 files tasks

```groovy
apply plugin: 'gradleutil.aws.s3'

task syncObjects(type: SyncTask) {
  bucketName 'foobar.example.com'
  source file('path/to/objects')
}
```

Look at [S3 example 1](samples/01-s3-upload-simple) and [S3 example 2](samples/02-s3-sync-contents) for more information.


### EC2 instance tasks

```groovy
apply plugin: 'gradleutil.aws.ec2'

// You can overwrite default credentials and region settings like this:
// ec2 {
//   profileName 'another-credentials-profile-name' // optional
//   region = 'us-east-1'
// }

task stopBastion(type: AmazonEC2StopInstanceTask) {
  instanceIds += 'i-12345678'
}

task startBastion(type: AmazonEC2StartInstanceTask) {
  instanceIds += 'i-12345678'
}
```

Look at [EC2 example](samples/03-ec2) for more information.


### RDS DB instance tasks

```groovy
apply plugin: "gradleutil.aws.rds"

// You can overwrite default credentials and region settings like this:
// rds {
//   profileName 'another-credentials-profile-name' // optional
//   region = 'us-east-1'
// }

task migrateDBInstance(type: AmazonRDSMigrateDBInstanceTask) {
	dbInstanceIdentifier = "foobar"
	allocatedStorage = 5
	dbInstanceClass = "db.t2.micro"
	engine = "MySQL"
	masterUsername = "root"
	masterUserPassword = "passW0rd"
	vpcSecurityGroupIds = [ "sg-d3958fbf" ]
	dbSubnetGroupName = "default"
	multiAZ = false
	publiclyAccessible = true
}

task rebootDBInstance(type: AmazonRDSRebootDBInstanceTask) {
	dbInstanceIdentifier = "foobar"
}

task deleteDBInstance(type: AmazonRDSDeleteDBInstanceTask) {
	dbInstanceIdentifier = "foobar"
	skipFinalSnapshot = true
}
```

Look at [RDS example](samples/07-rds) for more information.


### Route 53 hosted zone tasks

```groovy
apply plugin: 'gradleutil.aws.route53'

task createHostedZone(type: CreateHostedZoneTask) {
	hostedZoneName "foobar.example.com"
	callerReference '0BF44985-9D79-BF3B-A9B0-5AE24D6E86E1'
}

task deleteHostedZone(type: DeleteHostedZoneTask) {
	hostedZoneId "XXXX"
}
```

Look at [Route 53 example](samples/04-route53) for more information.


### Elastic Beanstalk environment tasks

```groovy
apply plugin: 'gradleutil.aws.beanstalk'
beanstalk {
  String extension = project.war.archiveName.tokenize('.').last()
  String timestamp = new Date().format("yyyyMMdd'_'HHmmss", TimeZone.default)

  appName 'foobar'
  appDesc 'foobar demo application'
  
  version {
    label = "foobar-${project.war.version}-${timestamp}"
    description = "${artifactId} v${version}"
    bucket = 'sample-bucket'
    key = "eb-apps/foobar-${project.war.version}-${timestamp}.${extension}"
  }
  
  configurationTemplates {
    production {
      optionSettings = file('src/main/config/production.json')
      solutionStackName = '64bit Amazon Linux 2013.09 running Tomcat 7 Java 7'
    }
    development {
      optionSettings = file('src/main/config/development.json')
      solutionStackName = '64bit Amazon Linux 2013.09 running Tomcat 7 Java 7'
    }
  }
  
  environment {
    envName = 'foobar'
    envDesc = 'foobar demo application development environment'
    templateName = 'development'
    versionLabel = "foobar-${project.war.version}-${timestamp}"
  }
}

// task awsEbMigrateEnvironment, awsEbDeleteApplication and so on are declared
```

Look [Elastic Beanstalk example](samples/05-beanstalk) for more information.


### CloudFormation stack tasks

```groovy
apply plugin: 'gradleutil.aws.cloudformation'

cloudFormation {
  stackName 'foobar-stack'
  stackParams([
    Foo: 'bar',
    Baz: 'qux'
  ])
  stackTags([
    Bar: 'foo',
    Baz: 'fox'
  ])
  capabilityIam true
  templateFile project.file("foobar.template")
  templateBucket 'example-bucket'
  templateKeyPrefix 'foobar/'
}

// awsCfnMigrateStack and awsCfnDeleteStack task (and so on) are declared.
```

Look at [CloudFormation example](samples/06-cloudformation) for more information.


### Lambda function tasks

```groovy
apply plugin: "base"
apply plugin: "gradleutil.aws.lambda"
aws {
	profileName = "default"
	region = "ap-northeast-1"
}

lambda {
	region = "us-east-1"
}

task zip(type: Zip) {
	from "function/"
	destinationDir file("build")
}

task migrateFunction(type: AWSLambdaMigrateFunctionTask, dependsOn: zip) {
	functionName = "foobar"
	role = "arn:aws:iam::${aws.accountId}:role/lambda-poweruser"
	zipFile = zip.archivePath
	handler = "DecodeBase64.handler"
	alias = 'DEV'
	environment = [
	    p1: "Value",
	    p2: "Value2"
	]
	tags = [
	    p1: "Value",
	    p2: "Value2"    
	]
}

task invokeFunction(type: AWSLambdaInvokeTask) {
	functionName = "foobar"
	invocationType = InvocationType.RequestResponse
	payload = file("sample-input/input.txt")
	doLast {
		println "Lambda function result: " + new String(invokeResult.payload.array(), "UTF-8")
	}
}

task deleteFunction(type: AWSLambdaDeleteFunctionTask) {
	functionName = "foobar"
}

task publishVersionFunction(type: AWSLambdaPublishVersionTask, dependsOn: migrateFunction) {
	functionName = "foobar"
}

task createAlias(type: AWSLambdaCreateAliasTask, dependsOn: publishVersionFunction) {
	functionName = "foobar"
	aliasName = "alias"
	functionVersion = "1"
}

task updateAlias(type: AWSLambdaUpdateAliasTask, dependsOn: createAlias) {
	functionName = "foobar"
    aliasName = "alias"
	functionVersion = "1"
    routingConfig {
        additionalVersionWeight = 0.7
		useNextVersion = true
    }
}

task updateLambdaFunctionCode(type: AWSLambdaUpdateFunctionCodeTask) {
    functionName = "fooBar"
    zipFile = zip.archivePath
}
```

Look at [Lambda example](samples/08-lambda) for more information.

### SQS tasks

```groovy
apply plugin: "gradleutil.aws.sqs"

task sendMessages(type: AmazonSQSSendMessagesTask) {
	queueName 'gradle-aws-plugin-sample'
	messages Stream.of("Test 1", "Test 2")
}

task deleteMessages(type: AmazonSQSMessageConsumerTask) {
	queueName 'gradle-aws-plugin-sample'
	showMessages false
}

task viewMessages(type: AmazonSQSMessageConsumerTask) {
	queueName 'gradle-aws-plugin-sample'
	deleteMessages false
	maxNumberOfMessages 50
}
```

Look at [SQS example](samples/09-sqs) for more information.

### SNS tasks
```groovy
apply plugin: "gradleutil.aws.sns"

task publishMessage(type: AmazonSNSPublishMessageTask) {
	topicArn 'arn:aws:sns:us-east-1:000000000000:gradle-aws-plugin-sns-topic'
	message 'Test body'
	subject 'Optional test subject'
}

task publishJsonMessage(type: AmazonSNSPublishMessageTask) {
	topicArn 'arn:aws:sns:us-east-1:000000000000:gradle-aws-plugin-sns-topic'
	message JsonOutput.toJson(['default': 'Default message body.',
							   'email'  : 'Email message body.',
							   'sms': 'SMS message body.'])
	messageStructure 'json'
}
```
Look at [SNS example](samples/10-sns) for more information.

License
-------

Distributed under the Apache License v2.0.  See the file [copyright/LICENSE.txt](copyright/LICENSE.txt).

Development and Contribution
----------------------------
We are open to contributions.

To contribute to the plugin or make your own modifications, including the ability
to publish your build artifacts to your own Maven repository see: [development](docs/development.md).
