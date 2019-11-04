FROM opsmx11/java:ubuntu16_java8
MAINTAINER OpsMx
COPY /terraform  /usr/local/bin/terraform
#COPY /git  /usr/bin/git
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y git
COPY /run.sh  /usr/local/bin/run.sh
RUN groupadd -g 999 terraspin && \
    useradd -r -u 999 -g terraspin terraspin
RUN mkdir -p /home/terraspin
RUN chown terraspin /home/terraspin
USER terraspin
RUN mkdir -p /home/terraspin/opsmx/app /home/terraspin/opsmx/app/config /home/terraspin/opsmx/hal /home/terraspin/opsmx/kubeaccount
RUN touch /home/terraspin/opsmx/app/terraspin.log && \
    chmod 777 /home/terraspin/opsmx/app/terraspin.log
WORKDIR /home/terraspin
COPY /terraform  /usr/local/bin/terraform
COPY /run.sh  /usr/local/bin/run.sh
COPY /TerraSpin.jar  /home/terraspin/opsmx/app/TerraSpin.jar
CMD run.sh
