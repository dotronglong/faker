#!/usr/bin/env bash

port=3030
source=""
javaOptions=""
watch="false"

usage() {
  echo "usage: $0 [arguments]"
  echo ""
  echo "Arguments:"
  echo "  -s or --source        Specify source folder"
  echo "  -p or --port          Specify port to listen"
  echo "  -w or --watch         Watch source for changes"
  echo "  -j or --java-options  Customize java options"
  echo "  -h or --help          Show help"
}

while [ "$1" != "" ]; do
  case $1 in
    -w | --watch )          watch="true"
                            ;;
    -p | --port )           shift
                            port=$1
                            ;;
    -s | --source )         shift
                            source=$1
                            ;;
    -j | --java-options )   shift
                            javaOptions="$javaOptions $1"
                            ;;
    -h | --help )           usage
                            exit 0
                            ;;
    * )                     usage
                            exit 1
    esac
    shift
done

FAKER=$HOME/.faker/faker.jar
if [[ ! -f "${FAKER}" ]]; then
  echo "Unable to find any version of faker. Please try to install first."
  echo "Do you want to install latest version? (y/n)"
  read install
  if [[ "$install" == "y" ]]; then
    bash -c "$(curl -sL https://raw.githubusercontent.com/dotronglong/faker/master/install.sh)"
  else
    exit 0
  fi
fi

java -Dserver.port=$port \
     -Dfaker.source=$source \
     -Dfaker.watch=$watch \
     $javaOptions \
     -jar "$FAKER"
