#!/bin/bash
if [ $# -ne 6 ]
then
   echo "Usage: curl.sh <URL> <file: jsonrequest / NOJSON> <RequestType> <user / NOUSER> <secret / NOPW> <HTTP/HTTPS>"
   echo "Example: #/curl.sh komani.ath.cx:8081 simpleSP.json sp FooUser FooPassword HTTPS"
   exit 1
fi


if [ "NOJSON" == $2 ]
then
    JSON_OPTION=""
else
    JSON_OPTION="-H 'Accept: application/json' --data-binary @$2"
fi

if [ "HTTPS" == "$6" ]
then
    HTTPS_OPTION="--insecure --ssl"
else
	HTTPS_OPTION=""
fi

USER_AUTH=YES

if [ "NOUSER" == "$4" ]
then
    if [ "NOPW" == "$5" ]
    then
        USER_OPTION=""
        USER_AUTH=NO
    else
        USER_OPTION="$5"
    fi
else
	if [ "NOPW" == "$5" ]
    then
        USER_OPTION="$4"
    else
        USER_OPTION="$4:$5"
    fi
fi

HTTP_CODE='\n\nHttpStatusCode:\t%{http_code}\n'

if [ "YES" == "$USER_AUTH" ]
then
    curl -s $HTTPS_OPTION $JSON_OPTION -u "$USER_OPTION" "$1/$3" -w $HTTP_CODE

    echo "";
    echo "END OF curl -sL" $HTTPS_OPTION $JSON_OPTION -u "$USER_OPTION" \"$1/$3\" -w $HTTP_CODE
    echo "";

else
    curl -s $HTTPS_OPTION $JSON_OPTION "$1/$3" -w $HTTP_CODE

    echo "";
    echo "END OF curl -sL" $HTTPS_OPTION $JSON_OPTION \"$1/$3\" -w $HTTP_CODE
    echo "";
fi



