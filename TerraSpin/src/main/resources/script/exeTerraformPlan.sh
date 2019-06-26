#!/bin/bash

planDir=$1
cd $planDir
echo In shell script path :: $planDir

terraform init -no-color
terraform plan -no-color


