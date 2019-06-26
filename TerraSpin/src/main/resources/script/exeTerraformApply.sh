#!/bin/bash

planDir=$1
cd $planDir
echo In shell script path :: $planDir


terraform apply -no-color


