#!/usr/bin/env bash

version="2.0.3"

usage() {
  echo "usage: $0 [arguments]"
  echo ""
  echo "Arguments:"
  echo "  -v or --version       Specify faker's version"
  echo "  -h or --help          Show help"
}

while [ "$1" != "" ]; do
  case $1 in
    -v | --version )        shift
                            version=$1
                            ;;
    -h | --help )           usage
                            exit 0
                            ;;
    * )                     usage
                            exit 1
    esac
    shift
done

DIR_FAKER=~/.bin/faker
DIR_INSTALL=$DIR_FAKER/$version
FAKER=$DIR_INSTALL/faker.jar
FAKER_BIN=$DIR_FAKER/faker

if [[ ! -d "$DIR_INSTALL" ]]; then
  mkdir -p "$DIR_INSTALL"
fi

if [[ ! -f "$FAKER" ]]; then
  echo "Downloading faker $version ..."
  curl -o "${FAKER}" -SLO "https://github.com/dotronglong/faker/releases/download/v${version}/faker.jar"
fi

if [[ ! -f "$DIR_FAKER/faker.jar" ]]; then
  ln -s "$FAKER" $DIR_FAKER/faker.jar
fi

if [[ ! -f "${FAKER_BIN}" ]]; then
  curl -o "${FAKER_BIN}" -sSLO https://raw.githubusercontent.com/dotronglong/faker/master/faker.sh
  chmod +x "${FAKER_BIN}"
fi

check=$(echo $PATH | grep "${DIR_FAKER}")
if [[ -z "${check}" ]]; then
  echo "Please add ${DIR_FAKER} to PATH environment variable"
  echo "export \$PATH=\$PATH:${DIR_FAKER}"
fi

echo "Use version: $version"