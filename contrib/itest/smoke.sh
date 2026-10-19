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

# The log of a service. It is always read completely before it is searched: with pipefail,
# `logs | grep -q` fails when grep exits at the first match and the log command gets SIGPIPE.
logs() {
    "${COMPOSE[@]}" logs --no-log-prefix "$1" 2>&1 || true
}

# Writes the log lines about loading mods as annotations, so the cause of a failure is visible
# without access to the job log
diagnose() {
    local log
    log="$(logs "$1")"
    grep -iE "c0de|fabric-api|modrinth|loading [0-9]+ mods|incompatible|mods/|error" <<<"$log" | head -15 \
        | while IFS= read -r line; do echo "::notice title=$1 log::$(cut -c1-400 <<<"$line")"; done || true
}

services=()
for node in "${NODES[@]}"; do
    jar=""
    for candidate in versions/"$node"/build/libs/c0de-*.jar; do
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
    echo "==> $service"

    deadline=$((SECONDS + TIMEOUT_SECONDS))
    until grep -q 'Done ([0-9.]*s)! For help' <<<"$(logs "$service")"; do
        if [ $SECONDS -ge $deadline ]; then
            echo "::error title=Smoke test::$service did not start within $TIMEOUT_SECONDS seconds"
            diagnose "$service"
            status=1
            continue 2
        fi
        if ! grep -qx "$service" <<<"$("${COMPOSE[@]}" ps --status running --services)"; then
            echo "::error title=Smoke test::$service stopped during startup"
            diagnose "$service"
            status=1
            continue 2
        fi
        sleep 5
    done
    echo "started"

    case "${node#*-}" in
        fabric) loader=Fabric ;;
        neoforge) loader=NeoForge ;;
        *) fail "Unknown loader in node $node" ;;
    esac
    log="$(logs "$service")"
    if grep -q "c0de [0-9.]* for Minecraft [0-9.]* on $loader" <<<"$log"; then
        echo "mod loaded"
    else
        echo "::error title=Smoke test::The c0de mod was not loaded on $service"
        diagnose "$service"
        status=1
    fi

    if grep -q "players online" <<<"$("${COMPOSE[@]}" exec -T "$service" rcon-cli list 2>&1 || true)"; then
        echo "RCON responds"
    else
        echo "::error title=Smoke test::$service does not respond to RCON"
        status=1
    fi

    errors="$(grep -E "ERROR\].*c0de|Exception.*c0de" <<<"$log" || true)"
    if [ -n "$errors" ]; then
        head -5 <<<"$errors"
        echo "::error title=Smoke test::$service logged errors of the c0de mod"
        status=1
    fi
done

exit $status
