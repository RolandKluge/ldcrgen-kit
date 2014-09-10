#!/bin/bash

cd $(dirname $0)

javadoc -private -author -version -d ../build/javadoc -link http://download.oracle.com/javase/6/docs/api/ -classpath ../lib/c4j.jar:../lib/junit-4.9b2.jar -sourcepath ../src:../test -subpackages edu
