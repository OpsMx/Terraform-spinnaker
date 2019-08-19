#!/bin/bash

UserPort=$1
DefaultPort=8090
CurrentDir="$(pwd)"

echo current directory :: $CurrentDir
echo user port :: $UserPort
echo default port :: $DefaultPort

IsRunning="$(ps -ef | grep TerraSpin.jar | wc -l)"	 

if [ $IsRunning -gt 1 ]
then
    echo TerraSpin service is already running.
else

		wget -O TerraSpin.jar "https://github.com/OpsMx/Terraform-spinnaker/blob/master/TerraSpin/artifacts/TerraSpin.jar"

		if [ -z "$UserPort" ]
    then
        nohup java -Dserver.port=8090 -jar $CurrentDir/TerraSpin.jar 2>&1 &
    else
        nohup java -Dserver.port=$UserPort -jar $CurrentDir/TerraSpin.jar 2>&1 &
    fi
fi
