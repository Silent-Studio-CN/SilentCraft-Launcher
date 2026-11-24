#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"

JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home}"
GRADLE="${GRADLE:-$HOME/tools/gradle-9.9.1/bin/gradle}"
export JAVA_HOME

echo "== HMCL build =="
echo "JAVA_HOME: $JAVA_HOME"
echo "Gradle:    $GRADLE"

"$GRADLE" :HMCL:shadowJar --no-daemon --console=plain

mkdir -p out
NAME="SilentCraft Launcher-v3.18-build$(date +%y%m%d%H%M%S).jar"
cp HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar "out/$NAME"
echo
echo "== Done =="
echo "out/$NAME"
