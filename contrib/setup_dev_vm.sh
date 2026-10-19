#!/usr/bin/env bash
# Provisions the development VM (Debian 13) for the c0de mod. It defines the environment:
# a tool the tests need that this script doesn't install is a defect of the script.
#
# The script is idempotent and can be run again at any time, e.g. after a change of the pinned versions.
# It must run as the development user (doener) with passwordless sudo:
#
#   ssh doener@<vm> 'bash -s' < contrib/setup_dev_vm.sh
#
# See docs/development_environment.md.
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive

REPOSITORY_URL="https://github.com/joernott/minecraft-c0de.git"
CLONE_DIR="$HOME/src/minecraft-c0de"
SHARED_DIR="$HOME/minecraft-c0de"
TOOLS_DIR="$HOME/tools"
JDKS=(21 25)
DOCKER_IMAGES=(itzg/minecraft-server:java21 itzg/minecraft-server:java25)
HEADLESSMC_VERSION="2.10.0"

step() { printf '\n==> %s\n' "$*"; }

if [ "$(id -u)" -eq 0 ]; then
    echo "Run this script as the development user, not as root." >&2
    exit 1
fi

step "Base packages"
sudo -E apt-get update -qq
sudo -E apt-get install -y -qq ca-certificates curl gnupg git jq rsync unzip xvfb xauth >/dev/null

step "Eclipse Temurin JDKs ${JDKS[*]} (Adoptium APT repository)"
if [ ! -f /etc/apt/keyrings/adoptium.asc ]; then
    sudo install -d -m 0755 /etc/apt/keyrings
    curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /etc/apt/keyrings/adoptium.asc >/dev/null
fi
codename="$(. /etc/os-release && echo "$VERSION_CODENAME")"
sudo tee /etc/apt/sources.list.d/adoptium.sources >/dev/null <<EOF
Types: deb
URIs: https://packages.adoptium.net/artifactory/deb
Suites: $codename
Components: main
Signed-By: /etc/apt/keyrings/adoptium.asc
EOF
sudo -E apt-get update -qq
for version in "${JDKS[@]}"; do
    sudo -E apt-get install -y -qq "temurin-$version-jdk" >/dev/null
done
# Java 25 is the default: the Gradle daemon needs it (Fabric Loom 1.18). The other JDKs are found by the
# Gradle toolchains.
sudo update-alternatives --set java "$(update-alternatives --list java | grep temurin-25)"
java -version 2>&1 | head -1

step "Docker"
if ! command -v docker >/dev/null; then
    echo "Docker is not installed. It is expected to be installed already (doenerstag setup)." >&2
    exit 1
fi
if ! id -nG | grep -qw docker; then
    sudo usermod -aG docker "$USER"
    echo "Added $USER to the group docker. Log in again before using docker without sudo."
fi
for image in "${DOCKER_IMAGES[@]}"; do
    sudo docker pull -q "$image"
done

step "Repository clone in $CLONE_DIR"
if [ -d "$CLONE_DIR/.git" ]; then
    git -C "$CLONE_DIR" fetch --prune origin
else
    mkdir -p "$(dirname "$CLONE_DIR")"
    git clone "$REPOSITORY_URL" "$CLONE_DIR"
fi

step "HeadlessMC $HEADLESSMC_VERSION in $TOOLS_DIR/headlessmc"
mkdir -p "$TOOLS_DIR/headlessmc"
jar="$TOOLS_DIR/headlessmc/headlessmc-launcher-$HEADLESSMC_VERSION.jar"
if [ ! -f "$jar" ]; then
    curl -fsSL -o "$jar" \
        "https://github.com/headlesshq/headlessmc/releases/download/$HEADLESSMC_VERSION/headlessmc-launcher-$HEADLESSMC_VERSION.jar"
fi
ln -sf "$jar" "$TOOLS_DIR/headlessmc/headlessmc-launcher.jar"

step "Shared folder $SHARED_DIR"
if mount | grep -q " on $SHARED_DIR type vboxsf"; then
    echo "The Windows working copy is mounted."
else
    echo "WARNING: the VirtualBox shared folder is not mounted at $SHARED_DIR."
    echo "Add the folder C:\\Users\\white\\git\\minecraft-c0de as shared folder 'minecraft-c0de'"
    echo "(auto-mount, mount point $SHARED_DIR) in VirtualBox and restart the VM."
fi

step "Done"
free -g | head -2
df -h / | tail -1
