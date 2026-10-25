#!/usr/bin/env bash
# Puts newly built mod jars onto the test servers without resetting their worlds: the servers are recreated
# with the new jars, and the saved programs and operators are backed up before (contrib/itest/backup.sh).
#
# Usage: contrib/itest/deploy.sh [node ...]   (the jars must have been built before: ./gradlew build)
set -euo pipefail
cd "$(dirname "$0")/../.."

COMPOSE=(docker compose -f contrib/docker/compose.yaml)
NODES=("$@")
if [ ${#NODES[@]} -eq 0 ]; then
    NODES=(1.21.1-fabric 1.21.1-neoforge)
fi
# shellcheck source=contrib/itest/backup.sh
source "$(dirname "$0")/backup.sh"

services=()
for node in "${NODES[@]}"; do
    jar=""
    for candidate in versions/"$node"/build/libs/c0de-*.jar; do
        [[ "$candidate" == *-sources.jar ]] || jar="$candidate"
    done
    [ -n "$jar" ] && [ -f "$jar" ] || { echo "No mod jar for $node; run ./gradlew build first" >&2; exit 1; }
    backup_service "c0de-$node"
    mkdir -p "contrib/docker/mods/$node"
    rm -f "contrib/docker/mods/$node"/*.jar
    cp "$jar" "contrib/docker/mods/$node/"
    services+=("c0de-$node")
done

# The worlds are kept in their volumes; only the containers are recreated with the new jars
"${COMPOSE[@]}" up -d --force-recreate "${services[@]}"
for service in "${services[@]}"; do
    for _ in $(seq 1 120); do
        "${COMPOSE[@]}" exec -T "$service" rcon-cli list >/dev/null 2>&1 && break
        sleep 5
    done
    echo "$service is running: $("${COMPOSE[@]}" exec -T "$service" rcon-cli list | head -1)"
done
