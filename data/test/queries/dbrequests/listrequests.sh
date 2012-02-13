#!/bin/sh

if [ $# -ne 3 ]
then
   echo "Usage: $0 <PARAMETERS> <URL> <HTTP/HTTPS>"
   echo "Example 1: #$0 \"?id=all&limit=5&offset=10&details=nojson\" komani.ath.cx:8081 HTTPS"
   echo "Example 2: #$0 \"?id=36&limit=5&offset=10\" komani.ath.cx:8081 HTTPS"
   echo "Example 3: #$0 \"?limit=5&offset=10\" komani.ath.cx:8081 HTTPS"
   exit 1
fi

./curl.sh "$2" "NOJSON" listrequests$1 root@tourenplaner.de toureNPlaner "$3"
