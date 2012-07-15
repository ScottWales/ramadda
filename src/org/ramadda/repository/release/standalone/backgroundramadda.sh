#!/bin/sh
#
#This script runs RAMADDA and redirects stdout/stderr to ramadda.out
#

RAMADDA_DIR=`dirname $0`
sh ${RAMADDA_DIR}/ramadda.sh $* > ramadda.out 2>&1
