cloud-slang
==============
 
CloudSlang is a YAML based language for writing human-readable workflows for the CloudSlang Orchestration Engine (Score). This project includes the CLI to trigger flows.

[![Build Status](https://travis-ci.org/cloudslang/cloud-slang.svg?branch=master)](https://travis-ci.org/cloudslang/cloud-slang)

#### Getting started:

###### Pre-Requisite: Java JRE >= 7

1. Download the slang zip from [here](https://github.com/cloudslang/cloud-slang/releases/download/slang-CLI-v0.2.1/score-lang-cli.zip).
2. Unzip it.
3. Go to the folder /slang/bin/
4. Run the executable :
  - For Windows : slang.bat 
  - For Linux : bash slang
5. Run the Docker example flow:  run --f ../content/org/cloudslang/docker/containers/demo_dev_ops.sl  --cp ../content/  --inputs dockerHost=[*dockerHost*],dockerUsername=[*dockerHostUser*],dockerPassword=[*dockerHostPasword*],emailHost=[*emailHost*],emailPort=[*Emailport*],emailSender=[*EmailSender*],emailRecipient=[*EmailRecipient*]



#### Documentation :

All documentation is available on the [CloudSlang website](http://www.cloudslang.io/#/docs).

#### Get Involved

Contact us at [here](mailto:support@cloudslang.io).

#### Building and Testing from Source

###### Pre-Requisites:

1. maven version >= 3.0.3
2. Java JDK version >= 7

###### Steps:

1. ```git clone``` the source code
2. ```mvn clean install```
3. Run the CLI executable from score-lang-cli\target\cslang\bin 

### Another way of getting the CloudSlang command line interface.
###### cslang-cli
> The CloudSlang command line interface.

Install this globally and you'll have access to the `cslang` command anywhere on your system.

```shell
npm install -g cslang-cli
```

Now you can just use the `cslang` command anywhere
```shell
cslang
```

Refer to [CloudSlang](http://cloudslang.io) website for more information.

###### Pre-Requisites:
Node.js & Java installed.

cslang-cli page in the [NPM repository](https://www.npmjs.com/package/cslang-cli).
