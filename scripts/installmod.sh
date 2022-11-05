#!/usr/bin/env bash

PROJECT_DIR="$(pwd)"

if [ -z "$1" ]; then
  cd "$APPDATA"/.minecraft/mods/1.12.2 || exit
else
  cd "$1" || exit
fi

rm -f Konas-*.jar
cp "$PROJECT_DIR"/build/libs/Konas-*-release.jar .
