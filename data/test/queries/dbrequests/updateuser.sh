#!/bin/sh

if [ $# -ne 4 ]
then
   echo "Usage: $0 <PARAMETERS> <file.json> <URL> <HTTP/HTTPS>"
   echo "Example 1: #$0 "\" update-user.json komani.ath.cx:8081 HTTPS"
   echo "Example 2: #$0 "?id=36" update-user.json komani.ath.cx:8081 HTTPS"
   exit 1
fi

./curl.sh "$3" "$2" updateuser$1 testuser@tourenplaner.de testThisQuery "$4"