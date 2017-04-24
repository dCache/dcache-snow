#!/bin/bash

CP=`find . -type f -name "*.jar" | tr '\n' ':'`
CP=${CP}:`find /usr/share/dcache/classes -type f -name "*.jar" | tr '\n' ':'`

JAVA=`which java`

if [ -z ${JAVA} ] ; then
    echo "java not found"
    exit 1
fi

KEYPATH=$1

if [ -z ${KEYPATH} ] ; then
    echo "public key path argument undefined; using default: "
    echo "   /etc/dcache/admin/alarms_key.pub"
    $JAVA -cp ${CP} org.dcache.alarms.spi.PasswordManager
else
    $JAVA -cp ${CP} org.dcache.alarms.spi.PasswordManager -key=${KEYPATH}
fi
