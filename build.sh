#!/bin/bash

[[ $# -le 1 ]] && echo -e "Usage:\n\t$0 [jar|test] [arguments to mvn]" && exit 1

case $1 in
  jar)
    mvn compile assembly:single ${@:2}
    exit $?
    ;;
  test)
    mvn test ${@:2}
    exit $?
    ;;
  *)
    echo "$1 not supported by this script"
    ;;
esac
