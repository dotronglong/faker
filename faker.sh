#!/usr/bin/env sh

port=3030
version="1.0.0"
source=""
javaOptions=""

usage() {
  echo "usage: $0 [arguments]"
  echo ""
  echo "Arguments:"
  echo "  -v or --version       Specify faker's version"
  echo "  -s or --source        Specify source folder"
  echo "  -p or --port          Specify port to listen"
  echo "  -j or --java-options  Customize java options"
  echo "  -h or --help          Show help"
}

while [ "$1" != "" ]; do
  case $1 in
    -v | --version )        shift
                            version=$1
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

faker=$PWD/faker.jar
if [[ ! -f "$faker" ]]; then
    echo "Downloading faker ..."
    curl -SLO https://github.com/dotronglong/faker/releases/download/v$version/faker.jar
fi

java -jar $faker -Dserver.port=$port -Dfaker.source=$source $javaOptions