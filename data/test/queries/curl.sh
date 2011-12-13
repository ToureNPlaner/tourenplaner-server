#!/bin/bash
if [ $# -ne 6 ]
then
   echo "Usage: curl.sh <URL> <file: jsonrequest / NOJSON> <RequestType> <user> <secret> <HTTP/HTTPS>"
   echo "Example: #/curl.sh komani.ath.cx:8081 simpleSP.json sp FooUser FooPassword HTTPS"
   exit 1
fi

#curl -i  -s -H 'Content-Type: application/json' -u "$4:$5" --data-binary @$2 "$1/$3"

if [ "NOJSON" == $2 ]
then

	if [ "HTTPS" == "$6" ]
	then
		curl  -s --insecure --ssl -u "$4:$5" "$1/$3"
	else
		curl  -s -u "$4:$5" "$1/$3"
	fi

else

	if [ "HTTPS" == "$6" ]
	then
		curl  -s --insecure --ssl -H 'Content-Type: application/json' -u "$4:$5" --data-binary @$2 "$1/$3"
	else
		curl  -s -H 'Content-Type: application/json' -u "$4:$5" --data-binary @$2 "$1/$3"
	fi

fi

echo "";
