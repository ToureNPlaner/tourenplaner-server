#!/bin/bash
set -e

[[ $# -ne 5 ]] && echo 'USAGE: gpxspquery <URI> <latitude_source> <longitude_source> <latitude_destination> <latitude_destination>
example: gpxspquery localhost:8080 48.778611 9.179444 53.565278 10.001389' && exit 0

JSONTOGPX=${HOME}/sptogpx.jar
TMP=$(mktemp)
TMP2=$(mktemp)

echo "{
   \"points\" : [
                { \"lt\": $2, \"ln\": $3 },
                { \"lt\": $4, \"ln\": $5 }
        ]
}" > ${TMP}
./sptest.sh ${TMP} "$1" > ${TMP2} || (echo "sending json failed"; exit 1)
ERRID=$(grep -i errorid ${TMP2}) || true
[[ -n ${ERRID} ]] && (echo "computing json failed: $ERRID"; echo "sent json:"; cat ${TMP}; exit 1)
java -jar ${JSONTOGPX} ${TMP2} || (echo "sptogpx.jar failed"; exit 1)
rm ${TMP}
rm ${TMP2}
