# c0de

c0de is a primitive programming language for Minecraft. Programs are written by placing blocks on endless sheets of greenbar paper in a dimension of their own, and they are run by trigger blocks anywhere in the world.

The mod supports Fabric and NeoForge. Development starts with Minecraft 1.21.1; newer versions follow.

> **Status:** the first alpha (0.1.0) is in preparation. See the [implementation plan](docs/implementation_plan.md) for the progress.

## Documentation

  - [Guide](docs/guide/01-getting-started.md): how to write and run programs, for players
  - [Reference](docs/reference/index.md): handbooks with a page for every block, item and function
  - [Concept](docs/concept.md): the language, the dimension, the blocks and how programs are run
  - [Implementation plan](docs/implementation_plan.md): sprints and tasks
  - [Development environment](docs/development_environment.md): tools, VM, CI

## Building

Requirements: JDK 21 or newer to run Gradle. Gradle downloads the JDKs the build needs.

```sh
./gradlew build            # builds and tests everything, jars in versions/<node>/build/libs/
./gradlew :core:test       # unit tests of the interpreter (no Minecraft needed)
./gradlew :core:spotlessApply   # formats the interpreter sources
scripts/check-whitespace.sh     # checks the whitespace of all other files
```

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) to build one source tree for several Minecraft versions and both loaders. Every combination is a *node*, e.g. `1.21.1-fabric` or `1.21.1-neoforge`:

```sh
./gradlew :1.21.1-fabric:runClient         # starts the Fabric client with the mod
./gradlew :1.21.1-neoforge:runClient       # starts the NeoForge client with the mod
./gradlew :1.21.1-fabric:runGametest       # runs the GameTests on Fabric
./gradlew :1.21.1-neoforge:runGameTestServer   # runs the GameTests on NeoForge
```

Version- or loader-specific code is marked with Stonecutter comments such as `//? if fabric {`. The committed sources are in the state of the node `1.21.1-fabric`; switch the active node back to it before committing (`./gradlew "Reset active project"`).

The interpreter lives in the module `core`. It is plain Java without any dependency on Minecraft and is compiled into every node.

## License

[BSD 3-Clause](LICENSE)
