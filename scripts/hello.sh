#!/bin/bash

ARG1=$1
ARG2=$2

if [ -z $ARG1 ] || [ -z $ARG2 ]; then
	echo "USAGE: hello2.sh ARG1 ARG2"
	exit 1
fi

echo "Hello $ARG1 $ARG2"

exit 0

