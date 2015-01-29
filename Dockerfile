############################################################
# Dockerfile to build score slang container image
# Based on Ubuntu with pre-installed java
############################################################

FROM java:openjdk-7-jdk

MAINTAINER Meir Wahnon

RUN apt-get update

RUN apt-get install maven -y

RUN mkdir -p /root/.ssh
ADD url_for_id_rsa /root/.ssh/id_rsa
RUN chmod 700 /root/.ssh/id_rsa
RUN echo "Host github.com\n\tStrictHostKeyChecking no\n" >> /root/.ssh/config

ADD . /app-src/

WORKDIR /app-src/

RUN git clone git@github.com:openscore/score-language.git

RUN mvn package

ADD slang-content/ /score-lang-cli/target/slang/content/

WORKDIR score-lang-cli/target/slang/bin/

CMD ["sh","slang"]