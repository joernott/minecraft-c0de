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

# Runs a command on a server and prints its answer
rcon() {
    local service="$1"
    shift
    "${COMPOSE[@]}" exec -T "$service" rcon-cli "$@" 2>&1 || true
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
    if grep -q "c0de data formats available" <<<"$log"; then
        echo "JSON and YAML available"
    else
        echo "::error title=Smoke test::The embedded YAML library is missing on $service"
        diagnose "$service"
        status=1
    fi

    if grep -q "players online" <<<"$(rcon "$service" list)"; then
        echo "RCON responds"
    else
        echo "::error title=Smoke test::$service does not respond to RCON"
        status=1
    fi

    if grep -q "c0de:settings" <<<"$(rcon "$service" datapack list enabled)"; then
        echo "settings pack enabled"
    else
        echo "::error title=Smoke test::The data pack c0de:settings is not enabled on $service"
        status=1
    fi

    # The floor of the c0de dimension: the chunk at the origin is loaded with a ticket first
    rcon "$service" "execute in c0de:c0de run forceload add -16 -16 15 15" >/dev/null
    floor_ok=1
    for check in "0 -4 0 c0de:black_c0de_bedrock" "1 -4 0 c0de:white_c0de_bedrock" \
        "1 -4 1 c0de:light_green_c0de_bedrock" "4 -4 7 c0de:cursor_c0de_bedrock" "-5 -4 -5 c0de:black_c0de_bedrock"; do
        passed=0
        for attempt in 1 2 3 4 5 6 7 8 9 10; do
            if grep -q "Test passed" <<<"$(rcon "$service" "execute in c0de:c0de if block $check")"; then
                passed=1
                break
            fi
            sleep 1
        done
        if [ $passed = 0 ]; then
            echo "::error title=Smoke test::The floor of the c0de dimension on $service is not c0de:${check##*c0de:} at ${check% c0de:*}"
            floor_ok=0
            status=1
        fi
    done
    [ $floor_ok = 1 ] && echo "c0de dimension floor ok"

    errors="$(grep -E "ERROR\].*c0de|Exception.*c0de" <<<"$log" || true)"
    if [ -n "$errors" ]; then
        head -5 <<<"$errors"
        echo "::error title=Smoke test::$service logged errors of the c0de mod"
        status=1
    fi
done

exit $status
