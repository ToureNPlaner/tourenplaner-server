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

5)

coordinates=( $(cat rdm-shuffled-coordinates-germany.txt) )
coordinateslength=${#coordinates[@]}

echo "looping over $coordinateslength coordinates" 1>&2

currentcoordinate=0
#cat rdm-shuffled-coordinates-germany.txt | while read i
#do
while :
do
sleep $4

JSONTMP="{
  \"points\" : ["

for j in $(seq $(( $5 - 1 )))
do
JSONTMP="$JSONTMP
    { \"lt\": $(echo ${coordinates[$currentcoordinate]} | cut -f 1 -d " "), \"ln\": $(echo ${coordinates[$currentcoordinate]} | cut -f 2 -d " ") },"
currentcoordinate=$(( $currentcoordinate + 1 ))
done
JSONTMP="$JSONTMP
    { \"lt\": $(echo ${coordinates[$currentcoordinate]} | cut -f 1 -d " "), \"ln\": $(echo ${coordinates[$currentcoordinate]} | cut -f 2 -d " ") }"

currentcoordinate=$(( $currentcoordinate + 1 ))

if [ $currentcoordinate -ge $coordinateslength ]
then
  currentcoordinate=0
fi

JSONTMP="$JSONTMP
  ]
}"

TMP="$(mktemp)"
echo "$JSONTMP" > ${TMP}
echo "Running ./curl.sh ${1} ${TMP} $3 root@tourenplaner.de toureNPlaner $2" 1>&2
echo "$TMP:
$(cat ${TMP})" 1>&2
./curl.sh "$1" "${TMP}" "$3" root@tourenplaner.de toureNPlaner "$2" && rm ${TMP} &  # don't add response time to script wait time
done
;;
*)
   echo "Usage: $0 <URL> <HTTP/HTTPS> <ALG_TYPE> <PAUSE> <POINTS_PER_REQUEST>
Example:
       $ $0 https://gerbera.informatik.uni-stuttgart.de:8081 HTTPS algsp 0.5 4 > randomtest.log	(spams 2 semirandom request with 4 points each)
       $ $0 komani.ath.cx:8080 HTTP algtsp 2 6 > log.txt" 1>&2
   exit 1
 ;;
esac
