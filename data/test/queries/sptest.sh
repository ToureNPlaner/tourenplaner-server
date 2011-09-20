#!/bin/sh

if [ $# -ne 2 ] 
then
   echo "Usage: $0 <file.json> <URL> "
   echo "Example: #/curl.sh simpleSP.json komani.ath.cx:8081"
   exit 1
fi

./curl.sh "$2" "$1" algsp root@tourenplaner toureNPlaner
