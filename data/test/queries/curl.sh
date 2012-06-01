#!/bin/sh
#Copyright 2012 ToureNPlaner
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

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
    JSON_OPTION="-H 'Accept: application/x-jackson-smile' --data-binary @$2"
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
    curl -s $HTTPS_OPTION $JSON_OPTION -u "$USER_OPTION" "$1/$3"


else
    curl -H 'Accept: application/x-jackson-smile' -s $HTTPS_OPTION $JSON_OPTION "$1/$3" 

fi



