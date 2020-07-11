FROM opsmx11/java:ubuntu16_java8

COPY TerraSpin/container/terraform  /usr/local/bin/terraform
#COPY /git  /usr/bin/git
RUN apt-get update && \
    apt-get install -y --no-install-recommends git
RUN groupadd -g 999 terraspin && \
    useradd -r -u 999 -g terraspin terraspin
RUN mkdir -p /home/terraspin
RUN chown terraspin /home/terraspin
USER terraspin
RUN mkdir -p /home/terraspin/opsmx/app /home/terraspin/opsmx/app/config /home/terraspin/opsmx/hal /home/terraspin/opsmx/kubeaccount
RUN touch /home/terraspin/opsmx/app/terraspin.log && \
    chmod 777 /home/terraspin/opsmx/app/terraspin.log
COPY TerraSpin/container/TerraSpin.jar  /home/terraspin/opsmx/app/TerraSpin.jar
COPY TerraSpin/container/run.sh  /usr/local/bin/run.sh
WORKDIR /home/terraspin
CMD run.sh
