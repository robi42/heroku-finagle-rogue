#!/bin/sh

export JAVA_OPTS="-Xmx1536M -Xss512k -XX:+UseCompressedOops"
export LIFT_PROD="-Drun.mode=production"

exec sh `dirname $0`/run.sh >> `dirname $0`/../logs/stdout.log 2>> `dirname $0`/../logs/errout.log &
echo $! > PROD_PID
