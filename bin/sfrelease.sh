#Usage:
# sfrelease.sh <sf user id>
#

sfuser=${1}

if test -z ${sfuser} ; then 
     sfuser=jmcwhirter
fi


dest=/home/frs/project/r/ra/ramadda/ramadda${RAMADDA_VERSION}

#scp all of the plugins over to SF
scp ~/.ramadda/plugins/nlasplugin.jar dist/otherplugins/* ${sfuser},ramadda@frs.sourceforge.net:${dest}/plugins

#scp the top level build products to SF
scp dist/repository.war dist/ramadda${RAMADDA_VERSION}.zip  dist/allplugins.jar dist/ramaddaclient.zip  dist/pointtools.zip ${sfuser},ramadda@frs.sourceforge.net:${dest}




