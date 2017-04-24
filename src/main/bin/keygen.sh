#!/bin/bash

KEYPATH=$1

if [ -z ${KEYPATH} ] ; then
    KEYPATH=alarms_key
fi

# generate a 2048-bit RSA private key
openssl genrsa -out private_key.pem 2048

# convert private Key to PKCS#8 format (so Java can read it)
echo "Please hit return when asked for a password or the decryption will not work."
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out $KEYPATH

# output public key portion in DER format (so Java can read it)
openssl rsa -in private_key.pem -pubout -outform DER -out ${KEYPATH}.pub

rm -f private_key.pem

echo "private key is $KEYPATH"
echo "public key is ${KEYPATH}.pub"
echo ""
echo "Be sure the following plugin properties are properly set: "
echo "   snow.private-key (path to key file)"
echo "   snow.private-key.encryption (should be 'true')"

