#!/bin/sh

RAMADDA_DIR=`dirname $0`

#RAMADDA home directory
if [ -z "$RAMADDA_HOME" ]; then
    RAMADDA_HOME=${HOME}/.ramadda
fi

#Port RAMADDA runs on
if [ -z "$RAMADDA_PORT" ]; then
    RAMADDA_PORT=8080
fi

#Java settings
if [ -z "$JAVA" ]; then
    JAVA=java
fi

JAVA_MEMORY=1024m
JAVA_PERMGEN=256m



##See if there is one in the release dir
RAMADDA_ENV_FILE=${RAMADDA_DIR}/ramaddaenv.sh
if test  ${RAMADDA_ENV_FILE} ; then 
    . ${RAMADDA_ENV_FILE}
fi

##See if there is one in the cwd
if test  ramaddaenv.sh ; then 
    . ramaddaenv.sh
fi


${JAVA} -Xmx${JAVA_MEMORY} -XX:MaxPermSize=${JAVA_PERMGEN} -Dfile.encoding=utf-8 -jar ${RAMADDA_DIR}/lib/ramadda.jar -port ${RAMADDA_PORT} -Dramadda_home=${RAMADDA_HOME} $* 












