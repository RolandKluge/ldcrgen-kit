#!/bin/bash

versionFile=“../version.txt“
rm -f $versionFile
echo "SVN Revision:" >> $versionFile
next_revision=$(echo $(svnversion) + 1 | sed -e 's/M//' | bc)
echo $next_revision >> $versionFile
