#!/bin/bash
mv /home/terraform/exeTerraformCmd.sh /tmp
nohup java  -Dserver.port=${PORT:-8080} -jar /home/terraform/terraApp.jar > /home/terraform/logs/terraform_service.log 2>&1 &
sleep 10
tail -f /home/terraform/logs/terraform_service.log