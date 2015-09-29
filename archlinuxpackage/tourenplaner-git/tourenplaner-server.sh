#!/bin/bash
. /etc/profile
sudo -E -u http "${JAVA_HOME}/bin/java" -Xmx8g -Xincgc -jar '/usr/share/java/tourenplaner/tourenplaner-server.jar' -c /etc/tourenplaner.conf -f dump
