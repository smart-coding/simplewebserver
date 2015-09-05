#!/usr/bin/env bash
mvn package
mvn install:install-file -Dfile=target/simplewebserver-1.2.1.jar -DgroupId=com.fzb -DartifactId=simplewebserver -Dversion=1.2.1 -Dpackaging=jar
mvn install:install-file -Dfile=pom.xml -DgroupId=com.fzb -DartifactId=simplewebserver -Dversion=1.2.1 -Dpackaging=pom
