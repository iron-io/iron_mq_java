#!/bin/bash

if [ -z "$1" ]; then
  echo "must specify token as first param"
  exit 1
fi
if [ -z "$2" ]; then
  echo "must specify project ID as the second param"
  exit 2
fi

docker run -it --rm --name iron-maven-test -e IRON_TOKEN=$1 -e IRON_PROJECT_ID=$2 -v "$PWD":/usr/src/maven -w /usr/src/maven maven:3.2-jdk-7 sh -c 'mvn clean test'
