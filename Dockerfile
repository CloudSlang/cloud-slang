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

RUN git clone git@github.com:openscore/score-language.git

ADD slang-content/ /score-lang-cli/target/slang/content/

WORKDIR score-lang-cli/target/slang/bin/

CMD ["sh","slang"]