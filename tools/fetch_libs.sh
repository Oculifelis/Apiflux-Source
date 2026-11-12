#!/usr/bin/env bash
set -eu
cd "$(dirname "$0")/.."
mkdir -p libs/nested libs/runtime

get() {  # get <dest> <url>
  [ -s "$1" ] && return 0
  echo "fetching $1"
  curl -sSfL -o "$1.part" "$2" && mv "$1.part" "$1"
}

MR=https://cdn.modrinth.com/data
get libs/complicated_bees-1.21.1-3.6.2.jar "$MR/uhqEKIQS/versions/mgIcHfld/complicated_bees-1.21.1-3.6.2.jar"
get libs/create-1.21.1-6.0.6.jar "$MR/LNytGWDc/versions/tS7ygzAE/create-1.21.1-6.0.6.jar"
get libs/Jade-1.21.1-NeoForge-15.10.3.jar "$(curl -sSf 'https://api.modrinth.com/v2/project/jade/version?loaders=%5B%22neoforge%22%5D&game_versions=%5B%221.21.1%22%5D' \
  | python -c "import sys,json;print(next(f['url'] for v in json.load(sys.stdin) if '15.10.3' in v['version_number'] for f in v['files'] if f['primary']))")"
if [ -z "$(ls libs/nested/*.jar 2>/dev/null)" ]; then
  unzip -o -q -j libs/create-1.21.1-6.0.6.jar 'META-INF/jarjar/*.jar' -d libs/nested
fi

BJ=https://maven.blamejared.com
get libs/runtime/jei-1.21.1-neoforge-19.21.2.313.jar "$BJ/mezz/jei/jei-1.21.1-neoforge/19.21.2.313/jei-1.21.1-neoforge-19.21.2.313.jar"
echo "libs ready"
