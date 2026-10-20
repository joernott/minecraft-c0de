# Implementation plan

This plan implements the [c0de concept](concept.md) in sprints. Each sprint has a goal, numbered tasks and a result that can be demonstrated. The development environment is described in [development_environment.md](development_environment.md).

## Principles

  - **Interpreter first, in plain Java.** The module `core` contains the whole language without Minecraft: line model, parser, types, expressions, cursor, functions and the libraries c0de/math, c0de/strings and c0de/time. It is developed test-first with JUnit. The Minecraft modules only translate blocks into the line model, run cursors and provide c0de/minecraft.
  - **Both loaders from the first line of code.** Every sprint delivers Fabric and NeoForge. Loader-specific code is kept small and behind a platform interface.
  - **One Minecraft version until 1.0, then the others.** Sprints 1–11 target 1.21.1 only. Mojang names are used from the start, so the port to newer versions (sprint 12) doesn't need a mapping migration.
  - **Every sprint ends with a working mod.** CI is green for both loaders, and the result of the sprint can be shown in the game (from sprint 2 on).

## Branching and commits

  - `sprint-0` contains the concept and this plan and shares its commit with `main`.
  - Every sprint n gets a branch `sprint-n`, created from the tip of `sprint-(n-1)`. It contains exactly the commits of that sprint.
  - Commit messages start with the task they close: `Task 3.4: Evaluate expressions with shunting-yard`. A commit that closes several tasks names all of them.
  - A defect found in a later sprint is fixed on the branch of the sprint that found it; the commit message names the sprint whose work it corrects.
  - At the end of a sprint, the branch is merged into `main` once CI is green.

## Tracking

  - Completed tasks are marked with ✅ in the column "Done" of their sprint table. A sprint is marked as done in the sprint overview when all of its tasks are done.
  - If an additional task turns out to be necessary to complete a task, it is added below that task with the next level number: an additional task for 0.1 becomes 0.1.1, a second one 0.1.2, an additional task for 0.1.1 becomes 0.1.1.1.

## Definition of done

A task is done when
  - the code is implemented for both loaders (if it is not part of `core`),
  - unit tests (`core`) or GameTests (Minecraft modules) cover the behavior described in the concept, including the error cases,
  - CI is green,
  - the concept is updated if the implementation required a decision that the concept doesn't cover yet.

## Sprint overview

| Sprint | Goal | Result | Done |
|---|---|---|:---:|
| 0 | Concept and plan | Concept, implementation plan, development environment plan | ✅ |
| 1 | Project skeleton and environment | Empty mod loads on Fabric and NeoForge 1.21.1, CI and VM are ready | ✅ |
| 2 | The c0de dimension | Players can enter the dimension with its greenbar paper floor | ✅ |
| 3 | Interpreter I: syntax and values | `core` parses programs and evaluates expressions | ✅ |
| 4 | Interpreter II: execution | `core` runs complete programs step by step, including functions, loops and errors | ✅ |
| 5 | Libraries in `core` | Built-in functions, c0de/math, c0de/strings (incl. FORMAT, JSON, YAML), c0de/time |  |
| 6 | Language blocks I | All language blocks exist, with dialogs and recipes |  |
| 7 | Language blocks II: validation | Blocks show whether their statements are valid |  |
| 8 | Processes | Trigger, timer, PID block and cursor run programs in the world |  |
| 9 | Debugging and tools | Debug cursor, debugging tool, marker, wrench, hammer |  |
| 10 | c0de/minecraft and c0de disk | Programs interact with the world; programs are imported and exported as YAML |  |
| 11 | Release 1.0 for 1.21.1 | Polished, documented and published mod |  |
| 12 | Newer Minecraft versions | The mod is released for 1.21.x and 26.x |  |

## Sprint 0: Concept and plan

| Task | Description | Done |
|---|---|:---:|
| 0.1 | Write and review the concept (`docs/concept.md`, `docs/concept/`). | ✅ |
| 0.2 | Write the implementation plan (this document). | ✅ |
| 0.3 | Write the development environment plan. | ✅ |
| 0.4 | Answer the open questions that block sprint 1 and 2 (see [Decisions](#decisions)). Add the BSD 3-Clause license. | ✅ |

## Sprint 1: Project skeleton and environment

**Goal:** an empty mod with the ID `c0de` loads on Fabric and NeoForge 1.21.1. CI builds and tests every push, and the VM can run test servers.

| Task | Description | Done |
|---|---|:---:|
| 1.1 | Evaluate the Stonecutter templates ([multicutter](https://github.com/pajicadvance/multicutter), [stonecutter-template-multiloader](https://github.com/stonecutter-versioning/stonecutter-template-multiloader)): both must build 1.21.1 for Fabric and NeoForge on Windows and Linux. Adopt the better one and keep only the 1.21.1 nodes active. | ✅ |
| 1.2 | Set up the Gradle build: Stonecutter nodes `1.21.1-fabric` and `1.21.1-neoforge`, Mojang mappings with Parchment, Java 21 toolchain, mod metadata (`fabric.mod.json`, `neoforge.mods.toml`), mod ID `c0de`, group ID and base package `de.joernott.c0de`, license `BSD-3-Clause`, version from Git tags. | ✅ |
| 1.3 | Create the module `core` (plain Java 21, JUnit 5, no Minecraft dependency) and include it in the mod jar of both loaders. | ✅ |
| 1.4 | Create the platform abstraction for loader-specific functionality and the mod entry points of both loaders. Registration, networking and events are added to the abstraction when the first content needs them (sprint 2). | ✅ |
| 1.5 | Configuration file with all settings of the concept (`maxBuildHeight`, `stackSize`, `sayRadius`, `allowInventoryChanges`, `allowBlockChanges`, `allowSpawning`, `allowTeleporting`), loaded on both loaders. | ✅ |
| 1.6 | Code quality: Spotless (formatting of `core`), `scripts/check-whitespace.sh` (whitespace of all other files, as Spotless only accepts files within its project and a Java formatter would move Stonecutter comments), `.editorconfig`, `.gitattributes` (line endings), README with build instructions. | ✅ |
| 1.7 | GitHub Actions workflow `build.yml` (matrix over nodes, Windows job for `core`), Renovate or Dependabot for dependency updates. | ✅ |
| 1.8 | `contrib/setup_dev_vm.sh`: provision the VM as described in the development environment plan, and run it on the VM. | ✅ |
| 1.9 | `contrib/docker/compose.yaml` with the test servers for both loaders; workflow `integration.yml` with a first smoke test `contrib/itest/smoke.sh` (server starts, mod is loaded, checked via RCON and the log). | ✅ |
| 1.10 | GameTest setup for both loaders with one trivial test, run in CI with `runGameTestServer`. | ✅ |

## Sprint 2: The c0de dimension

**Goal:** a player can enter the c0de dimension and sees the endless sheets of greenbar paper.

| Task | Description | Done |
|---|---|:---:|
| 2.1 | Dimension type and dimension (data pack in the mod resources): no mob spawning, no daylight cycle, no weather, cloudless sky, ambient light, bottom at y=-16, configurable height (`maxBuildHeight`). | ✅ |
| 2.2 | Blocks white, light green, black and cursor c0de bedrock: unbreakable, uncraftable, textures. | ✅ |
| 2.3 | Chunk generator for the floor: the sheet pattern from `floor_c0de.svg` for chunks with x ≥ 0 and z ≥ 0, black bedrock elsewhere, cursor rail at relative x=4. | ✅ |
| 2.4 | Entering and leaving the dimension: a provisional trigger block that can be right-clicked with a provisional wrench (both completed in sprints 8 and 9), arrival at the program's sheet or the first free sheet, storing the return position, leaving by right-clicking black c0de bedrock with an empty hand. | ✅ |
| 2.5 | Sheet geometry helpers: absolute ↔ relative coordinates, sheet index, line, code layer y=-3, valid code area x=5..28. Unit tests. | ✅ |
| 2.6 | GameTests: floor pattern, unbreakability, no mob spawning. | ✅ |

## Sprint 3: Interpreter I – syntax and values (`core`)

**Goal:** `core` turns a sheet (as line model) into an internal program structure and evaluates expressions. Everything runs in unit tests, without Minecraft.

| Task | Description | Done |
|---|---|:---:|
| 3.1 | Line model: a program is a list of lines, each line a list of block tokens with x coordinate and properties (keyword, label name/index/kind, literal type/value, operator symbol, comment text, tag language/name). A text notation for tests (like the examples in the concept), so that test programs can be written as text. | ✅ |
| 3.2 | Type system: BOOLEAN, INTEGER, FLOAT, CHAR, TIME, STRING (with length limit), ARRAY, HASH, STRUCT, ERROR; default values; text representation; copy semantics. | ✅ |
| 3.3 | Line reading: COMMENT handling, NOOP rules, indentation checks, placement rules, matching BEGIN/END (all SYNTAX_ERROR_* cases of the cursor chapter). | ✅ |
| 3.4 | Expressions: shunting-yard with all operators and their precedence, parentheses up to depth 8, `;`, continuation lines, type rules (incl. TIME arithmetic), runtime errors (DIVISION_BY_ZERO, NUMERIC_OVERFLOW, TYPE_MISMATCH). | ✅ |
| 3.5 | Labels with index: name resolution, index evaluation for arrays, hashes, structs, strings (read and write access). | ✅ |
| 3.6 | Declarations: STRUCT with tags, VARIABLE, CONSTANT, literals, initializers of complex values (nested up to depth 8). | ✅ |
| 3.7 | Error codes as enum with numbers, names and the built-in constants. | ✅ |

## Sprint 4: Interpreter II – execution (`core`)

**Goal:** `core` executes complete programs line by line, exactly as the cursor will do in the world.

| Task | Description | Done |
|---|---|:---:|
| 4.1 | Cursor state machine: current line, state (RUNNING, PAUSED, CRASHED), one step per line, jumps. The state can be serialized, so that processes survive a server restart (needed in sprint 8). | ✅ |
| 4.2 | Stack with configurable size: variable and constant items, function items, block markers, STACK_OVERFLOW, lexical name resolution. | ✅ |
| 4.3 | Program header: PROGRAM, INCLUDE (library registry), global declarations, entry function, empty line. | ✅ |
| 4.4 | Functions: FUNCTION, PARAMETER, RESULT, RETURN, CALL with named parameters and results, recursion, by-reference parameters for FROM_JSON/FROM_YAML, tag passing. | ✅ |
| 4.5 | Control flow: BEGIN/END, IF/ELSE, FOR, EACH, WHILE, REPEAT, BREAK, EXIT. | ✅ |
| 4.6 | Error handling: crash vs. raise, propagation through call frames, error labels of CALL, exit codes (incl. 15 for crashes). | ✅ |
| 4.7 | Interface between `core` and the mod: input and output values of the entry function (variable and constant items), library functions implemented outside of `core` (c0de/minecraft), SLEEP as "wait n steps". | ✅ |
| 4.8 | Test suite with complete example programs (one per chapter of the concept). | ✅ |

## Sprint 5: Libraries in `core`

**Goal:** all functions that don't need Minecraft are available.

| Task | Description | Done |
|---|---|:---:|
| 5.1 | Built-in functions: LEN, HAS_KEY, REMOVE, INSERT, CLEAR, SWAP, RAISE. |  |
| 5.2 | c0de/math: all functions, PI, per-process random number generator. |  |
| 5.3 | c0de/strings: string and character functions, STR, VAL, HEX, OCT. |  |
| 5.4 | FORMAT: printf conversions, flags, width, precision, `%t…` conversions, server time zone. |  |
| 5.5 | TO_JSON and FROM_JSON with Gson: conversion table, struct tags, variable tags with paths (incl. array indexes), defaults for missing elements, atomic writes. |  |
| 5.6 | TO_YAML and FROM_YAML with SnakeYAML Engine (embedded via jar-in-jar), same rules as JSON. |  |
| 5.7 | c0de/time: NOW, MILLISECOND … YEAR, WEEKDAY, WEEK (ISO 8601), TIMER, SLEEP. |  |

## Sprint 6: Language blocks I

**Goal:** all language blocks and crafting items exist in the game with textures, recipes and dialogs.

| Task | Description | Done |
|---|---|:---:|
| 6.1 | Base blocks and items: NOOP, type base, structural base. All recipes of the concept as data-generated JSON. |  |
| 6.2 | Block entities for properties and their synchronization to clients (label, literal, operator, comment, tag). |  |
| 6.3 | Dialog framework (client screen + network packets for both loaders), read-only mode for protected sheets. |  |
| 6.4 | Blocks with dialogs: LABEL (incl. the context-dependent dropdowns, filled with static data for now), LITERAL, OPERATOR, COMMENT (text shown above the block), TAG. |  |
| 6.5 | Keyword blocks: PROGRAM, INCLUDE, EXIT, BEGIN, END, BREAK, VARIABLE, CONSTANT, ASSIGN, FUNCTION, PARAMETER, RESULT, RETURN, CALL, IF, ELSE, FOR, EACH, WHILE, REPEAT. |  |
| 6.6 | Type blocks: TYPE_BOOLEAN … TYPE_ERROR, including TYPE_TIME. |  |
| 6.7 | Textures and models for all blocks with a valid and an invalid variant (block state `valid`); language files `en_us` and `de_de`. |  |
| 6.8 | Data generation for models, block states, recipes, loot tables and tags, shared by both loaders. |  |

## Sprint 7: Language blocks II – validation

**Goal:** the blocks of a sheet show whether their statements are valid while the player is writing the program.

| Task | Description | Done |
|---|---|:---:|
| 7.1 | Sheet reader: reads the blocks of a sheet into the line model of `core` (server side, only loaded chunks). |  |
| 7.2 | Static validation in `core`: checks a program without running it and reports the state of every block (valid, invalid, reason). Reuses the rules of sprints 3 and 4. |  |
| 7.3 | Incremental validation: when a block on a sheet is placed or removed, the affected statements are validated again and the `valid` block states are updated. |  |
| 7.4 | Placement rules that are checked when a block is placed (e.g. FUNCTION only at x=5 after a NOOP line, END not directly after BEGIN, NOOP lines stay empty). |  |
| 7.5 | LABEL dialogs with live data: programs, libraries, functions, parameters, variables in scope, struct types. |  |
| 7.6 | Program registry: unique program names per dimension, list of programs and their functions for the trigger dialogs. |  |

## Sprint 8: Processes

**Goal:** trigger blocks start processes, and cursors run programs in the world.

| Task | Description | Done |
|---|---|:---:|
| 8.1 | Data item, type items, variable and constant items with their properties and edit dialog. |  |
| 8.2 | Trigger block, timer block and debug trigger block: configuration dialog, owner, input and output inventory, camouflage slot, preconditions for starting a process; placement rules in the c0de dimension (negative area only). |  |
| 8.3 | Process start: PID allocation, PID block, cursor entity, transfer of the input items. |  |
| 8.4 | Process scheduler: one step per tick and cursor, chunk tickets for running sheets, SLEEP. |  |
| 8.5 | Process end: results to the output inventory, exit code, error, comparator output, automatic transfer to an adjacent inventory, removal of cursor and PID block. |  |
| 8.6 | Protection of running programs: no changes to sheets with running processes by players, pistons, explosions, fluids or other mods; read-only dialogs; message to the player. |  |
| 8.7 | Persistence: running processes survive saving and restarting the server (serialized cursor state). |  |
| 8.8 | GameTests and RCON scenario tests for complete programs started by redstone and timers. |  |

## Sprint 9: Debugging and tools

**Goal:** players can debug programs and edit them comfortably.

| Task | Description | Done |
|---|---|:---:|
| 9.1 | Debug cursor: PAUSED/RUNNING, single steps, started by the debug trigger block. |  |
| 9.2 | Debugging tool: controlling debug cursors, cursor dialog (properties and stack), read-only property dialog for every c0de block. |  |
| 9.3 | Marker: marking lines with an overlay (client rendering, synchronized marks). |  |
| 9.4 | Wrench: moving marked lines. |  |
| 9.5 | Hammer: deleting lines or marked areas, moving the following lines up. |  |

## Sprint 10: c0de/minecraft and c0de disk

**Goal:** programs can interact with the world, and programs can be exchanged as YAML files.

| Task | Description | Done |
|---|---|:---:|
| 10.1 | Library infrastructure in the mod: library types (POSITION, ITEM_STACK, ENTITY), owner and permission checks, CHUNK_NOT_LOADED, PERMISSION_DENIED. |  |
| 10.2 | Unrestricted functions: TICKS, BEEP, SOUND, SAY, GET_BLOCK, GET_INVENTORY, levers, buttons, pressure plates, doors/trapdoors/fence gates, FIND_ENTITIES. |  |
| 10.3 | Restricted functions: MOVE_ITEMS, DELETE_ITEMS, PLACE_BLOCK, REMOVE_BLOCK, SPAWN_ITEM, SPAWN_ENTITY, TELEPORT; changes are made as the owner, so protection mods apply. |  |
| 10.4 | YAML export of a sheet (`core`: line model → YAML; mod: sheet → line model). |  |
| 10.5 | YAML import: layout algorithm in `core` (indentation, BEGIN/END, continuation lines), placement in the world, consumption of blocks in survival mode. |  |
| 10.6 | c0de disk item with its dialogs. |  |
| 10.7 | Round-trip tests: every example program of the test suite survives export and import unchanged. |  |

## Sprint 11: Release 1.0 for Minecraft 1.21.1

**Goal:** the mod is published for Fabric and NeoForge 1.21.1.

| Task | Description | Done |
|---|---|:---:|
| 11.1 | Final textures and sounds, review of all block and item looks. |  |
| 11.2 | Player documentation: a guide with examples (Markdown in the repository and the Modrinth page); optionally an in-game book. |  |
| 11.3 | Performance tests: many processes at the same time, large programs, deep recursion; tuning of the scheduler. |  |
| 11.4 | Multiplayer tests on the Docker servers with several clients (development clients and Modrinth App profiles), including non-operator players and permissions. |  |
| 11.5 | Workflow `release.yml`, Modrinth project, changelog. |  |
| 11.6 | Release 1.0.0. |  |

## Sprint 12: Newer Minecraft versions

**Goal:** the mod is available for all newer Minecraft releases.

| Task | Description | Done |
|---|---|:---:|
| 12.1 | Add the node for the last 1.21.x release (1.21.11) and port the API changes (e.g. the reworked recipe system of 1.21.2, item model definitions of 1.21.4, GUI rendering changes). |  |
| 12.2 | Add the nodes for 26.x (unobfuscated, Java 25, Gradle 9.1+) and port the API changes. |  |
| 12.3 | Extend the CI matrix and the Docker test servers to all nodes. |  |
| 12.4 | Replace HeadlessMC by Fabric client GameTests where available. |  |
| 12.5 | Release for all versions; document the process for adding future Minecraft versions. |  |

After sprint 12, every new Minecraft release is added as a new node in its own small sprint.

## Risks

| Risk | Mitigation |
|---|---|
| API changes between 1.21.1 and 26.x are large (rendering, recipes, item models, networking). | The interpreter is in `core` and doesn't depend on Minecraft. Minecraft-specific code is kept thin and is concentrated in few classes. |
| Two loaders double the effort for everything that touches the game. | Platform interface, shared data generation and Stonecutter keep the loader-specific code small. GameTests run on both loaders in CI. |
| Cursors running every tick slow down servers with many processes. | Performance tests in sprint 11; the scheduler can limit the number of steps per tick. |
| Running processes depend on loaded chunks and must survive restarts. | Chunk tickets and serialized cursor states (tasks 8.4, 8.7). |
| The concept still changes during implementation. | The concept is updated as part of the definition of done; `concept_review.md` records decisions. |

## Decisions

Answers to the open questions of sprint 0 (2026-10-19):

| Question | Decision |
|---|---|
| Access to the dimension | Right-clicking a trigger block with a wrench teleports the player into the dimension; right-clicking black c0de bedrock with an empty hand teleports them back. See [the concept](concept/01-dimension.md#entering-and-leaving-the-dimension). |
| Package name and group ID | `de.joernott.c0de` |
| License | BSD 3-Clause ([LICENSE](../LICENSE)) |
| Publishing | Modrinth only for now. The Modrinth account is linked to the GitHub account. CurseForge can be added later. |
