#!/bin/sh

# Set for production env via: `$ heroku config:add`
# LIFT_PROD="-Drun.mode=production"

exec java $JAVA_OPTS \
-cp build/application.jar:build/scala-library.jar:build/dependencies.jar \
-Dlogback.configurationFile=config/logback.xml \
$LIFT_PROD \
com.robert42.ft.RestServer \
$1
