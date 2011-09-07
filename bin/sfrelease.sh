
#Usage:
# sfrelease.sh <sf user id>
#

#Make the release
ant release

#scp the top level build products to SF

scp dist/repository.war dist/ramadda1.3.zip  dist/allplugins.zip dist/geodataplugins.zip dist/repositoryclient.zip dist/repositoryclient.jar ${1},ramadda@frs.sourceforge.net:/home/frs/project/r/ra/ramadda/ramadda1.3

#now make the rest of the plugins
ant otherplugins

#scp all of the plugins over to SF
#This will unfortunately prompt for a password
scp dist/plugins/* ${1},ramadda@frs.sourceforge.net:/home/frs/project/r/ra/ramadda/ramadda1.3/plugins


