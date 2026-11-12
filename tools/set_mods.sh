#!/usr/bin/env bash
set -eu
cd "$(dirname "$0")/.."
RUN=$1; COMBO=$2
mkdir -p "$RUN/mods"
rm -f "$RUN"/mods/*.jar
cp libs/create-1.21.1-6.0.6.jar "$RUN/mods/"
case "$COMBO" in
  full*) cp libs/complicated_bees-1.21.1-3.6.2.jar "$RUN/mods/" ;;
esac
case "$COMBO" in
  *+client) cp libs/runtime/jei-*.jar libs/Jade-*.jar "$RUN/mods/" ;;
esac
ls "$RUN/mods"
