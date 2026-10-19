#!/usr/bin/env bash
# Smoke test: starts the dedicated test servers with the built mod and checks that the mod is loaded
# and the servers respond to RCON commands.
#
# Usage: contrib/itest/smoke.sh [node ...]
#   Without arguments, all nodes of contrib/docker/compose.yaml are tested.
#   The jars must have been built before (./gradlew build).
#   KEEP_SERVERS=1 leaves the servers running after the test.
set -euo pipefail
cd "$(dirname "$0")/../.."

COMPOSE=(docker compose -f contrib/docker/compose.yaml)
TIMEOUT_SECONDS=${TIMEOUT_SECONDS:-600}
NODES=("$@")
if [ ${#NODES[@]} -eq 0 ]; then
    NODES=(1.21.1-fabric 1.21.1-neoforge)
fi

fail() {
    echo "::error title=Smoke test::$*"
    exit 1
}

# Writes the log lines about loading mods as annotations, so the cause of a failure is visible
# without access to the job log
diagnose() {
    "${COMPOSE[@]}" logs --no-log-prefix "$1" 2>&1 \
        | grep -iE "c0de|fabric-api|modrinth|loading [0-9]+ mods|incompatible|mods/|error" | head -15 \
        | while IFS= read -r line; do echo "::notice title=$1 log::$(cut -c1-400 <<<"$line")"; done
}

services=()
for node in "${NODES[@]}"; do
    jars=(versions/"$node"/build/libs/c0de-*.jar)
    jar=""
    for candidate in "${jars[@]}"; do
        [[ "$candidate" == *-sources.jar ]] || jar="$candidate"
    done
    [ -n "$jar" ] && [ -f "$jar" ] || fail "No mod jar for $node in versions/$node/build/libs. Run ./gradlew build first."
    mkdir -p "contrib/docker/mods/$node"
    rm -f "contrib/docker/mods/$node"/*.jar
    cp "$jar" "contrib/docker/mods/$node/"
    services+=("c0de-$node")
done

cleanup() {
    if [ "${KEEP_SERVERS:-0}" != 1 ]; then
        "${COMPOSE[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
    fi
}
trap cleanup EXIT

"${COMPOSE[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
"${COMPOSE[@]}" up -d "${services[@]}"

status=0
for i in "${!services[@]}"; do
    service="${services[$i]}"
    node="${NODES[$i]}"
    loader="${node#*-}"
    echo "==> $service"

    deadline=$((SECONDS + TIMEOUT_SECONDS))
    until "${COMPOSE[@]}" logs "$service" 2>/dev/null | grep -q 'Done ([0-9.]*s)! For help'; do
        if [ $SECONDS -ge $deadline ]; then
            "${COMPOSE[@]}" logs --tail 50 "$service" || true
            echo "::error title=Smoke test::$service did not start within $TIMEOUT_SECONDS seconds"
            diagnose "$service"
            status=1
            continue 2
        fi
        if ! "${COMPOSE[@]}" ps --status running --services | grep -qx "$service"; then
            "${COMPOSE[@]}" logs --tail 50 "$service" || true
            echo "::error title=Smoke test::$service stopped during startup"
            diagnose "$service"
            status=1
            continue 2
        fi
        sleep 5
    done
    echo "started"

    expected="c0de [0-9.]* for Minecraft [0-9.]* on ${loader^}"
    if [ "$loader" = neoforge ]; then
        expected="c0de [0-9.]* for Minecraft [0-9.]* on NeoForge"
    fi
    if "${COMPOSE[@]}" logs "$service" | grep -q "$expected"; then
        echo "mod loaded"
    else
        echo "::error title=Smoke test::The c0de mod was not loaded on $service"
        diagnose "$service"
        status=1
    fi

    if "${COMPOSE[@]}" exec -T "$service" rcon-cli list | grep -q "players online"; then
        echo "RCON responds"
    else
        echo "::error title=Smoke test::$service does not respond to RCON"
        status=1
    fi

    if "${COMPOSE[@]}" logs "$service" | grep -E "ERROR\].*c0de|Exception.*c0de" >/dev/null; then
        "${COMPOSE[@]}" logs "$service" | grep -E "ERROR\].*c0de|Exception.*c0de" | head -5
        echo "::error title=Smoke test::$service logged errors of the c0de mod"
        status=1
    fi
done

exit $status
