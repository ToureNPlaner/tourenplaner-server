#!/bin/sh

if [ $# -ne 3 ]
then
   echo "Usage: $0 <PARAMETERS> <URL> <HTTP/HTTPS>"
   echo "Example 1: #$0 \"?id=36\" komani.ath.cx:8081 HTTPS"
   echo "Example 2: #$0 \"\" komani.ath.cx:8081 HTTPS"
   exit 1
fi

./curl.sh "$2" "NOJSON" getuser$1 root@tourenplaner.de toureNPlaner "$3"
