#!/usr/bin/env bash
# Pull a screenshot from the TV. This Skyworth firmware prints driver noise to stdout during
# screencap, so `adb exec-out screencap -p` corrupts the PNG; write on-device and pull instead.
set -euo pipefail
OUT="${1:?usage: shot.sh <out.png>}"
adb shell screencap -p /data/local/tmp/shot.png >/dev/null 2>&1
adb pull -q /data/local/tmp/shot.png "$OUT"
sips -Z 960 "$OUT" >/dev/null
