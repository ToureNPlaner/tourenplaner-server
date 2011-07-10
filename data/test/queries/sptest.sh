#!/bin/sh

if [ $# -ne 1 ] 
then
   echo "Usage: hash.sh <URL> "
   echo "Example: #/curl.sh komani.ath.cx:8081"
   exit 1
fi

./curl.sh $1 simpleSP.json sp FooUser FooPassword
