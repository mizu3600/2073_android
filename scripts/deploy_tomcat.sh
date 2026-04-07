#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

source "$SCRIPT_DIR/env.sh"

bash "$SCRIPT_DIR/build_server.sh"

WEBAPPS_DIR="$TOMCAT_HOME/libexec/webapps"
if [[ ! -d "$WEBAPPS_DIR" ]]; then
    WEBAPPS_DIR="$TOMCAT_HOME/webapps"
fi

rm -rf "$WEBAPPS_DIR/clicker" "$WEBAPPS_DIR/clicker.war"
cp "$PROJECT_ROOT/server/target/clicker.war" "$WEBAPPS_DIR/clicker.war"

if command -v brew >/dev/null 2>&1; then
    brew services restart tomcat
else
    "$TOMCAT_HOME/bin/catalina" stop >/dev/null 2>&1 || true
    "$TOMCAT_HOME/bin/catalina" start
fi

echo "Tomcat deployed."
echo "Open: http://localhost:8080/clicker/display"
