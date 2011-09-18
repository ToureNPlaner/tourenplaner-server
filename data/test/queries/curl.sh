#!/bin/sh
if [ $# -ne 5 ]
then
   echo "Usage: curl.sh <URL> <file: jsonrequest> <RequestType> <user> <secret>"
   echo "Example: #/curl.sh komani.ath.cx:8081 simpleSP.json sp FooUser FooPassword"
   exit 1
fi

#curl -i  -s -H 'Content-Type: application/json' -u "$4:$5" --data-binary @$2 "$1/$3"

curl  -s -H 'Content-Type: application/json' -u "$4:$5" --data-binary @$2 "$1/$3"
