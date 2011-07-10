#!/bin/sh
if [ $# -ne 5 ]
then
   echo "Usage: hash.sh <URL> <file: jsonrequest> <RequestType> <user> <secret>"
   echo "Example: #/curl.sh komani.ath.cx:8081 simpleSP.json sp FooUser FooPassword"
   exit 1
fi

curl -i  -s -H 'Content-Type: application/json' --data-binary @$2 "$1/$3?tp-user=$4&tp-signature=$(./hash.sh $2 $5)"
