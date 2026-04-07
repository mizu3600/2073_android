#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"

if [[ -n "$MYSQL_PASSWORD" ]]; then
    mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" < "$PROJECT_ROOT/database/clicker_dump.sql"
else
    mysql -u "$MYSQL_USER" < "$PROJECT_ROOT/database/clicker_dump.sql"
fi

echo "MySQL schema imported into clicker_system."
