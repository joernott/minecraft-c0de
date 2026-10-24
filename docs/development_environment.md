# Development environment

This document describes how the development environment for the c0de mod is set up. The mod supports Fabric and NeoForge. Development starts on Minecraft 1.21.1; all newer releases (1.21.x and the year-based versions 26.x) are added later.

## Overview

| Place | Purpose |
|---|---|
| Windows workstation | Writing code, building with Gradle, unit tests, running the Minecraft client of both loaders for manual tests (development runs and Modrinth App profiles) |
| Development VM (`doener@192.168.178.101`, Debian 13) | Linux builds, dedicated test servers in Docker, automated integration tests, headless client tests |
| GitHub Actions | Build and test of every push and pull request for all loaders and Minecraft versions, releases |

## Toolchain decisions

### Multi-loader and multi-version build

The project uses **Stonecutter** to build one source tree for several Minecraft versions and both loaders:
  - Every combination of Minecraft version and loader (e.g. `1.21.1-fabric`, `1.21.1-neoforge`) is a Stonecutter node with its own Gradle subproject.
  - Version- and loader-specific code is kept in the same source files and switched with Stonecutter comments (`//? if fabric {`, `//? if >=26.1 {`).
  - Fabric is built with **Fabric Loom**, NeoForge with **ModDevGradle**. No Architectury API is used, so players don't need an extra library mod.
  - The project is based on the official [stonecutter-template-multiloader](https://github.com/stonecutter-versioning/stonecutter-template-multiloader) (evaluated in task 1.1). It is minimal, uses the plugin [loom-back-compat](https://codeberg.org/KikuGie/loom-back-compat) to build obfuscated (1.21.x) and unobfuscated (26.x) versions with the same script, and doesn't add any runtime dependency. The alternative [multicutter](https://github.com/pajicadvance/multicutter) was rejected: it requires the mods Fzzy Config and Mixson at runtime and adds Kotlin and KSP to the build. Its use of the [Mod Publish Plugin](https://github.com/modmuss50/mod-publish-plugin) for Modrinth is adopted in sprint 11.
  - The committed sources are in the state of the node `1.21.1-fabric` (`vcsVersion`). Before committing, the active node MUST be switched back to `1.21.1-fabric` (`./gradlew "Reset active project"`).

In addition, the interpreter is a separate Gradle module **`core`** written in plain Java without any Minecraft or loader dependency. Its sources are compiled directly into every node (they are added as source directory), so no jar-in-jar is needed for it; the module itself exists to compile and test them in isolation. It contains the line model, parser, type system, expression evaluator, cursor state machine, built-in functions and the libraries c0de/math, c0de/strings and c0de/time. Most of the language can therefore be developed and tested with plain JUnit, independently of Minecraft versions. The Minecraft modules translate blocks in the world into the line model of `core` and execute the cursor.

### Mappings

Minecraft 26.1 and newer are no longer obfuscated, and Fabric has stopped maintaining its Yarn mappings. To keep porting cheap, the code uses **Mojang's official names from the start**, with Parchment for parameter names on 1.21.x. Code written for 1.21.1 then uses the same names as 26.x.

### Java

| Minecraft | Java |
|---|---|
| 1.21.1 – 1.21.11 | 21 |
| 26.1 and newer | 25 |

The build uses Gradle toolchains. Gradle downloads missing JDKs automatically (foojay resolver), so each node is compiled with the right Java version. The Gradle daemon itself runs on **Java 25**, because Fabric Loom 1.18 requires it; `gradle/gradle-daemon-jvm.properties` declares this, so Gradle starts the daemon with Java 25 (and downloads it if necessary) even if it is launched with an older Java. NeoForge for 26.x needs Gradle 9.1 or newer.

### Library versions

Versions of Minecraft, Fabric Loader, Fabric API, NeoForge, Parchment and the Gradle plugins are pinned per node in the Stonecutter configuration of the repository. They are taken from the official sources when a node is added or updated:
  - Fabric: <https://fabricmc.net/develop/>
  - NeoForge: <https://projects.neoforged.net/neoforged/neoforge>
  - Parchment: <https://parchmentmc.org/docs/getting-started>

Dependabot proposes updates of the GitHub Actions and the Gradle plugins as pull requests (task 1.7). It doesn't understand `stonecutter.properties.toml`, so the versions of Minecraft, the loaders, Fabric API and Parchment are updated manually from the sources above, at the latest at the start of every sprint.

### Third-party libraries

YAML (for TO_YAML/FROM_YAML and the c0de disk) needs SnakeYAML Engine (YAML 1.2), which also parses JSON (see concept review §13). It is embedded in the mod jar with the jar-in-jar mechanisms of both loaders (`include` in Loom, `jarJar` in ModDevGradle), so players don't need to install anything else.

## Windows workstation

### Software

| Software | Notes |
|---|---|
| JDK 21 | Already installed (Oracle Java 21.0.8). Eclipse Temurin 21 is recommended because CI uses it too. JDK 25 is downloaded by Gradle when 26.x nodes are built. |
| Git, GitHub CLI (`gh`) | For the repository and for checking CI results |
| IntelliJ IDEA Community + plugin "Minecraft Development" | Recommended IDE: understands Mixins, access wideners, mod metadata and Stonecutter run configurations |
| Visual Studio Code + "Extension Pack for Java" + "Gradle for Java" | Possible alternative. Mixin support is weaker; runs are started as Gradle tasks |
| Python 3 | Already installed (`py` on Windows); used for the documentation scripts and the generators of the resources |

### Daily work

  - `./gradlew :core:test` runs the unit tests of the interpreter. They don't need Minecraft and run in seconds.
  - `./gradlew build` builds and tests all active nodes.
  - Stonecutter selects the *active* node (e.g. `1.21.1-fabric`), whose sources are shown in the IDE. `./gradlew "Set active project to 1.21.1-neoforge"` switches it.
  - `runClient` and `runServer` of each node start Minecraft with the mod in development mode. Two client instances (one per loader) can be connected to the test servers on the VM.
  - `py scripts/generate_content.py` writes the block states, models, loot tables, recipes and language files of the language blocks, and `py scripts/generate_textures.py` writes the placeholder textures. Both loaders use the same generated files. After adding a block to `CodeBlockType`, add it to both scripts and commit the generated files; CI fails if the JSON files are not up to date.
  - To test a built jar like a player, add it to a Fabric or NeoForge 1.21.1 profile in the Modrinth App ("Add content" → from file). This also shows how the mod behaves together with other mods from Modrinth.

## Development VM

### Current state

Checked on 2026-10-19: Debian 13 (trixie), 8 CPU cores, 15 GB RAM, Docker 29.8, xvfb, Git. **No JDK.** The disk was increased to 128 GB; the root file system has 107 GB free.

### Disk space

The VM needs more disk space. Estimated demand for this project:

| Item | Size |
|---|---|
| Gradle caches (Minecraft, loaders, mappings) per Minecraft version and loader | 1–2 GB |
| JDK 21 and 25 | 0.7 GB |
| Docker images and server directories per Minecraft version and loader | 1–1.5 GB |
| Headless client installations | 1 GB per version |

With 4–6 Minecraft versions, both loaders and the existing doenerstag data, at least 60 GB are needed. This is covered by the enlarged disk.

### Repository on the VM

The doenerstag repository is shared with the VM via a VirtualBox shared folder. Gradle does not work reliably on `vboxsf` (file locking, slow I/O), so this project uses two locations:
  - a **shared folder** `C:\Users\white\git\minecraft-c0de` → `/home/doener/minecraft-c0de` (read-only is sufficient). It gives the VM direct access to jars built on Windows, without copying.
  - a **native clone** in `/home/doener/src/minecraft-c0de` for Linux builds and integration tests, which checks out the branch to be tested from GitHub (`git fetch && git checkout <branch>`).

### Provisioning

The script `contrib/setup_dev_vm.sh` (task 1.8) defines the VM environment, like `contrib/setup_dev_pipeline.sh` does for doenerstag. A tool the tests need that the script doesn't install is a defect in the script. It
  1. installs Eclipse Temurin 21 and 25 from the Adoptium APT repository,
  2. installs `jq`, `rsync` and `xvfb` (RCON commands are sent with `rcon-cli`, which is part of the server image),
  3. creates `/home/doener/src/minecraft-c0de` and clones the repository,
  4. pulls the Docker image `itzg/minecraft-server` (Java 21 and Java 25 variants),
  5. installs HeadlessMC for headless client tests,
  6. checks that the shared folder is mounted.

### Test servers

`contrib/docker/compose.yaml` (task 1.9) starts one dedicated server per loader and Minecraft version with the image [`itzg/minecraft-server`](https://docker-minecraft-server.readthedocs.io/):

| Service | Loader | Port |
|---|---|---|
| `c0de-1.21.1-fabric` | Fabric (with Fabric API) | 25565 |
| `c0de-1.21.1-neoforge` | NeoForge | 25566 |

  - `EULA=TRUE`, `TYPE=FABRIC` or `TYPE=NEOFORGE`, `VERSION=1.21.1`, loader versions pinned like in the build.
  - `contrib/itest/smoke.sh` copies the built mod jars to `contrib/docker/mods/<node>/`, which is mounted into `/mods`, and starts the servers.
  - RCON is only used inside the containers: `docker compose -f contrib/docker/compose.yaml exec c0de-1.21.1-fabric rcon-cli <command>`.
  - `ONLINE_MODE=false`, so development clients from Windows can connect without a Microsoft account check; the ports are only reachable in the local network.
  - Server data is stored in volumes that the integration tests can reset.

### Integration and client tests

  - **GameTests** (server side) are part of the mod source and run with `runGameTestServer` of both loaders, on the VM and in GitHub Actions. They build sheets with test programs in the c0de dimension, start processes and check results.
  - **RCON scenario tests** (`contrib/itest/`) run against the Docker servers. They cover what GameTests can't easily reach: server restarts with running processes, configuration files, permissions of non-operator players and long-running timers.
  - **Headless client tests** start the client with HeadlessMC under `xvfb-run`, join a test server and check that the client starts without errors and that the mod's screens and models load. For versions whose Fabric API offers client GameTests, those replace HeadlessMC.

The smoke test (`contrib/itest/smoke.sh`) also runs a process scenario (`contrib/itest/process_scenario.sh`): it builds a program on sheet 0 with `setblock` commands, starts it with a trigger block (redstone) and a timer block, and restarts the server while a third process runs, which must continue afterwards.

## GitHub Actions

| Workflow | Trigger | Content |
|---|---|---|
| `build.yml` | Push, pull request | Matrix over all active nodes (Minecraft version × loader): build, unit tests of `core`, GameTests with `runGameTestServer`, upload of the jars as artifacts. A Windows job runs the `core` tests to catch platform-specific problems (line endings, paths). |
| `integration.yml` | Push to `main` and sprint branches, manually | Starts the Docker test servers on the Ubuntu runner (Docker is available there) and runs the RCON scenario tests. |
| `release.yml` | Tag `v*` | Builds all nodes, creates a GitHub release with all jars and publishes them to Modrinth. The token is stored as repository secret `MODRINTH_TOKEN`. |

All workflows use `actions/setup-java` (Temurin) and `gradle/actions/setup-gradle` for caching. Every check is reported as an annotation, so failures are readable without opening the job logs.

### Releasing a version

The jars are published with the [Mod Publish Plugin](https://modmuss50.github.io/mod-publish-plugin/) (`publishMods` in both build scripts): one Modrinth version per node, named `<mod.version>+<Minecraft version>-<loader>`, as alpha for 0.x versions, with Fabric API as required and Patchouli as optional dependency. Without the secret or the project ID, `./gradlew :<node>:publishMods` is a dry run that prints what it would publish.

  1. Once: create the Modrinth project (description from `docs/modrinth.md`), put its ID into `publish.modrinth_id` in `stonecutter.properties.toml` and store a Modrinth token with the scope to create versions as repository secret `MODRINTH_TOKEN`.
  2. Set `mod.version` in `stonecutter.properties.toml` and add the section `## <version>` to `CHANGELOG.md`; it becomes the changelog on Modrinth and the notes of the GitHub release.
  3. Fast-forward `main`, then tag and push: `git tag v<version> && git push origin v<version>`. `release.yml` checks that the tag matches `mod.version`, builds and tests all nodes, publishes them to Modrinth and creates the GitHub release (a pre-release for 0.x versions).

## Setup steps

1. ~~Increase the VM disk~~ (done: 128 GB).
2. ~~Add the VirtualBox shared folder `minecraft-c0de`~~ (done, mounted at `/home/doener/minecraft-c0de`).
3. ~~Run `contrib/setup_dev_vm.sh` on the VM~~ (done on 2026-10-19; run it again after changes of the script).
4. Install IntelliJ IDEA Community with the Minecraft Development plugin on Windows (optional, if VS Code is not enough).
5. Create the c0de project on Modrinth, create a personal access token with the scope to upload versions and store it as GitHub secret `MODRINTH_TOKEN` (only needed before the first release, sprint 11).

## Running the tests on the VM

```sh
cd ~/src/minecraft-c0de
git fetch && git checkout <branch> && git pull
./gradlew build                      # build, unit tests
./gradlew :1.21.1-fabric:runGametest :1.21.1-neoforge:runGameTestServer
contrib/itest/smoke.sh               # dedicated servers in Docker
```
