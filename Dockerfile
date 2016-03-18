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

RUN git clone https://github.com/CloudSlang/cloud-slang-content.git

RUN mvn package

RUN mkdir -p cloudslang-cli/target/cslang/content/

RUN cp -r cloud-slang-content/* cloudslang-cli/target/cslang/content/

WORKDIR cloudslang-cli/target/cslang/bin/

CMD ["sh","slang"]
