#!/bin/sh

[[ $# -ne 1 ]] && echo -e "Usage:\n\t$0 jar|test" && exit 1

case $1 in
jar)
	mvn compile assembly:single
	;;
test)
	mvn test
	;;
*)
	echo "$1 not supported by this script"
	;;
esac
