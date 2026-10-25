#!/usr/bin/env bash
# Puts newly built mod jars onto the test servers without resetting their worlds: the servers are recreated
# with the new jars, and the saved programs and operators are backed up before (contrib/itest/backup.sh).
#
# Usage: contrib/itest/deploy.sh [node ...]   (the jars must have been built before: ./gradlew build)
#   Without arguments, the running test servers get the new jars.
set -euo pipefail
cd "$(dirname "$0")/../.."

COMPOSE=(docker compose -f contrib/docker/compose.yaml)
# shellcheck source=contrib/itest/nodes.sh
source "$(dirname "$0")/nodes.sh"
NODES=("$@")
if [ ${#NODES[@]} -eq 0 ]; then
    mapfile -t NODES < <(running_nodes)
    [ ${#NODES[@]} -gt 0 ] || { echo "No test server is running; name the nodes, e.g. 26.3-fabric" >&2; exit 1; }
fi
# shellcheck source=contrib/itest/backup.sh
source "$(dirname "$0")/backup.sh"

services=()
for node in "${NODES[@]}"; do
    # Variants of a server (1.21.1-neoforge-plain) use the jar of their node
    build="$(cut -d- -f1,2 <<<"$node")"
    jar=""
    for candidate in versions/"$build"/build/libs/c0de-*.jar; do
        [[ "$candidate" == *-sources.jar ]] || jar="$candidate"
    done
    [ -n "$jar" ] && [ -f "$jar" ] || { echo "No mod jar for $build; run ./gradlew build first" >&2; exit 1; }
    backup_service "c0de-$node"
    mkdir -p "contrib/docker/mods/$build"
    rm -f "contrib/docker/mods/$build"/*.jar
    cp "$jar" "contrib/docker/mods/$build/"
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
