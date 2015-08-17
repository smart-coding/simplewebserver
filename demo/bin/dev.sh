#!/usr/bin/env bash
if [ -f "sim.pid" ]; then
kill -9 $(cat sim.pid)
fi

cd ..
mvn clean compile assembly:single
mv ${1}/target/*.jar ${1}/${1}.jar
java -jar ${1}/${1}.jar
