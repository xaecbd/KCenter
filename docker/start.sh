#!/bin/sh
echo "PROJECT_BASE_DIR :"$PROJECT_BASE_DIR
#cd $APP_ROOT_DIR
cd $PROJECT_BASE_DIR



appName=`ls|grep .jar$`
echo start to run $appName

if [ -n "$JAVA_OPTIONS" ];then
	java $JAVA_OPTIONS -jar $appName   $@
else
    java -jar $appName   $@
fi