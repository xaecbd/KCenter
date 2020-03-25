#!/bin/bash
echo "PROJECT_BASE_DIR :"$PROJECT_BASE_DIR
#cd $APP_ROOT_DIR
cd $PROJECT_BASE_DIR
 

 
if [ ! -n "$START_JAR" ]; then
    START_JAR=(`ls|grep .jar$`)
    if [ "${#START_JAR[@]}"x != "1x" ]; then
        echo ${START_JAR[@]}" please set START_JAR by environment!"
        exit 2
    fi
fi
 
echo "***jar start!***"+$START_JAR
if [ ! -n "$JAVA_OPTS" ]; then
    java -jar $START_JAR $@
else
   java $JAVA_OPTS -jar $START_JAR $@
fi