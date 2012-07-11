#!/bin/sh
#
#This script runs RAMADDA stand-alone 
#

RAMADDA_DIR=`dirname $0`

sh ${RAMADDA_DIR}/ramadda.sh $* 2>  ramadda.err > ramadda.out
