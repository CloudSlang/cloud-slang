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

RUN git clone http://github.com/openscore/slang-content.git

RUN mvn package

RUN mkdir -p score-lang-cli/target/slang/content/

RUN cp -r slang-content/* score-lang-cli/target/slang/content

WORKDIR score-lang-cli/target/slang/bin/

CMD ["sh","slang"]
