#!/usr/bin/env bash
NAME=${NAME:-pttg-ip-audit}

mv /app/truststore.jks /tmp
keytool -genkey -noprompt -alias ks1 -dname "CN=testCN, OU=testOU, O=testO, L=testL, S=testS, C=testC" -keystore /app/truststore.jks -storepass storepass -keypass keypass

JAR=$(find . -name ${NAME}*.jar|head -1)
java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"

