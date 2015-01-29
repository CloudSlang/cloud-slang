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

ADD id_rsa.pub /home/ubuntu/.ssh/authorized_keys

RUN chown -R ubuntu:ubuntu /home/ubuntu/.ssh
RUN chmod -R 700 /home/ubuntu/.ssh

RUN git clone git@github.com:openscore/score-language.git

RUN mvn package

ADD slang-content/ /score-lang-cli/target/slang/content/

WORKDIR score-lang-cli/target/slang/bin/

CMD ["sh","slang"]