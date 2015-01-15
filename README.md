score-language (Slang)
==============

Slang is a YAML based language for writing human-readable workflows for score. This project includes the CLI to trigger flows.

[![Build Status](https://travis-ci.org/openscore/score-language.svg)](https://travis-ci.org/openscore/score-language)

#### Getting started:

1. Download the slang zip from [here](https://github.com/openscore/score-language/releases/download/slang-CLI-0.2/slang.zip).
2. Unzip it.
3. Go to the folder /slang/appassembler/bin/
4. Run the executable :
  - For Windows : slang.bat 
  - For Linux : bash slang
5. Run the Docker example flow:  run --f ../../docker-demo-flows/demo_dev_ops_flow.yaml  --inputs dockerHost=[*dockerHost*],dockerUsername=[*dockerHostUser*],dockerPassword=[*dockerHostPasword*],emailHost=[*emailHost*],emailPort=[*Emailport*],emailSender=[*EmailSender*],emailRecipient=[*EmailRecipient*]



#### Documentation :

All documentation is available on the [openscore website](http://www.openscore.io/#/docs).

#### Get Involved

Contact us at [here](mailto:support@openscore.io).

#### Building and Testing from Source

The score project uses Maven to build and test.

1. ```git clone``` the source code
2. ```mvn clean install```
3. Run the CLI executable from score-lang-cli\target\appassembler\bin 

