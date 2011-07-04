#!/bin/sh
#based on http://broadcast.oreilly.com/2009/12/principles-for-standardized-rest-authentication.html

if [ $# -ne 2 ]
then
   echo "Usage: hash.sh <file: jsonrequest> <secret>"
   exit 1
fi


BODYFILE="$1"
SECRET="$2"
BODYHASH=$(sha1sum "$BODYFILE" | egrep -o -e '[0123456789abcdef]*')

echo "$BODYHASH:$SECRET" | sha1sum - | egrep -o -e '[0123456789abcdef]*'
