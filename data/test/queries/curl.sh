#!/bin/sh
curl  -i -H 'Content-Type: application/json' --data-binary @$1 "http://localhost:8080/sp?tp-user=$2&tp-signature=$(./hash.sh $1 $3)"
