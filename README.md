CloudSlang
==============
 
CloudSlang is a YAML based language for writing human-readable workflows for the CloudSlang Orchestration Engine (Score). This project includes the CLI to trigger flows.

[![Build Status](https://travis-ci.org/CloudSlang/cloud-slang.svg?branch=master)](https://travis-ci.org/CloudSlang/cloud-slang)
[![Coverage Status](https://coveralls.io/repos/CloudSlang/cloud-slang/badge.svg?branch=coveralls)](https://coveralls.io/r/CloudSlang/cloud-slang?branch=coveralls)

#### Getting started:

###### Pre-Requisite: Java JRE >= 7

1. Download the cslang zip from [here](https://github.com/CloudSlang/cloud-slang/releases/download/cloudslang-0.7.4/cslang-cli.zip). (For the latest snapshot, download from [here](https://github.com/CloudSlang/cloud-slang/releases/latest).)
2. Unzip it.
3. Go to the folder /cslang/bin/
4. Run the executable :
  - For Windows : cslang.bat 
  - For Linux : bash cslang
5. Run a simple example print text flow:  run --f ../../content/io/cloudslang/base/print/print_text.sl --i text=first_flow

#### Documentation :

All documentation is available on the [CloudSlang website](http://www.cloudslang.io/#/docs).

#### Get Involved

Contact us at [here](mailto:support@cloudslang.io).

#### Building and Testing from Source

###### Pre-Requisites:

1. Maven version >= 3.0.3
2. Java JDK version >= 7

###### Steps:

1. ```git clone``` the source code
2. ```mvn clean install```
3. Run the CLI executable from cloudslang-cli\target\cslang\bin 

### Another way of getting the score command line interface.
###### cslang-cli
> The score command line interface.

Install this globally and you'll have access to the `cslang` command anywhere on your system.

```shell
npm install -g cslang-cli
```

Now you can just use the `cslang` command anywhere
```shell
cslang
```

###### Pre-Requisites:
Node.js & Java installed.

cslang-cli page in the [NPM repository](https://www.npmjs.com/package/cslang-cli).
