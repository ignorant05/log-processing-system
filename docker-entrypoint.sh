#!/bin/sh

if [ $# -eq 0 ]; then
  echo ""
  echo "klog-cli container is running"
  echo "Available commands: topic|generate|consume|help"
  echo "Usage: docker compose exec klog-cli java -jar klog.jar [command] [flags & values]"
  echo ""
  exec tail -f /dev/null
fi

if [ "${1#-}" != "$1" ] || echo "$1" | grep -qE '^(topic|generate|consume|help)$'; then
  exec java -jar klog.jar "$@"
fi

exec "$@"
