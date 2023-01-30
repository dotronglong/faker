#!/usr/bin/env bash

version="latest"

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

# Set default latest version
if [[ "$version" == "latest" ]]; then
  version=$(curl --silent "https://api.github.com/repos/dotronglong/faker/releases/latest" |
                grep '"tag_name":' |
                sed -E 's/v//g' |
                sed -E 's/.*"([^"]+)".*/\1/')
fi

DIR_FAKER=.faker
DIR_ROOT=$HOME/$DIR_FAKER
DIR_INSTALL=$DIR_ROOT/$version
FAKER=$DIR_INSTALL/faker.jar
FAKER_BIN=$DIR_ROOT/fakerio

# Create folder if missing
if [[ ! -d "$DIR_INSTALL" ]]; then
  mkdir -p "$DIR_INSTALL"
fi

# Download if necessary
if [[ ! -f "$FAKER" ]]; then
  echo "Downloading faker $version ..."
  curl -o "${FAKER}" -#SLO "https://github.com/dotronglong/faker/releases/download/v${version}/faker.jar"
fi

# Update alias
rm -rf "$DIR_ROOT/faker.jar"
ln -s "$FAKER" "$DIR_ROOT/faker.jar"

if [[ ! -f "${FAKER_BIN}" ]]; then
  curl -o "${FAKER_BIN}" -sSLO https://raw.githubusercontent.com/dotronglong/faker/master/faker.sh
  chmod +x "${FAKER_BIN}"
fi

# Check for environment variable
check=$(echo $PATH | grep "${DIR_FAKER}")
if [[ -z "${check}" ]]; then
  echo "Please add \$HOME/${DIR_FAKER} to PATH environment variable"
  echo "export PATH=\$PATH:\$HOME/${DIR_FAKER}"
fi

# Success
echo "Use version: $version"
