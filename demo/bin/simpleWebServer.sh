#!/usr/bin/env bash
if [ -f "sim.pid" ]; then
kill -9 $(cat sim.pid)
fi

if [ "$1" = "run" ] ; then
java -jar ${2}.jar
fi

if [ "$1" = "start" ] ; then
nohup java -jar ${2}.jar > nohup.out 2>&1 &
echo "simpleServer is run"
fi
