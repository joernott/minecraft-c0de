#!/usr/bin/env bash
# Helpers for the test servers of contrib/docker/compose.yaml, sourced by smoke.sh and deploy.sh (task 12.3).
# The caller defines COMPOSE, e.g. COMPOSE=(docker compose -f contrib/docker/compose.yaml).

# All nodes that have a test server, e.g. 1.21.1-fabric; variants like c0de-1.21.1-neoforge-plain are left out
all_nodes() {
    "${COMPOSE[@]}" config --services | sed -n 's/^c0de-\([^-]*-\(fabric\|neoforge\)\)$/\1/p' | sort -V
}

# The nodes whose test servers are running
running_nodes() {
    "${COMPOSE[@]}" ps --status running --services | sed -n 's/^c0de-\(.*\)$/\1/p' | sort -V
}

# Stops and removes the containers of the given services and the volumes with their worlds. Only these services
# are touched, so that the other test servers keep running with their worlds.
remove_services() {
    [ $# -gt 0 ] || return 0
    "${COMPOSE[@]}" rm --stop --force "$@" >/dev/null 2>&1 || true
    local project node version rest
    project="$("${COMPOSE[@]}" config | sed -n 's/^name: //p')"
    for service in "$@"; do
        # c0de-<version>-<loader>[-variant] has the volume <loader>[-variant]-<version>
        node="${service#c0de-}"
        version="${node%%-*}"
        rest="${node#*-}"
        docker volume rm "${project}_${rest}-${version}" >/dev/null 2>&1 || true
    done
}
