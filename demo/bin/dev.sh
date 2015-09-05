#!/usr/bin/env bash
if [ -f "sim.pid" ]; then
kill -9 $(cat sim.pid)
fi


mvn clean compile assembly:single
mv target/*.jar ${1}.jar
java -jar ${1}.jar
