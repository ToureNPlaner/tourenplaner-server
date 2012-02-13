#!/bin/sh

if [ $# -ne 3 ]
then
   echo "Usage: $0 <PARAMETERS> <URL> <HTTP/HTTPS>"
   echo "Example: #$0 \"?id=42\" komani.ath.cx:8081 HTTPS"
   exit 1
fi

./curl.sh "$2" "NOJSON" getresponse$1 root@tourenplaner.de toureNPlaner "$3"
