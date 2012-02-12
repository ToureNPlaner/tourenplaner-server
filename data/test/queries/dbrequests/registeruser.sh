#!/bin/sh

if [ $# -ne 3 ]
then
   echo "Usage: $0 <file.json> <URL> <HTTP/HTTPS>"
   echo "Example: #$0 register-user.json komani.ath.cx:8081 HTTPS"
   exit 1
fi

./curl.sh "$2" "$1" registeruser NOUSER NOPW "$3"
