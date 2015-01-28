score-language (Slang)
==============

Slang is a YAML based language for writing human-readable workflows for score. This project includes the CLI to trigger flows.

[![Build Status](https://travis-ci.org/openscore/score-language.svg)](https://travis-ci.org/openscore/score-language)

#### Getting started:

###### Pre-Requisite: Java JRE >= 7

1. Download the slang zip from [here](https://github.com/openscore/score-language/releases/download/slang-CLI-v0.2.1/score-lang-cli.zip).
2. Unzip it.
3. Go to the folder /slang/appassembler/bin/
4. Run the executable :
  - For Windows : slang.bat 
  - For Linux : bash slang
5. Run the Docker example flow:  run --f ../content/org/openscore/slang/docker/containers/demo_dev_ops.sl  --cp ../content/  --inputs dockerHost=[*dockerHost*],dockerUsername=[*dockerHostUser*],dockerPassword=[*dockerHostPasword*],emailHost=[*emailHost*],emailPort=[*Emailport*],emailSender=[*EmailSender*],emailRecipient=[*EmailRecipient*]



#### Documentation :

All documentation is available on the [openscore website](http://www.openscore.io/#/docs).

#### Get Involved

Contact us at [here](mailto:support@openscore.io).

#### Building and Testing from Source

###### Pre-Requisites:

1. maven version >= 3.0.3
2. Java JDK version >= 7

###### Steps:

1. ```git clone``` the source code
2. ```mvn clean install```
3. Run the CLI executable from score-lang-cli\target\slang\bin 

