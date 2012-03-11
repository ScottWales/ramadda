#!/bin/sh

RAMADDADIR=`dirname $0`
JAVA=java

echo 'RAMADDA: running at http://localhost:8080/repository'

${JAVA} -Xmx512m -XX:MaxPermSize=256m -Dfile.encoding=utf-8 -jar ${RAMADDADIR}/lib/ramadda.jar -port 8080 $*


##RAMADDA will create a home directory under ~/.ramadda to store content 
##and the database. To change the directory do:
##${JAVA} -Xmx512m -XX:MaxPermSize=256m -jar lib/ramadda.jar -port 8080 -Dramadda_home=/some/other/directory


#The default above is to use Java Derby as the database
#To run with mysql you do:
#${JAVA} -Xmx512m -XX:MaxPermSize=256m -jar ramadda.jar -Dramadda.db=mysql


#For more information see:
#http://facdev.unavco.org/repository/userguide/installing.html









