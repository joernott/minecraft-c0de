#!/usr/bin/env bash
# Backup and restore of what players keep on the test servers outside of the worlds: the program files of
# earlier versions of the c0de disk (world/c0de/programs; since task 11.11.9 programs are kept on the disks and in
# the folder c0de of the clients) and the operators (ops.json). Sourced by smoke.sh and deploy.sh, which recreate servers.
#
# Needs COMPOSE (the docker compose command as an array). Backups go to contrib/docker/backup/<service>/.

BACKUP_ROOT=${BACKUP_ROOT:-contrib/docker/backup}

# Copies the saved programs and the operators of a running service into the backup folder
backup_service() {
    local service="$1" target="$BACKUP_ROOT/$1" container
    container="$("${COMPOSE[@]}" ps -q "$service" 2>/dev/null || true)"
    [ -n "$container" ] || return 0
    mkdir -p "$target"
    if docker cp "$container:/data/world/c0de/programs/." "$target/programs" >/dev/null 2>&1; then
        echo "backed up $(find "$target/programs" -name '*.yaml' | wc -l) programs of $service"
    fi
    docker cp "$container:/data/ops.json" "$target/ops.json" >/dev/null 2>&1 && echo "backed up the operators of $service"
    return 0
}

# Copies the backup into a running service; existing programs with the same name are overwritten.
# The operators take effect after the next restart of the service.
restore_service() {
    local service="$1" source="$BACKUP_ROOT/$1" container
    container="$("${COMPOSE[@]}" ps -q "$service" 2>/dev/null || true)"
    [ -n "$container" ] && [ -d "$source" ] || return 0
    if [ -d "$source/programs" ]; then
        "${COMPOSE[@]}" exec -T "$service" mkdir -p /data/world/c0de/programs
        docker cp "$source/programs/." "$container:/data/world/c0de/programs" >/dev/null
        "${COMPOSE[@]}" exec -T -u root "$service" chown -R 1000:1000 /data/world/c0de >/dev/null 2>&1 || true
        echo "restored $(find "$source/programs" -name '*.yaml' | wc -l) programs of $service"
    fi
    if [ -f "$source/ops.json" ]; then
        docker cp "$source/ops.json" "$container:/data/ops.json" >/dev/null
        "${COMPOSE[@]}" exec -T -u root "$service" chown 1000:1000 /data/ops.json >/dev/null 2>&1 || true
        echo "restored the operators of $service"
    fi
}
