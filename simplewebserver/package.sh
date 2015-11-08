#!/usr/bin/env bash
version="1.2.2"
mvn clean package
mvn install:install-file -Dfile=target/simplewebserver-$version.jar -DgroupId=com.fzb -DartifactId=simplewebserver -Dversion=$version -Dpackaging=jar
mvn install:install-file -Dfile=pom.xml -DgroupId=com.fzb -DartifactId=simplewebserver -Dversion=$version -Dpackaging=pom