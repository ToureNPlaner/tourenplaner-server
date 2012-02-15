#!/bin/sh

if [ $# -ne 3 ]
then
   echo "Usage: $0 <PARAMETERS> <URL> <HTTP/HTTPS>"
   echo "Example: #$0 \"?limit=5&offset=10\" komani.ath.cx:8081 HTTPS"
   exit 1
fi

./curl.sh "$2" "NOJSON" listusers$1 root@tourenplaner.de toureNPlaner "$3"