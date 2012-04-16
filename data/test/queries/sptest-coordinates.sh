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


case $# in

2)
sed '$!N;s/\n/ /' rdm-shuffled-coordinates-germany.txt | while read i
do
LATSRC=$(echo $i | cut -f 1 -d " ")
LONSRC=$(echo $i | cut -f 2 -d " ")

LATTRGT=$(echo $i | cut -f 3 -d " ")
LONTRGT=$(echo $i | cut -f 4 -d " ")

TMP=$(mktemp)
echo "{
        \"points\" : [
                { \"lt\": $LATSRC, \"ln\": $LONSRC },
                { \"lt\": $LATTRGT, \"ln\": $LONTRGT }
        ]
}" > ${TMP}
echo "Running ./curl.sh ${1} $(cat ${TMP}) algsp root@tourenplaner.de toureNPlaner $2"
./curl.sh "$1" "${TMP}" algsp root@tourenplaner.de toureNPlaner "$2"
rm ${TMP}
done
	;;

3)
sed '$!N;s/\n/ /' rdm-shuffled-coordinates-germany.txt | while read i
do
sleep $3
LATSRC=$(echo $i | cut -f 1 -d " ")
LONSRC=$(echo $i | cut -f 2 -d " ")

LATTRGT=$(echo $i | cut -f 3 -d " ")
LONTRGT=$(echo $i | cut -f 4 -d " ")

TMP=$(mktemp)
echo "{
        \"points\" : [
                { \"lt\": $LATSRC, \"ln\": $LONSRC },
                { \"lt\": $LATTRGT, \"ln\": $LONTRGT }
        ]
}" > ${TMP}
echo "Running ./curl.sh ${1} $(cat ${TMP}) algsp root@tourenplaner.de toureNPlaner $2"
./curl.sh "$1" "${TMP}" algsp root@tourenplaner.de toureNPlaner "$2"
rm ${TMP}
done
	;;

6)
TMP=$(mktemp)
echo "{
	\"points\" : [
		{ \"lt\": $3, \"ln\": $3 },
		{ \"lt\": $5, \"ln\": $6 }
	]
}" > ${TMP}
#echo "Running ./curl.sh ${1} ${TMP} algsp root@tourenplaner.de toureNPlaner $2"
./curl.sh "$1" "${TMP}" algsp root@tourenplaner.de toureNPlaner "$2"
rm ${TMP}
;;

*)
   echo "Usage: $0 <URL> <HTTP/HTTPS> [<LAT SRC> <LON SRC> <LAT TRGT> <LON TRGT> | <PAUSE>]"
   echo "Example:"
   echo "       $ $0 https://komani.ath.cx:8081 HTTPS		(spams semirandom requests)"
   echo "       $ $0 komani.ath.cx:8080 HTTP 0.5	(spams 2 semirandom requests per second)"
   echo "       $ $0 komani.ath.cx:8080 HTTP 525282565 7376016 479120705 107543753"
   exit 1
 ;;
esac
