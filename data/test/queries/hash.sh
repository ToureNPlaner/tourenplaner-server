#!/bin/sh
#based on http://broadcast.oreilly.com/2009/12/principles-for-standardized-rest-authentication.html

BODYFILE="$1"
SECRET="$2"
BODYHASH=$(sha1sum "$BODYFILE" | egrep -o -e '[0123456789abcdef]*')

echo "$BODYHASH:$SECRET" | sha1sum - | egrep -o -e '[0123456789abcdef]*'
