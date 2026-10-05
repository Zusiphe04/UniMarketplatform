#!/bin/sh
set -eu

UPLOAD_DIR="${UNIMARKET_MEDIA_UPLOAD_DIR:-/var/data/uploads}"
mkdir -p "$UPLOAD_DIR"
chown communitystore:communitystore "$UPLOAD_DIR"

exec su -s /bin/sh communitystore -c 'exec /opt/java/openjdk/bin/java ${JAVA_OPTS:-} -jar /app/community-store-api.jar'
