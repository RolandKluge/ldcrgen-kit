#!/bin/bash
java -classpath build/classes:lib/randoop.1.3.2.jar randoop.main.Main gentests --junit-output-dir=build/randoop/fy --testclass=edu.kit.iti.lcdrgen.data_structures.FisherYatesShuffle --timelimit=180 --omitmethods="(.*propose.*)|(.*select\(\))|(.*delete\(\))"
javac -classpath lib/junit-4.9b2.jar:lib/c4j.jar:build/classes  build/randoop/fy/RandoopTest*.java
java -classpath build/randoop/fy:lib/junit-4.9b2.jar:build/classes RandoopTest
 
