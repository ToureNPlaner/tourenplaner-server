#!/bin/sh

if [ $# -ne 4 ]
then
   echo "Usage: $0 <USER> <PW> <URL> <HTTP/HTTPS>"
   echo "Example: #$0 test@test.de testpw komani.ath.cx:8081 HTTPS"
   exit 1
fi

./curl.sh "$3" "NOJSON" authuser "$1" "$2" "$4"
