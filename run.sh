#!/usr/bin/env bash
JAR=$(find . -name ${NAME}*.jar|head -1)

java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}" | tee ${LOGFILE} 2>&1

