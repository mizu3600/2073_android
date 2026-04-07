#!/usr/bin/env bash
set -euo pipefail

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export TOMCAT_HOME="${TOMCAT_HOME:-/opt/homebrew/opt/tomcat}"
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:/opt/homebrew/bin:$TOMCAT_HOME/bin:$PATH"

SCRIPT_NAME="${BASH_SOURCE[0]:-$0}"

if [[ "$SCRIPT_NAME" == "$0" ]]; then
    echo "JAVA_HOME=$JAVA_HOME"
    echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
    echo "TOMCAT_HOME=$TOMCAT_HOME"
fi
