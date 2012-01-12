#!/bin/sh

if [ $# -ne 3 ] 
then
   echo "Usage: $0 <file.json> <URL> <HTTP/HTTPS>"
   echo "Example: #/curl.sh simpleSP.json komani.ath.cx:8081 HTTPS"
   exit 1
fi

./curl.sh "$2" "$1" algcsp root@tourenplaner.de toureNPlaner "$3"
