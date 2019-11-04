#!/bin/bash

nohup java -Dspring.config.location=/home/terraspin/opsmx/app/config/application.properties  -jar /home/terraspin/opsmx/app/TerraSpin.jar > /home/terraspin/opsmx/app/terraspin.log 2>&1 &
tail -f /home/ubuntu/logs/data_wrapper.log &
while :; do
  sleep 100
done
echo "started terraspin service..."
