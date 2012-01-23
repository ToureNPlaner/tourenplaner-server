#!/bin/sh

case $# in

5)

coordinates=( $(cat rdm-shuffled-coordinates-germany.txt) )
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

JSONTMP="$JSONTMP
  ]
}"

TMP="$(mktemp)"
echo "$JSONTMP" > ${TMP}
echo "Running ./curl.sh ${1} ${TMP} $2 root@tourenplaner.de toureNPlaner $3" 1>&2
echo "$TMP:
$(cat ${TMP})" 1>&2
./curl.sh "$1" "${TMP}" $2 root@tourenplaner.de toureNPlaner "$3" && rm ${TMP} &  # don't add response time to script wait time
done
;;
*)
   echo "Usage: $0 <URL> <HTTP/HTTPS> <ALG_TYPE> <PAUSE> <POINTS_PER_REQUEST>"
   echo "Example:"
   echo "       $ $0 https://komani.ath.cx:8081 algsp HTTPS 0.5 3	(spams 2 semirandom requests with 3 points)"
   echo "       $ $0 komani.ath.cx:8080 HTTP algtsp 2 6 > log.txt"
   exit 1
 ;;
esac
