#!/bin/bash

java -Xmx2G -Xms2G -cp $(dirname $0)/build/classes edu.kit.iti.ldcrgen.Main $@
