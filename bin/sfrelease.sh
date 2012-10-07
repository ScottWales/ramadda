
#Usage:
# sfrelease.sh <sf user id>
#

sfuser=${1}

if test -z ${sfuser} ; then 
#    echo "error: missing argument"
#    echo "usage: sfrelease.sh <sf user id>"
#    exit;
     sfuser=jmcwhirter
fi



#Make the release
#ant release

#scp the top level build products to SF

scp dist/repository.war dist/ramadda${RAMADDA_VERSION}.zip  dist/allplugins.jar dist/ramaddaclient.zip  ${sfuser},ramadda@frs.sourceforge.net:/home/frs/project/r/ra/ramadda/ramadda${RAMADDA_VERSION}

#now make the rest of the plugins
#ant otherplugins

#scp all of the plugins over to SF
#This will unfortunately prompt for a password
#scp dist/plugins/* ${sfuser},ramadda@frs.sourceforge.net:/home/frs/project/r/ra/ramadda/ramadda${RAMADDA_VERSION}/plugins


