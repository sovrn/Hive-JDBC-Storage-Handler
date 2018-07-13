#!/usr/bin/env bash
JAVA_HOME=$(/usr/libexec/java_home -v 1.8) mvn clean package -Phadoop-2 -DskipTest=true
