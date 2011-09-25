#!/bin/sh

export JAVA_OPTS="-Xmx1536M -Xss512k -XX:+UseCompressedOops"
export LIFT_PROD="-Drun.mode=production"

exec sh `dirname $0`/run.sh > server.log &
