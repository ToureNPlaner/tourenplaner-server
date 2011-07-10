#!/bin/sh
if [ $# -ne 4 ]
then
   echo "Usage: hash.sh <URL> <file: jsonrequest> <user> <secret>"
   echo "Example: #/curl.sh komani.ath.cx:8081 simpleSP.json FooUser FooPassword"
   exit 1
fi

curl -i  -s -H 'Content-Type: application/json' --data-binary @$2 "$1/sp?tp-user=$3&tp-signature=$(./hash.sh $2 $4)"
