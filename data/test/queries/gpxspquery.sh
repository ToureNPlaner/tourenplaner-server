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
