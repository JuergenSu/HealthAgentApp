#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="$ROOT/build"
STAGE="${1:-$BUILD_DIR/coach-gateway}"
ZIP_FILE="${2:-$BUILD_DIR/health-agent-php-gateway.zip}"

if [[ ! -f "$ROOT/vendor/autoload.php" ]]; then
  echo "vendor/autoload.php fehlt. Vorher 'composer install --no-dev --classmap-authoritative' ausführen." >&2
  exit 1
fi

rm -rf "$STAGE" "$ZIP_FILE"
mkdir -p "$STAGE" "$(dirname "$ZIP_FILE")"

cp "$ROOT/index.php" "$STAGE/index.php"
cp "$ROOT/.htaccess" "$STAGE/.htaccess"
cp "$ROOT/.env.example" "$STAGE/.env.example"
cp "$ROOT/README.md" "$STAGE/README.md"
cp -R "$ROOT/public" "$STAGE/public"
cp -R "$ROOT/src" "$STAGE/src"
cp -R "$ROOT/config" "$STAGE/config"
cp -R "$ROOT/resources" "$STAGE/resources"
cp -R "$ROOT/vendor" "$STAGE/vendor"

# Never ship local secrets or test-only service configuration.
rm -f "$STAGE/.env" "$STAGE/.env.local" "$STAGE/config/config.local.php"
rm -f "$STAGE/config/services_test.yaml"

# Fail the build if development-only content accidentally enters the artifact.
for forbidden in tests bin .git composer.json composer.lock phpunit.xml phpunit.xml.dist .phpunit.cache; do
  if [[ -e "$STAGE/$forbidden" ]]; then
    echo "Nicht erlaubter Deployment-Inhalt gefunden: $forbidden" >&2
    exit 1
  fi
done

if [[ ! -f "$STAGE/vendor/autoload.php" || ! -f "$STAGE/resources/prompts/coach-v1.txt" ]]; then
  echo "Deployment ist unvollständig (vendor/autoload.php oder coach-v1 Prompt fehlt)." >&2
  exit 1
fi

(
  cd "$(dirname "$STAGE")"
  zip -qr "$ZIP_FILE" "$(basename "$STAGE")"
)

echo "Webspace-Artefakt erstellt: $ZIP_FILE"
