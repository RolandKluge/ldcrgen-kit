#!/bin/bash

rm -f version.txt
echo "SVN Revision:" >> version.txt
next_revision=$(echo $(svnversion) + 1 | sed -e 's/M//' | bc)
echo $next_revision >> version.txt
