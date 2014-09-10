#!/bin/bash

# $1: name of the tested class

java -Xmx2G -Xms2G -ea -classpath ../lib/c4j.jar:../lib/junit-4.9b2.jar:../build/classes org.junit.runner.JUnitCore $1
 
