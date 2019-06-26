#!/bin/bash

planDir=$1
cd $planDir
echo In shell script path :: $planDir

terraform destroy -no-color


