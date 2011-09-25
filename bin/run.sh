#!/bin/sh

nohup java $JAVA_OPTS \
-cp `dirname $0`/../build/application.jar:`dirname $0`/../build/scala-library.jar:`dirname $0`/../build/dependencies.jar \
-Dlogback.configurationFile=`dirname $0`/../config/logback.xml \
$LIFT_PROD \
com.robert42.ft.RestServer \
$1
