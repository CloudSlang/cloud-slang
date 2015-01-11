############################################################
# Dockerfile to build score slang container image
# Based on Ubuntu with pre-installed java
############################################################

FROM java:openjdk-7-jdk

MAINTAINER Meir Wahnon

RUN apt-get update

RUN apt-get install maven -y

ADD . /app-src/

WORKDIR /app-src/

RUN mvn package

CMD ["bash","slang","score-lang-cli/target/appassembler/bin"]