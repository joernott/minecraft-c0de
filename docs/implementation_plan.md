# | ✅ |
| ✅ |
| ✅ |
| ✅ |
| ✅ |
| ✅ |
| ✅ |
Implementation plan

This plan implements the [c0de concept](concept.md) in sprints. Each sprint has a goal, numbered tasks and a result that can be demonstrated. The development environment is described in [development_environment.md](development_environment.md).

## Principles

  - **Interpreter first, in plain Java.** The module `core` contains the whole language without Minecraft: line model, parser, types, expressions, cursor, functions and the libraries c0de/math, c0de/strings and c0de/time. It is developed test-first with JUnit. The Minecraft modules only translate blocks into the line model, run cursors and provide c0de/minecraft.
  - **Both loaders from the first line of code.** Every sprint delivers Fabric and NeoForge. Loader-specific code is kept small and behind a platform interface.
  - **One Minecraft version until the first release, then the others.** Sprints 1–11 target 1.21.1 only. Mojang names are used from the start, so the port to newer versions (sprint 12) doesn't need a mapping migration.
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
| 5 | Libraries in `core` | Built-in functions, c0de/math, c0de/strings (incl. FORMAT, JSON, YAML), c0de/time | ✅ |
| 6 | Language blocks I | All language blocks exist, with dialogs and recipes | ✅ |
| 7 | Language blocks II: validation | Blocks show whether their statements are valid | ✅ |
| 8 | Processes | Trigger, timer, PID block and cursor run programs in the world | ✅ |
| 9 | Debugging and tools | Debug cursor, debugging tool, marker, wrench, hammer | ✅ |
| 10 | c0de/minecraft and c0de disk | Programs interact with the world; programs are imported and exported as YAML | ✅ |
| 11 | Release 0.1.0 for 1.21.1 | Polished, documented and published first alpha of the mod |  |
| 12 | Newer Minecraft versions | The mod is released for 1.21.x and 26.x |  |
| 13 | Usability, process information and colors (0.3.0) | Pausable timers, a better disk drive, the built-in constants SHEET, PID, CURSOR and COLOR_*, the used stack in the cursor dialog, mining and placing blocks with items from inventories, boots and pliers | ✅ |
| 14 | Documentation (0.4.0) | The guide and the handbooks in German, a runner that tests the programs of the tutorials of c0de.at, and 3D models of all blocks for the website | ✅ |
| 15 | Feedback from the tutorials | Deviations from the concept that the tutorials of c0de.at found | ✅ |
| 16 | Security (0.6.0) | Hidden input and items, sensitive values in the debug output, a vault of secrets per player with SECRET and the library c0de/security, persistent in a KeePass file | ✅ |
| 17 | Handbooks of other mods (0.6.1) | Other mods ship full handbooks (binders, crafting grids, links, translations) as resources and data, without code in c0de; the documentation errors found by the recipes of c0de.at are fixed | ✅ |
| 18 | Findings from the screenshot world (0.6.2) | The wrench leads to the program of a trigger block or to where the player left the dimension, the password dialog of the vault keeps the password, lines wider than a sheet are reported | ✅ |
| 19 | Library types (0.7.0) | Libraries of other mods define types of their own besides structs, for the canvases and styles of c0de paint |  |

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
| 2.2.1 | Items for the floor blocks, so that creative players can repair a broken floor. | ✅ |
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
| 5.1 | Built-in functions: LEN, HAS_KEY, REMOVE, INSERT, CLEAR, SWAP, RAISE. | ✅ |
| 5.2 | c0de/math: all functions, PI, per-process random number generator. | ✅ |
| 5.3 | c0de/strings: string and character functions, STR, VAL, HEX, OCT. | ✅ |
| 5.4 | FORMAT: printf conversions, flags, width, precision, `%t…` conversions, server time zone. | ✅ |
| 5.5 | TO_JSON and FROM_JSON (own JSON writer, parsed with SnakeYAML Engine, see concept review §13): conversion table, struct tags, variable tags with paths (incl. array indexes), defaults for missing elements, atomic writes. | ✅ |
| 5.6 | TO_YAML and FROM_YAML with SnakeYAML Engine (embedded via jar-in-jar), same rules as JSON. | ✅ |
| 5.7 | c0de/time: NOW, MILLISECOND … YEAR, WEEKDAY, WEEK (ISO 8601), TIMER, SLEEP. | ✅ |

## Sprint 6: Language blocks I

**Goal:** all language blocks and crafting items exist in the game with textures, recipes and dialogs.

| Task | Description | Done |
|---|---|:---:|
| 6.1 | Base blocks and items: NOOP, type base, structural base. All recipes of the concept as data-generated JSON. | ✅ |
| 6.2 | Block entities for properties and their synchronization to clients (label, literal, operator, comment, tag). | ✅ |
| 6.3 | Dialog framework (client screen + network packets for both loaders), read-only mode for protected sheets. | ✅ |
| 6.4 | Blocks with dialogs: LABEL (incl. the context-dependent dropdowns, filled with static data for now), LITERAL, OPERATOR, COMMENT (text shown above the block), TAG. | ✅ |
| 6.4.1 | The text of a COMMENT is drawn without depth test, like the names of entities, so that it is not partly hidden. | ✅ |
| 6.5 | Keyword blocks: PROGRAM, INCLUDE, EXIT, BEGIN, END, BREAK, VARIABLE, CONSTANT, ASSIGN, FUNCTION, PARAMETER, RESULT, RETURN, CALL, IF, ELSE, FOR, EACH, WHILE, REPEAT. | ✅ |
| 6.6 | Type blocks: TYPE_BOOLEAN … TYPE_ERROR, including TYPE_TIME. | ✅ |
| 6.7 | Textures and models for all blocks with a valid and an invalid variant (block state `valid`); language files `en_us` and `de_de`. | ✅ |
| 6.7.1 | The text on the sides of blocks is shaded like the sides themselves, so that it looks the same on the top and on the sides. | ✅ |
| 6.7.2 | The triangle of ASSIGN points to positive x on all sides (dot on the east side, cross on the west side). | ✅ |
| 6.8 | Data generation for models, block states, recipes, loot tables and tags, shared by both loaders. | ✅ |

## Sprint 7: Language blocks II – validation

**Goal:** the blocks of a sheet show whether their statements are valid while the player is writing the program.

| Task | Description | Done |
|---|---|:---:|
| 7.1 | Sheet reader: reads the blocks of a sheet into the line model of `core` (server side, only loaded chunks). | ✅ |
| 7.2 | Static validation in `core`: checks a program without running it and reports the state of every block (valid, invalid, reason). Reuses the rules of sprints 3 and 4. | ✅ |
| 7.3 | Incremental validation: when a block on a sheet is placed or removed, the affected statements are validated again and the `valid` block states are updated. | ✅ |
| 7.4 | Placement rules that are checked when a block is placed (e.g. FUNCTION only at x=5 after a NOOP line, END not directly after BEGIN, NOOP lines stay empty). | ✅ |
| 7.4.1 | On a sheet, c0de blocks can only be placed in the code area of the code layer (x=5 to x=28). | ✅ |
| 7.4.2 | Indentation of keywords that start a line, compared with the keyword line above. | ✅ |
| 7.5 | LABEL dialogs with live data: programs, libraries, functions, parameters, variables in scope, struct types. | ✅ |
| 7.5.1 | Assignment targets only list variables that can be changed; the names of functions that can't be parsed yet (missing END) are listed too. | ✅ |
| 7.6 | Program registry: unique program names per dimension, list of programs and their functions for the trigger dialogs. | ✅ |
| 7.6.1 | Sheets are validated when the chunk of their PROGRAM line is loaded, so that existing programs are registered. | ✅ |

## Sprint 8: Processes

**Goal:** trigger blocks start processes, and cursors run programs in the world.

| Task | Description | Done |
|---|---|:---:|
| 8.1 | Data item, type items, variable and constant items with their properties and edit dialog. | ✅ |
| 8.2 | Trigger block, timer block and debug trigger block: configuration dialog, owner, input and output inventory, camouflage slot, preconditions for starting a process; placement rules in the c0de dimension (negative area only). | ✅ |
| 8.2.1 | The dialog of a trigger block closes when the configuration is saved. | ✅ |
| 8.3 | Process start: PID allocation, PID block, cursor entity, transfer of the input items. | ✅ |
| 8.3.1 | The cursor is an upright cuboid (one block high, 3/4 block wide) pulsing between dark green and lime (debug cursors: dark red and red). | ✅ |
| 8.4 | Process scheduler: one step per tick and cursor, chunk tickets for running sheets, SLEEP. | ✅ |
| 8.5 | Process end: results to the output inventory, exit code, error, comparator output, automatic transfer to an adjacent inventory, removal of cursor and PID block. | ✅ |
| 8.6 | Protection of running programs: no changes to sheets with running processes by players, pistons, explosions, fluids or other mods; read-only dialogs; message to the player. | ✅ |
| 8.7 | Persistence: running processes survive saving and restarting the server (serialized cursor state). | ✅ |
| 8.8 | GameTests and RCON scenario tests for complete programs started by redstone and timers. | ✅ |

## Sprint 9: Debugging and tools

**Goal:** players can debug programs and edit them comfortably.

| Task | Description | Done |
|---|---|:---:|
| 9.1 | Debug cursor: PAUSED/RUNNING, single steps, started by the debug trigger block. | ✅ |
| 9.2 | Debugging tool: controlling debug cursors, cursor dialog (properties and stack), read-only property dialog for every c0de block. | ✅ |
| 9.3 | Marker: marking lines with an overlay (client rendering, synchronized marks). | ✅ |
| 9.3.1 | Tool actions have a cooldown of 6 ticks, because holding the attack button repeats them. | ✅ |
| 9.3.2 | Clicking the floor of a line works like clicking a block of the line (marker, wrench, hammer). | ✅ |
| 9.4 | Wrench: moving marked lines. | ✅ |
| 9.4.1 | The marked lines are the selection: a left click with the wrench moves them in front of the clicked line, a right click copies them there (blocks from the inventory in survival mode). | ✅ |
| 9.5 | Hammer: deleting lines or marked areas, moving the following lines up. | ✅ |

## Sprint 10: c0de/minecraft and c0de disk

**Goal:** programs can interact with the world, and programs can be exchanged as YAML files.

| Task | Description | Done |
|---|---|:---:|
| 10.1 | Library infrastructure in the mod: library types (POSITION, ITEM_STACK, ENTITY), owner and permission checks, CHUNK_NOT_LOADED, PERMISSION_DENIED. | ✅ |
| 10.2 | Unrestricted functions: TICKS, BEEP, SOUND, SAY, GET_BLOCK, GET_INVENTORY, levers, buttons, pressure plates, doors/trapdoors/fence gates, FIND_ENTITIES. | ✅ |
| 10.3 | Restricted functions: MOVE_ITEMS, DELETE_ITEMS, PLACE_BLOCK, REMOVE_BLOCK, SPAWN_ITEM, SPAWN_ENTITY, TELEPORT; changes are made as the owner, so protection mods apply (not yet: changes are made directly, see concept 12). | ✅ |
| 10.4 | YAML export of a sheet (`core`: line model → YAML; mod: sheet → line model). | ✅ |
| 10.5 | YAML import: layout algorithm in `core` (indentation, BEGIN/END, continuation lines), placement in the world, consumption of blocks in survival mode. | ✅ |
| 10.6 | c0de disk item with its dialogs. | ✅ |
| 10.6.1 | Uninitialized variable and constant items (crafted from a data item and dye) as targets of results without a parameter; trash can button in the item dialog, saving empty fields makes an item uninitialized. | ✅ |
| 10.6.2 | A process takes the items it uses from the input inventory and passes all other initialized items through to the output; unneeded empty variable items stay. It starts as long as the output inventory has space for its items. The output goes into an adjacent trigger block (chaining). | ✅ |
| 10.6.3 | Trigger blocks as containers for hoppers, pipes and cables (input from all sides, output to all sides; NeoForge item handler capability), so trigger blocks can be chained. | ✅ |
| 10.7 | Round-trip tests: every example program of the test suite survives export and import unchanged. | ✅ |

## Sprint 11: Release 0.1.0 for Minecraft 1.21.1

**Goal:** the first alpha of the mod (version 0.1.0) is published for Fabric and NeoForge 1.21.1.

| Task | Description | Done |
|---|---|:---:|
| 11.1 | Final textures and sounds, review of all block and item looks. | ✅ |
| 11.1.1 | The text marker is the feather of Minecraft, colored yellow by an item color. | ✅ |
| 11.1.2 | Right click of the hammer: inserts an empty line, the clicked line and the following lines move south. | ✅ |
| 11.1.3 | Right click of the text marker: marks or unmarks all lines from the line of the last left click to the clicked line, as the left click did. | ✅ |
| 11.2 | Block shapes: the PARAMETER block has the shape of a hopper (recolored), the RESULT block the same shape upside down; BEGIN and END are trapezoids with diagonal sides, lying on their trapezoid side (a model that moves the vertices of the baked cube). | ✅ |
| 11.2.1 | All language blocks are as high as a slab, with their symbol only on the top. PARAMETER and RESULT are hoppers at half their size. | ✅ |
| 11.2.2 | One color per block (the former invalid color); the line numbers show the state of a line instead. | ✅ |
| 11.2.3 | NOOP: the metal corners are as wide as the wooden edges. | ✅ |
| 11.2.4 | PROGRAM: round in the west, square in the east, without symbol; LITERAL: octagon (models extruded from a polygon in code, which also make BEGIN and END). | ✅ |
| 11.2.5 | OPERATOR: the symbol lying on the floor, extruded; the operator is mirrored into the block state operator, one model per symbol. | ✅ |
| 11.2.6 | VARIABLE: a blue chest, CONSTANT: a blue barrel, both as high as a slab. | ✅ |
| 11.2.7 | The symbols of FOR, EACH, WHILE and REPEAT in lime; a thin circular arrow and a sharp # that don't touch. | ✅ |
| 11.2.8 | PROGRAM in the gray of FUNCTION with a book on the top and INCLUDE with a bookshelf, as in invisible item frames; ASSIGN a triangle, IF and ELSE diamonds with lime symbols, COMMENT two extruded green slashes; the OPERATOR item shows the question mark; the BEGIN and END items are swapped. | ✅ |
| 11.2.9 | INCLUDE lines start at x=5, like the other global statements. | ✅ |
| 11.2.10 | Label dialog: a scrollable list of the choices, filtered by the entered name, instead of six buttons. | ✅ |
| 11.2.11 | Item dialog: a dropdown list of the types below the type field; dialogs with slightly rounded corners. | ✅ |
| 11.2.12 | Shift-right-clicking a PID block with the debugging tool aborts its process (PROCESS_ABORTED, 206). | ✅ |
| 11.2.13 | A triangle marks the type field as a dropdown; the borders of the tops of PROGRAM, LITERAL, ASSIGN, IF, ELSE, BEGIN and END follow their shapes; the items of the language blocks are turned by 180 degrees in the inventory. | ✅ |
| 11.2.14 | The borders of the shaped tops include the pixels cut by round and diagonal sides (two pixels wide there); the loop blocks FOR, EACH, WHILE and REPEAT are cylinders. | ✅ |
| 11.2.15 | The symbols of all blocks are raised half a pixel above the top, like the book of PROGRAM; the type blocks carry the item they are crafted from; the symbol of CALL is drawn by hand (scripts/hand_drawn). | ✅ |
| 11.2.16 | The hammer deletes empty lines too; left-clicking a cursor with the debugging tool shows its line in the chat, wrong lines in red. | ✅ |
| 11.2.17 | The variable item has no lines on its card. | ✅ |
| 11.2.18 | The gray pixels of the CALL symbol are raised like its black and white pixels. | ✅ |
| 11.2.19 | VARIABLE is a plain blue chest, without the dark blue lines. | ✅ |
| 11.2.20 | A CHAR literal can be a single space. | ✅ |
| 11.2.21 | A wrong line is red even if an incomplete statement above it would make it yellow. | ✅ |
| 11.3 | Line numbers: every line with content shows its number on the floor at x=2. The color shows the state of the line: a faint shade of the floor for NOOP and COMMENT lines, dark red for a wrong line, dark yellow for an incomplete statement (e.g. BEGIN without END, FUNCTION without code block), dark green for a correct line. | ✅ |
| 11.4 | Player documentation: a guide with examples (Markdown in the repository and the Modrinth page); optionally an in-game book. | ✅ |
| 11.4.1 | In-game guide book for Patchouli, generated from the guide: craftable from a book and a PROGRAM block if Patchouli is installed; every player gets it on the first join. CALL shows a black telephone receiver. | ✅ |
| 11.4.2 | Handbooks in docs/reference (data, statements, tools, triggers, functions) with one page per element (description, recipe, example), recipe images (scripts/generate_recipe_images.py) and a generated index; the same handbooks as red Patchouli books. | ✅ |
| 11.4.3 | The concept of the functions matches the code; INT, FIX and ROUND return INTEGERs unchanged. The handbooks are described in the guide and on Modrinth. | ✅ |
| 11.4.4 | Documentation for mods that add c0de libraries (docs/library-development.md) and the API ModLibraries.register. | ✅ |
| 11.5 | PRINT, WRITE and INPUT: keyword blocks and statements; the screen functions of AmigaBASIC (CLS, LOCATE, CSRLIN, POS, COLOR, INKEY). The screen model in `core`: the screens of the displays, laptops and tablets bound to the program and function of a process. | ✅ |
| 11.5.1 | PRINT, WRITE and INPUT are built-in functions used with CALL instead of statements and keyword blocks. | ✅ |
| 11.6 | Display block: flat multiblock walls that share one screen (fixed characters per block, Monocraft font), rotation with the wrench, camouflage, binding to a program and function like a trigger block. | ✅ |
| 11.6.1 | Display dialog like that of the trigger block: light gray, program and function buttons, one camouflage slot. | ✅ |
| 11.6.2 | The camouflage doesn't mix with the front of a display block from far away. | ✅ |
| 11.7 | I/O block (a retextured hopper): binding, input and output inventories; INPUT reads variable, constant and literal items, WRITE fills uninitialized variable items; literal items (a card with a type and a value, without name). | ✅ |
| 11.7.1 | I/O dialog like that of the trigger block, with program and function buttons; 18 slots in each inventory. | ✅ |
| 11.8 | Laptop block: a terminal with the output of the bound program and function, a scroll bar and an input line for INPUT. | ✅ |
| 11.8.1 | ✔ in the terminal binds and closes it; the wrench turns laptops. | ✅ |
| 11.8.2 | Lines typed on a terminal reach a waiting INPUT reliably. | ✅ |
| 11.8.3 | Process state WAITING while INPUT or WRITE waits; the cursor pulses between black and dark green (debug cursor: dark red), at most #006600 and #660000. | ✅ |
| 11.8.4 | GET_BLOCK of c0de/minecraft also returns has_inventory; the VARIABLE chest keeps the seam between lid and body. | ✅ |
| 11.9 | Tablet: the mobile laptop; left click opens the terminal. Recipes between laptop and tablet. | ✅ |
| 11.10 | Performance tests: many processes at the same time, large programs, deep recursion; tuning of the scheduler. | ✅ |
| 11.11 | Multiplayer tests on the Docker servers with several clients (development clients and Modrinth App profiles), including non-operator players and permissions. | ✅ |
| 11.11.1 | Sheet owners: only the player who built on an empty sheet first and administrators can change it; administrator rights only count for programs on sheets of administrators. Patchouli on the test servers; a second development client (`runClient2` as Tester2; `runClient` as Tester1). | ✅ |
| 11.11.2 | Administrators also become owners of the sheets they build on; the COMMENT item is turned like the other language block items. |  |
| 11.11.3 | Redeploying the test servers backs up the saved programs and the operators and restores them. | ✅ |
| 11.11.4 | Patchouli stays optional: development runs without it (Fabric: `-Ppatchouli=false`; NeoForge: only the first client gets it in its mods folder) and a NeoForge test server without it. | ✅ |
| 11.11.5 | LABEL blocks show the index of the element they refer to. | ✅ |
| 11.11.6 | The debugging tool shows the owner of a sheet on PROGRAM and FUNCTION blocks and the owner of a process on PID blocks. | ✅ |
| 11.11.7 | The floor block at x=0, z=0 of a sheet shows its number, coordinates and owner; the wrench on that text gives a sheet to another player (owners their own sheets, administrators every sheet). | ✅ |
| 11.11.8 | Trigger property Run (Only me, Players, Mobs, Everyone): who may start it, identified by the button, lever or pressure plate touching it; restricted functions are fatal errors for processes started by players who are not administrators. | ✅ |
| 11.11.9 | Disk drive block: the c0de disk carries a program; the drive saves it to the folder `.minecraft/c0de` of the client and loads programs from there onto the disk. Right-clicking a PROGRAM block copies its program onto the disk, right-clicking the start of an empty sheet builds it. | ✅ |
| 11.11.10 | The COMMENT item reads // in the inventory; the wrench gives a sheet away on the floor that shows its information; the trigger dialog is read-only (greyed out) for players who may not change it; on NeoForge only the first development client gets Patchouli. | ✅ |
| 11.11.11 | Trigger dialog: the camouflage slot below the configuration with its caption to the right, ✔ in the lower right corner, the messages in a box with a scroll bar. | ✅ |
| 11.12 | Workflow `release.yml`, Modrinth project, changelog. | ✅ |
| 11.12.1 | `release.yml`, `publishMods` (Mod Publish Plugin 2.2.1, dry run without token) and `CHANGELOG.md`. | ✅ |
| 11.12.2 | Modrinth project created, its ID in `publish.modrinth_id` and the secret `MODRINTH_TOKEN` (by the maintainer). | ✅ |
| 11.12.3 | Logo: `docs/images/logo.png` (256 × 256, cropped from the picture of the maintainer) and `docs/images/logo.svg` (`scripts/generate_logo.py`: the voxels of the COMMENT model in a projection like the picture), also the icon of the mod. | ✅ |
| 11.13 | Release 0.1.0 (first alpha). | ✅ |

## Sprint 12: Newer Minecraft versions

**Goal:** the mod is available for all newer Minecraft releases.

| Task | Description | Done |
|---|---|:---:|
| 12.1 | Add the node for the last 1.21.x release (1.21.11) and port the API changes (e.g. the reworked recipe system of 1.21.2, item model definitions of 1.21.4, GUI rendering changes). | ✅ |
| 12.1.1 | Server side: NBT getters with `Optional`, `ValueInput`/`ValueOutput`, `SavedDataType`, block removal hooks, interaction results, permissions, teleports, chunk tickets (`compat` package). | ✅ |
| 12.1.2 | Client side: input events of screens, GUI strata, block entity and entity renderers with render states (`Canvas`), extruded models as unbaked geometry, dimension effects as environment attributes. | ✅ |
| 12.1.3 | Registration with IDs in the block and item properties; item definitions (`assets/c0de/items`). | ✅ |
| 12.1.4 | Data whose format changed (recipes, biome) in `src/main/versioned`; GameTests with the Fabric annotation of 1.21.5. All 44 GameTests pass on 1.21.1 and 1.21.11 (Fabric). | ✅ |
| 12.1.5 | NeoForge 1.21.11: platform calls, client registration, model loader `c0de:extruded`, GameTest registration. All GameTests pass on NeoForge 1.21.1 and 1.21.11. | ✅ |
| 12.1.6 | Item textures of Minecraft in block models are added to the blocks atlas (separate items atlas since 1.21.11); separate run directories per version. | ✅ |
| 12.2 | Add the nodes for 26.x (unobfuscated, Java 25, Gradle 9.1+) and port the API changes. | ✅ |
| 12.2.1 | Node 26.1.2: GUI renamed to extract methods (replacements and conditional headers), moved classes, messages to players, chunk positions, items, edit boxes, saved data IDs, Fabric menu and networking APIs, NeoForge events; access widener in the namespace "official". All GameTests pass on both loaders. | ✅ |
| 12.2.2 | Node 26.2: screens and light through `ClientCompat`, no buffers any more (text colors of the displays as a table, line numbers through the submit events). All GameTests pass on both loaders. | ✅ |
| 12.2.3 | Node 26.3: key codes from `InputConstants` (no GLFW), rotations as matrices, no block codecs, chunk generation in one step, pack resources, items to players and inventories through `Compat`; the recipe category `equipment` instead of the invalid `tools`. All GameTests pass on both loaders. | ✅ |
| 12.3 | Extend the CI matrix and the Docker test servers to all nodes. | ✅ |
| 12.3.1 | GameTests and smoke tests on all nodes in GitHub Actions (one job per node); test servers for all nodes in `compose.yaml`; `smoke.sh` and `deploy.sh` only touch the servers they work on; the smoke test starts one server after the other. All nodes pass in GitHub Actions and on the VM. | ✅ |
| 12.4 | Replace HeadlessMC by Fabric client GameTests where available. | ✅ |
| 12.4.1 | Client GameTests on Fabric 1.21.11 and 26.x: light of the c0de dimension, textures of the blocks, trigger dialog, terminal; in GitHub Actions in a virtual display with software rendering, screenshots as artifacts. | ✅ |
| 12.5 | Release for all versions; document the process for adding future Minecraft versions. | ✅ |
| 12.5.1 | Release workflow for all nodes; Patchouli only as optional dependency where it was tested (1.21.1); version 0.2.0, released on 2026-10-26 (GitHub pre-release and ten Modrinth versions); the steps to add a Minecraft version in `docs/development_environment.md`. | ✅ |

After sprint 12, every new Minecraft release is added as a new node in its own small sprint.

## Sprint 13: Usability, process information and colors

**Goal:** feedback from playing 0.2.0: timers can be stopped while their program is changed, the disk drive shows its messages completely and sounds like a floppy drive, programs can read information about their sheet and process, screen colors have names, programs mine and place blocks with the tools and blocks of inventories, boots take players through long programs and pliers indent code. Released as 0.3.0.

| Task | Description | Done |
|---|---|:---:|
| 13.1 | Timer blocks: a redstone signal pauses them reliably, also when the block is placed next to a signal that is already on; the timer dialog shows "Paused by redstone"; GameTest for the pause; the guide and the trigger handbook explain how to stop a timer to change its program. | ✅ |
| 13.2 | Disk drive dialog: a bit larger, the messages (e.g. "Saved as …") in a box with two full lines that no other element covers. | ✅ |
| 13.3 | Disk drive sound: a short floppy drive sound (seek clicks and spinning, generated by a script) when the drive reads or writes a disk; all players near the drive hear it. | ✅ |
| 13.4 | Built-in constant SHEET of the built-in struct type SHEET_INFO: number, x, y, z (of the start of the sheet), owner and program of the sheet the process runs on. Read-only like every constant. | ✅ |
| 13.5 | Built-in constant PID of the built-in struct type PID_INFO: pid, program, function, owner, activator, the position of the trigger block (x, y, z, dimension), debug and the start time in ticks. Kept when the process is restored after a restart. | ✅ |
| 13.6 | Built-in INTEGER constants COLOR_BLACK … COLOR_WHITE with the names of the 16 Minecraft text colors for COLOR; concept, handbook and guide example. | ✅ |
| 13.7 | Built-in constant CURSOR of the built-in struct type CURSOR_INFO: line, function, depth of the calls, used slots and size of the stack, computed whenever it is read. Read-only like every constant. | ✅ |
| 13.8 | The cursor dialog of the debugging tool shows how many slots of the stack are used. | ✅ |
| 13.9 | Error codes INVENTORY_FULL (312) and ITEM_MISSING (313) for library functions that work with inventories. || ✅ |
| 13.10 | MINE_BLOCK of c0de/minecraft: breaks a block with the most suitable tool of an inventory (the first of the best group: tools that obtain the block and are faster, items that obtain it, faster tools, bare hands), damages the tool, waits as long as a player needs, puts the drops into an optional output inventory (INVENTORY_FULL if they don't fit) or drops them. Not restricted. GameTest. || ✅ |
| 13.11 | PLACE_BLOCK of c0de/minecraft: optional parameter `inventory` that the block is taken from (ITEM_MISSING if it isn't there); with it, PLACE_BLOCK is not restricted. GameTest. || ✅ |
| 13.12 | Boots: a left click with any boots on the FUNCTION block of a FUNCTION line teleports the player next to the END FUNCTION line of the function and back; a right click on a FUNCTION block teleports them next to the PROGRAM line. Unit tests and GameTest. || ✅ |
| 13.13 | Pliers (shapeless from shears and a stick): a left click moves a line, or the marked area that contains it, one block to the left, a right click to the right; nothing moves if a block would leave the code area. Unit tests and GameTest. || ✅ |
| 13.14 | Release 0.3.0 for all Minecraft versions, released on 2026-10-28 (GitHub pre-release and ten Modrinth versions, environment "client and server"). | ✅ |

## Sprint 14: Documentation

**Goal:** the documentation reaches players who don't read English and works on the website c0de.at: the guide and the handbooks are translated into German, the programs of the tutorials on c0de.at are tested with every release of c0de, and the website can show programs as 3D models. Planned on 2026-11-11 from the tutorial plan of www-c0de (`docs/tutorials.md` there).

| Task | Description | Done |
|---|---|:---:|
| 14.1 | Program runner: a command-line entry point of `core` that reads a program in the text notation of the guide, validates it, runs a function with parameters from a YAML file and prints the results and the error as YAML. Programs that include c0de/minecraft are validated only, like in GuideExamplesTest; other libraries (c0de net) are added to the class path. Unit tests. | ✅ |
| 14.2 | The release attaches the runner as `c0de-runner-<version>.jar` to the GitHub release, so the CI of www-c0de can test the programs of its tutorials against the tag in its `sources.yaml` (a program file with an `.expected.yaml` of its results). | ✅ |
| 14.3 | German translation of the guide: `docs/de/guide/*.md` with the same file names and the same `##` and `###` headings in the same order as the English chapters (the import of c0de.at matches sections by position). Names in programs stay English, comments and texts are translated. | ✅ |
| 14.4 | German translation of the handbooks: `docs/de/reference/*.md`, with the same rules; the images are shared with the English files. | ✅ |
| 14.5 | Tests of the translations: GuideExamplesTest also validates the programs of `docs/de`, and a test checks that every translated file has the headings of its English original in the same order and levels. | ✅ |
| 14.6 | The Patchouli books in German: generate_book.py also writes the `de_de` pages from `docs/de`, so the books follow the language of the game. | ✅ |
| 14.7 | 3D models of all blocks: a script exports every block of the mod (language blocks, type blocks, devices, triggers, the floor of a sheet with the cursor rail and the line numbers) with its textures as glTF 2.0, including the shapes that ExtrudedModel computes. An index file maps the block ids to the files. A test checks that every registered block has a model. | ✅ |
| 14.7.1 | The runner prints the layout of a program (`--layout`): every line with the state of its line number and its blocks with their position, block ID, block state and text, so the website places the models without parsing the notation itself. | ✅ |
| 14.8 | The release attaches the models as `c0de-models-<version>.zip`; www-c0de builds 3D views of example programs from them with `<model-viewer>`, with screenshots as a fallback. | ✅ |
| 14.9 | Built-in STRING constant C0DE_VERSION with the version of the installed mod (e.g. "0.4.0"), read-only like every constant, also in the program runner. Other mods can register built-in constants of their own through the library API (for C0DE_NET_VERSION of c0de net). Concept, handbook and guide; unit tests and GameTest. Needed by the tutorial recipe "Is my c0de up to date?" of c0de.at. | ✅ |
| 14.10 | Release with the translations, the runner, the models and C0DE_VERSION for all Minecraft versions, released on 2026-11-11 as 0.4.0 (GitHub pre-release with c0de-runner-0.4.0.jar and c0de-models-0.4.0.zip, Modrinth). | ✅ |

## Sprint 15: Feedback from the tutorials

**Goal:** the programs of the tutorials of c0de.at (www-c0de sprint 6), tested with the program runner of 0.4.0, found places where the implementation differs from the concept. Planned on 2026-11-11; started on 2026-11-12, released as 0.5.0 at its end.

| Task | Description | Done |
|---|---|:---:|
| 15.1 | PRINT writes BOOLEAN values as `TRUE` and `FALSE`, as the concept (19-input-output.md) and the handbook say; today it writes `true` and `false` like `⊕` (whose `true`/`false` stays, concept 05-expressions.md). Unit test. | ✅ |
| 15.2 | An error with a code below 1000 that a function returns with `RETURN` raises INVALID_ARGUMENT, like RAISE does; the concept says user-defined codes MUST be 1000 or higher (04-data.md), but only RAISE checks it today. Unit test. Errors received from a call (with a `context`) can still be passed on with their codes. | ✅ |
| 15.3 | The program runner knows c0de/minecraft without Minecraft: its struct types (POSITION, ENTITY, …) and its functions with their parameters and results, so programs that use it are validated completely (today the runner only ignores UNKNOWN_LIBRARY and UNKNOWN_FUNCTION and reports UNKNOWN_STRUCT_ENTITY for every variable of these types). Calling a function of the library in the runner raises an error that says it needs the game. The definitions move from the mod into `core`, so the mod and the runner share them. Unit tests. The definitions are in `core` (`MinecraftLibraryDefinition`); the mod adds its implementations. | ✅ |
| 15.4 | The runner reports the problems of a program sorted by line, so expected files can name them (today their order changes from run to run). Each problem once (15.8), sorted by line, code and message. | ✅ |
| 15.5 | A JSON Schema of the YAML format (17-yaml-format.md) of the files the disk drive saves: the top-level keys, every statement object with its properties, the nesting of `code`. Published with the release and referenced from the concept, so editors can check hand-written files. Unit test: the files the export writes match it. `core/src/main/resources/de/joernott/c0de/core/yaml/c0de-program.schema.json` (draft 2020-12), attached to the release; concept 17-yaml-format.md "Schema". | ✅ |
| 15.6 | Loading a file from disk (the disk drive and the program runner) validates it against the schema of 15.5 before it is read; a file that doesn't match is refused with a message that names the first problem and its path in the file. Unit tests with invalid files. `ProgramSchema` checks with the keywords the schema uses, without a new library; the program runner reads the text notation, not YAML files, so the check covers the disk drive, the server and the building of disks. | ✅ |
| 15.7 | Program names are checked wherever a program crosses the disk: copying it onto a disk, saving and loading a file, and on the server when a loaded program arrives. A name MUST be a valid name (02-language-basics.md: letters, digits and `_`, starting with a letter, not a keyword); everything else is refused, in particular path separators, `.`/`..`, control characters, invisible and bidirectional characters (zero-width spaces, right-to-left overrides) and look-alike letters of other scripts, which can hide the real name or reach outside the folder `c0de` (after the incident with malicious programs of third parties that manipulated Litematica). Today `LocalPrograms.file()` also allows `-` and a leading digit or `_`. The resolved file MUST stay inside the folder `c0de`. The names of disks from older versions that aren't valid any more are reported, not used. Unit tests with hostile names. `Names.isValid` everywhere, and the file must lie directly in the folder; messages show hidden characters as `\uXXXX`; old disks are flagged in their tooltip; GameTest in DiskTests. | ✅ |
| 15.8 | The program runner, found by the recipes of c0de.at (www-c0de sprint 9): it runs the functions of a program that includes c0de/minecraft as long as they don't call the library (today it only validates such programs, so the recipes repeat their pure functions in programs of their own); it reports every problem once (a parameter and a result with the same name gave the same SYNTAX_ERROR_FUNCTION about 36 times); runner.md describes the format of `input:` (YAML values, structs as maps, unlike the laptop, which reads JSON) and whether typed lines appear in `screen`. When 15.3 and this task are released, c0de.at changes the expected files of its programs from `valid: false` to `valid: true`. Unit tests. The runner part is done with 15.3 and 15.4; c0de.at changes its expected files after the release. | ✅ |
| 15.9 | The guide and the reference say what the recipes of c0de.at had to find out by trying: an index is a literal or a variable, not an expression (`list[i - 1]`); there is no minus sign in front of a name (`0 - x`); CONSTANT takes only a literal; a parameter and a result can't have the same name; `\` keeps the sign of the left number; dividing FLOATs by zero raises DIVISION_BY_ZERO; whether AND binds tighter than OR; how to write a quote in a string literal (the handbook of c0de net uses `\"`); whether an ERROR variable goes back to code 0 after a CALL without error; whether the errors of libraries (PERMISSION_DENIED, HTTP_STATUS, …) are constants like DIVISION_BY_ZERO; TIME values can be compared and `⊕` writes them as ISO text in UTC; a comparator also reads a timer block; whether results come out after EXIT; switching a lever off doesn't start a trigger block; whether a laptop shows the lines typed for INPUT. A minus in front of a name does work (`- x`, an OPERATOR before the LABEL); only `-x` as one token of the text notation doesn't. | ✅ |
| 15.10 | The handbooks of the libraries, found by the recipes of c0de.at: PI gets a page of its own; FORMAT says that one value can feed several conversions, that the output is English (`,` as thousands separator, weekday names) and that `%f` takes an INTEGER; TO_FLOAT says when it is needed; TICKS is the total game time of the world, not the time of day (sleeping and `/time set` don't change it), with a function for the time of day or a note how to get it; WEEK, TIMER and the `%t` codes use the time zone of the server, TIMER starts at 0 at midnight, WEEK has no week-based year; POSITION, ENTITY and ITEM_STACK are described in one place with the types of their elements and whether positions are rounded to the block; every function says what an empty `dimension` means; FIND_ENTITIES says whether it searches a sphere or a cube; `PID.activator` says what it holds when redstone or a timer started the process; MINE_BLOCK on air, TELEPORT into an unloaded chunk or another dimension; the instruments of SOUND as a full list; the exact format of SAY; FROM_JSON says when it raises UNKNOWN_KEY (a tag whose path is missing) and when a missing key keeps the value. | ✅ |
| 15.11 | The release publishes the mod on CurseForge as well as on Modrinth: the `curseforge {}` block of the mod-publish-plugin in both builds (Fabric and NeoForge), with `publish.curseforge_id` in `stonecutter.properties.toml` next to `publish.modrinth_id`, the same files, versions, changelog and release type, required on client and server, Fabric API required and Patchouli optional as on Modrinth; `release.yml` needs the repository secret `CURSEFORGE_TOKEN` and fails without it or the project ID, like for Modrinth; `development_environment.md` describes the secret. The Minecraft versions must be known to CurseForge (26.x included). Checked with a dry run before the next release. Needs from the owner: the CurseForge project (approved) with its ID and the API token as a secret. Done on 2026-11-12: 0.4.0 is on CurseForge (10 files, uploaded with curseforge.yml). | ✅ |
| 15.12 | Release 0.5.0 for all Minecraft versions (GitHub pre-release with the runner, the models and the schema; Modrinth and CurseForge). Checked before on the VM: build, GameTests of both loaders, the test servers with the new jars. The first tag run failed before publishing, because publishing no longer built the jars (fixed in 5d9b304); the tag was pushed again and released on 2026-11-12. | ✅ |

## Sprint 16: Security

**Goal:** programs can use passwords and API keys without showing them: INPUT hides what is typed on laptops and tablets, items can hide their value, the tag `security: sensitive` hides values in the debug output, and every player has a vault of secrets, which programs read with the new SECRET statement and change with the new library c0de/security. Secrets can be kept in a KeePass file (KDBX 4) per player, which the server stores but only the client of the player decrypts (with hudeany/kdbx). Concept: [Security and the c0de/security library](concept/20-lib-security.md). Planned on 2026-11-15; version 0.6.0. Extended on 2026-11-15 before the release (tasks 16.10–16.15): the whole data (NBT) of blocks in GET_BLOCK and PLACE_BLOCK, MOVE_ITEMS for every player with quantities, filters and a duration, COMMAND like a command block, and regular expressions in c0de/strings. Extended again on 2026-11-15 (tasks 16.17–16.25): handbooks embedded in c0de instead of Patchouli, based on Lavender and lavender-md, as seven red binders.

| Task | Description | Done |
|---|---|:---:|
| 16.1 | Concept: the chapter [Security and the c0de/security library](concept/20-lib-security.md) and the references to it (keywords, tags, error codes 314–321, INPUT, items). Decided on 2026-11-15: a process uses the vault of its activator, and only if that player is online and owns the sheet; the server stores the KDBX file, the client decrypts it; SECRET is a declaration like CONSTANT; only items (not LITERAL blocks) can be hidden. | ✅ |
| 16.2 | The SECRET statement in `core`: notation `SECRET name TYPE`, parsing, validation (simple types and STRING, unique name, no tags or literal, not assignable), the value from a vault interface of the process context when the line runs (SECRET_NOT_FOUND, TYPE_MISMATCH, VAULT_UNAVAILABLE). The SECRET block with model, texture, recipes (from and back to CONSTANT), the YAML format (17-yaml-format.md) and its JSON Schema, the block reference (18-block-reference.md), the c0de disk.  | ✅ |
| 16.3 | Sensitive values: the tag language `security` with the only name `sensitive` (TAG dialog, validation, YAML), on VARIABLE and CONSTANT lines and struct elements; SECRET values and parameters from hidden items are sensitive. The debug output shows `***` for them: the stack in the cursor dialog, the debugging tool in the chat, the error context of trigger blocks, the report of the runner.  | ✅ |
| 16.4 | Hidden input: the parameter `hide` (BOOLEAN, default FALSE) of INPUT; the input line of laptops and tablets shows `*` per character, and the typed line is printed as `***`.  | ✅ |
| 16.5 | Hidden items: the checkbox Hide in the dialogs of variable, constant and literal items (the literal item dialog gets the trash can button); a hidden value shows `***` in the dialog and the tooltip and is only removed with the trash can; hidden items don't stack with others. WRITE of a sensitive value and sensitive results of a process produce hidden variable items.  | ✅ |
| 16.6 | The vault on the server: per player in memory, created on join, dropped on leave; available to a process only if the activator is an online player who owns the sheet. The library c0de/security with SET_SECRET, DELETE_SECRET and HAS_SECRET (non-persistent), the limit of 256 secrets, and the error codes 314–321 as built-in constants. GameTests.  | ✅ |
| 16.7 | The persistent vault: hudeany/kdbx (`de.soderer:kdbx`) and Bouncy Castle (`bcprov-jdk18on`) in the jar of both loaders, used only by the client; the file `<world>/data/c0de_vaults/<uuid>.kdbx` on the server; network payloads to send the file to its player, the decrypted secrets to the server and the changed file back; CREATE_VAULT, OPEN_VAULT, HAS_VAULT, IS_VAULT_OPEN and persistent SET_SECRET/DELETE_SECRET with the cursor waiting for the client; the password dialogs (with Repeat for CREATE_VAULT, Cancel, a new try after a wrong password); the KeePass layout (group `c0de`, title, password, custom field `c0de-type`). A file written by c0de opens in KeePassXC. Tests of the client side and GameTests with a fake client. Checked on 2026-11-15: a file written by c0de opens in keepassxc-cli 2.7 with its secrets; GameTests answer with a stand-in client that uses the same library. The password dialog itself is tried in the game in 16.16. | ✅ |
| 16.8 | The program runner: the `vault` of the options file (secrets, persistent vault with password), c0de/security and SECRET on it, VAULT_UNAVAILABLE without it, `***` for sensitive values in the report; docs/runner.md.  | ✅ |
| 16.9 | Documentation: a guide chapter about secrets, the handbook of c0de/security in English and German, the reference (statements, data, functions, devices, items), the changelog.  | ✅ |
| 16.10 | Concept of the additions: GET_BLOCK, PLACE_BLOCK, MOVE_ITEMS and COMMAND in [12-lib-minecraft.md](concept/12-lib-minecraft.md), REGEX_MATCH and REGEX_REPLACE in [10-lib-strings.md](concept/10-lib-strings.md), the new settings in [13-running-programs.md](concept/13-running-programs.md). Decided on 2026-11-15: MOVE_ITEMS gets the new behaviour (with `count` and `id` as before, or `filter` instead of `id`; changed on 2026-11-15 from `quantity` and `filter` only), one stack per tick travels 128 blocks, `p` prints the result like sed, `data` of PLACE_BLOCK needs allowBlockChanges (blocks whose data only operators may set, like command blocks, need an administrator). | ✅ |
| 16.11 | Block data: the result `data` of GET_BLOCK (the NBT of the block entity as SNBT, without position and ID) and the parameter `data` of PLACE_BLOCK (INVALID_ARGUMENT for text that isn't SNBT of a compound, for blocks without block entity and for data the block refuses), on all Minecraft versions. GameTests. | ✅ |
| 16.12 | MOVE_ITEMS for every player: parameters `count` (now optional, default 1, -1 for all), `id` or `filter` (STRING or ARRAY STRING: item IDs or search expressions like JEI: words, `@mod`, `$tag`, `&id`, `-` and `\|`), only between inventories in one dimension that the owner could open (no locked containers, spawn protection); the cursor waits one tick per stack per 128 blocks (settings `moveItemsStacksPerTick`, `moveItemsBlocksPerTick`); the setting `enableMoveItems`. The filter parser in core with unit tests, GameTests. | ✅ |
| 16.13 | COMMAND: runs a command like a command block at the trigger block (results: the success count and the output); only for administrators as owner and activator, only if command blocks are enabled on the server, and the setting `enableCommand`. GameTests. | ✅ |
| 16.14 | REGEX_MATCH and REGEX_REPLACE in c0de/strings: Java regular expressions (the PCRE syntax without recursion, conditionals and verbs), replacements with `\1` to `\9` and `&` like sed, the flags `g`, a number, `i` and `p` (PRINT if something was replaced); patterns that take too long raise INVALID_ARGUMENT (more than 1,000,000 reads of the text, or 10 per character of long texts). Unit tests, the program runner. | ✅ |
| 16.15 | Documentation of 16.11–16.14: guide, handbooks in English and German, Patchouli, changelog. | ✅ |
| 16.15.1 | Found on 2026-11-15: the edit scripts of tasks 16.10 and 16.15 used JavaScript's `replace` with replacements containing `$` + backtick, which inserts the text before the match; `docs/concept/10-lib-strings.md`, `docs/reference/functions.md`, its German translation and chapter 5 of the guide (English and German) got copies of their beginnings (up to six), and so did the reference index and the Functions handbook. Repaired by replaying the scripts with a function as replacement on the state before the damage and applying the later changes; only the copies are gone and `$` + backtick is back. | ✅ |
| 16.16 | Release 0.6.0 for all Minecraft versions (GitHub pre-release with the runner, the models and the schema; Modrinth and CurseForge). Checked before on the VM: build, GameTests of both loaders, the test servers with a persistent vault across leaving and joining again, c0de net 0.3.0 with 0.6.0.  Released on 2026-11-15 (the program runner job was rerun after Maven Central failed to deliver junit-bom). | ✅ |
| 16.16.1 | Version 0.6.0 in stonecutter.properties.toml, so the development clients and the VM test the version that will be released. | ✅ |
| 16.16.2 | Found on the VM on 2026-11-15: the jar of the active node (1.21.1-fabric) reported C0DE_VERSION 0.1.0 in every release, because Stonecutter writes the swap `mod_version` only into the generated sources of the other nodes. C0de.java refreshed, `scripts/check-version.sh` in CI, a step in the release checklist. | ✅ |
| 16.16.3 | Found in the in-game test on 2026-11-15: the language button of the TAG dialog shows only the language, without "Language:". | ✅ |
| 16.16.4 | Requested on 2026-11-15: c0de is published as beta from 0.6.0 on (Modrinth and CurseForge; alpha before). | ✅ |
| 16.17 | Concept: the chapter [Handbooks](concept/21-handbooks.md) and the references to it. Decided on 2026-11-15 (before the release, part of 0.6.0): the books are embedded instead of Patchouli, lavender-md is embedded too, and every book is a red binder of its own; the label colours as proposed. | ✅ |
| 16.18 | lavender-md in c0de: the core of lavender-md (branch 26.1, MIT) in `de.joernott.c0de.client.handbook.md`, with its licence notice in every file and `NOTICE.md`; Stonecutter conditions for 1.21.1–26.3 (ResourceLocation and Identifier, click and hover events). | ✅ |
| 16.19 | The c0de features and the book compiler: code, program examples, recipes, item icons, page breaks; a compiler that turns the Markdown into page elements (lines of text, images, recipe grids, item icons) and distributes them over pages. | ✅ |
| 16.20 | Loading the content on the client: books, categories and entries with their JSON metadata, languages with English as the fallback, `recipes.json`; reloaded with the resources. | ✅ |
| 16.21 | The book screen: landing page, categories, entries, page arrows, back arrow, links, keyboard, sounds, the remembered page per book; the red book texture of Lavender; rendering for all Minecraft versions. | ✅ |
| 16.22 | The seven binder items: textures (a red binder, the colour of the label per book), models, names in English and German, the creative tab, the recipes without the Patchouli condition, the guide on joining (tag `c0de.handbook_given`). | ✅ |
| 16.23 | The generator writes the books as Markdown and `recipes.json` (links between entries, program examples, recipes); Patchouli is removed (books, recipe conditions, development dependency, optional dependency on Modrinth and CurseForge). | ✅ |
| 16.24 | Documentation: guide and reference (English and German) without Patchouli, development environment, Modrinth description, changelog. | ✅ |
| 16.25 | Check on the VM (all nodes, GameTests) and in the game on 1.21.1 and 26.3 for both loaders. Done on 2026-11-15; the findings are 16.25.1 to 16.25.5. | ✅ |
| 16.25.1 | Found in the in-game test on 2026-11-15: long titles wrap (above entries and in the lists of categories and entries), program examples have the colours of c0de.at, and the guide shows how to craft the other handbooks (Tools, Handbooks, linked from its landing page). | ✅ |
| 16.25.2 | Requested on 2026-11-15: the book takes about 80 % of the screen and lays out its pages for that size; it is drawn as an open ring binder with a small gap and four metal rings instead of the binding of Lavender's texture. | ✅ |
| 16.25.3 | Requested on 2026-11-15: every entry of the guide ends with the recipes of the c0de items and blocks it names, and the guide's references to its other pages are links (in the documentation and in the book). | ✅ |
| 16.25.4 | Requested on 2026-11-15: more links in the guide. `scripts/guide_links.py` links the first mention of a term in each section (tools, items, trigger blocks, keywords, libraries and others) to the section that explains it: 168 links in English, 135 in German. | ✅ |
| 16.25.5 | Requested on 2026-11-15: crafting grids and program examples in the colours of the sheets (light brown lines, green and white rows and stripes). | ✅ |

## Sprint 17: Handbooks of other mods

**Goal:** other mods, like c0de net, ship complete handbooks without anything in c0de being changed for them: their books, binders, crafting grids, translations and links between books are resources and data of their own jars. The errors in the documentation of 0.6.0 that the recipes of c0de.at found (www-c0de sprint 13) are fixed. Planned on 2026-11-15; version 0.6.1.

| Task | Description | Done |
|---|---|:---:|
| 17.1 | Concept: handbooks of other mods in [Handbooks](concept/21-handbooks.md). Today only the namespace `c0de` is read, every binder is an item of c0de, and the crafting grids come from a single `recipes.json`, which a second mod would replace. To decide: books in every namespace (`assets/<namespace>/handbook/books/<book>.json`, book IDs like `c0de_net:net`); one generic binder item with a data component that names the book, so other mods need no item of their own (the seven binders of c0de stay, players have them); the label colour of the binder from the book definition (item tint on 1.21.1, a tint source of the item model since 1.21.4); the recipe of a binder as an ordinary recipe of the other mod with the component in its result; crafting grids of every namespace merged (`handbook/recipes.json` of all mods, or one file per book); links between books (`^namespace:book/entry`); whether a book is given on joining; where the binders of other mods appear in the creative tab.  Decided on 2026-11-15: see [Handbooks of other mods](concept/21-handbooks.md#handbooks-of-other-mods): books in every namespace, one generic binder `c0de:handbook` whose custom data names the book and the colour of its label, crafting grids of all namespaces merged, links between books; no gift on joining for the books of other mods. | ✅ |
| 17.2 | The content of all namespaces: books, categories and entries of every mod, the crafting grids of all mods merged, links into other books. Unit-tested loader, GameTest or client GameTest with a book of a test namespace.  Done on 2026-11-15; the client GameTest follows a link into another book. | ✅ |
| 17.3 | The generic binder: the item with the book component, its label colour from the book definition, its name from the book, opening the right book; recipes of other mods with the component in the result (1.21.1 and 1.21.2+ format); the creative tab.  Done on 2026-11-15; the client GameTest checks the name of a generic binder. | ✅ |
| 17.4 | Documentation for mod developers: a chapter "Handbooks" in `docs/library-development.md` (the files, the Markdown, the recipe of the binder, the crafting grids, the translations, and how `scripts/generate_book.py` can be used as a template), and the concept. c0de net is the first user (c0de net sprint 8).  Done on 2026-11-15: the chapter [Handbooks](library-development.md#handbooks). | ✅ |
| 17.5 | Documentation errors of 0.6.0 found by the recipes of c0de.at (English and German): the example of REGEX_REPLACE writes `"\3.\2.\1"`, which the text notation reads as `3.2.1` (`"\\3.\\2.\\1"`, reference and concept), with a note that backslashes of patterns are doubled in the text notation and typed once in the dialog; the example of GET_BLOCK shows the result `data`; the vault example of the guide keeps the key in a variable with the tag `security: "sensitive"` and catches VAULT_CANCELLED; the concept calls the choice "Hide the value" (No / Yes, for good) like the game, not a checkbox; COMMAND says what `result` is for queries like `time query` and in which language `output` comes back; the guide warns that a crash also gives an exit code (15) to a comparator; the duration of MOVE_ITEMS is described with the formula of the concept; REMOVE_BLOCK says what happens to the contents of a container; `docs/runner.md` says that `persistent.open: true` puts the persistent secrets into the vault at the start.  Done on 2026-11-15: the corrected example gives `15.11.2026` in the runner; a GameTest shows that REMOVE_BLOCK spills the contents of a chest also without `drop`; COMMAND: `result` counts the successful runs, `output` is in the language of the server. | ✅ |
| 17.6 | Release 0.6.1 for all Minecraft versions.  Released on 2026-11-16 (pre-release on GitHub, Modrinth and CurseForge, after a rerun of the release workflow). | ✅ |
| 17.6.1 | Version 0.6.1 in `stonecutter.properties.toml` and the changelog, the active node refreshed (C0de.VERSION). | ✅ |
| 17.7 | The GameTest `persistent_secrets_survive_leaving` fails now and then in CI (the release of 0.6.1 on 2026-11-15, 26.1.2-neoforge: "store returned {} on tick 1000"), never on the VM. Cause: it waited 1000 ticks for the Argon2 encryption of the vault, but the GameTest server of the newer versions runs its ticks much faster than 20 per second; in that run, the 1000 ticks took 8 seconds. Fix: `TestCompat.awaitSeconds` waits by the clock, the vault test up to 60 seconds per process, with a test timeout of 30000 ticks. The tag v0.6.1 keeps the old test; rerunning its release job is enough, the fix comes with the next release. CI of sprint-17 on 2026-11-16: the GameTests of all 10 nodes passed with it. | Checked again on 2026-11-21 after the VM had become faster again: five runs of the GameTests of 26.1.2-neoforge on the VM, all 58 tests passed each time (27 to 31 s per run). | ✅ |

## Sprint 18: Findings from the screenshot world

**Goal:** the bugs found while the screenshots of c0de.at were taken on the 1.21.1 Fabric test server (www-c0de task 13.11, 2026-11-17) are fixed: the wrench leads to the program of a trigger block, the password dialog of the vault keeps the password it was given and stays open after a wrong one, and force-loaded chunks of the c0de dimension don't stall the server. Planned on 2026-11-17; version 0.6.2.

| Task | Description | Done |
|---|---|:---:|
| 18.1 | Entering the dimension with the wrench leads to the sheet of the program of the trigger block, as the guide and the handbook say. `C0deDimension.enter` always picks the first free sheet (its comment still says that trigger blocks can't have a program, sprint 8), so a player in a world whose sheet 0 is empty always lands at the start. The sheet of a program comes from the program list of the dimension (`c0de_programs.dat`); Decided on 2026-11-17: a trigger block without a program, or with a program that isn't on any sheet, leads to the place where the player left the dimension last time (stored per player when they leave, like the return positions), and to the first free sheet only if they never were in the dimension. The guide, the handbook (wrench) and the concept say so. GameTests for the three cases.  Done on 2026-11-17: `ReturnPositions` also keeps where every player left the dimension (`left`), `C0deDimension.enter` asks the program list for the sheet of the program; the GameTests `playerReturnsToWhereTheyLeft` and `playerArrivesAtTheSheetOfTheProgram`, and the first entry of `playerEntersAndLeavesTheDimension` lands at the first free sheet. | ✅ |
| 18.2 | The password dialog of the persistent vault. `PasswordScreen.submit` clears the typed password (`Arrays.fill`) right after handing it over, while `VaultClient` decrypts it later on another thread, so the decryption may see a cleared password. Found on 2026-11-17: after a wrong password, the dialog of OPEN_VAULT closed instead of showing "Wrong password, try again". The password is copied before it is handed over and cleared only after the decryption; a client GameTest types a wrong password (the dialog stays open with the message), then the right one (the vault opens), and does the same for CREATE_VAULT. If the dialog still closes, find the cause with the test.  Done on 2026-11-17: the dialog hands its array over, and `VaultClient` clears it after encrypting or decrypting. The client GameTest `vaultPasswordDialog` creates a vault through the dialog, opens it with the typed password and not with a cleared one, and checks that a wrong password of the same length keeps the dialog open; with the old clearing, it fails ("The new vault doesn't open with the typed password"), so the bug was real: vaults created through the dialog of 0.6.0 and 0.6.1 are encrypted with a cleared password, and any password of the same length opened them. Decided on 2026-11-17: no repair; such vaults can't be opened any more and are deleted by an administrator and created again (changelog of 0.6.2). | ✅ |
| 18.3 | Force-loaded chunks of the c0de dimension stalled the server: on 2026-11-17, ticks took 380 and 250 seconds with about 8 and 51 force-loaded chunks of sheets (the watchdog stopped the server), and the stack of one crash was in the natural spawning. Find the cause (mobs spawning in the dimension, the chunk generator, the processes or the ticking of sheets) and fix it; the dimension should spawn no mobs. A GameTest or RCON scenario with force-loaded sheets.  Found on 2026-11-17: not a bug of c0de. All four watchdog crashes of the 1.21.1 Fabric test server (2026-11-16 07:34, 2026-11-17 19:03, 19:11 and 20:26, server time) are stalls of the whole VM, which got no CPU from its Windows host for minutes: the kernel of the VM logged a soft lockup of 485 s, RCU stalls and systemd watchdog timeouts at the same times, and at the moment of the dumps all threads of the server were idle. The test servers run with `MAX_TICK_TIME: -1` (no watchdog) in `contrib/docker/compose.yaml`; the stalls themselves are a matter of the VM and its host (docs/development_environment.md). | ✅ |
| 18.4 | What the program runner and the text notation don't tell: a line of a program must fit on its sheet (the runner accepts longer lines, which the game can't build; found with a recipe of c0de.at), and the dialog of a literal takes a text as typed, so it can't hold a line break (`\n` exists only in the text notation; programs use CHR 10). The runner reports lines wider than a sheet as a problem; the guide and the reference say how to get a line break into a text.  Done on 2026-11-17: `ProgramRunner.tooWide` reports a line that reaches past x=28 as SYNTAX_ERROR_PLACEMENT (unit test `reportsLinesWiderThanASheet`); the guide (Literals) and the reference (LITERAL) say that the dialog takes a text as typed and that CHR 10 gives a line break. | ✅ |
| 18.5 | Release 0.6.2 for all Minecraft versions.  Released on 2026-11-17 (pre-release on GitHub with 23 assets, Modrinth and CurseForge). | ✅ |
| 18.5.2 | Version 0.6.2 in `stonecutter.properties.toml` and the changelog, the active node refreshed (C0de.VERSION). CI of sprint-18 was green before (2026-11-17). | ✅ |
| 18.5.1 | Requested on 2026-11-17: the open Dependabot PR #3 (Gradle 9.8.2 to 9.9.1, Stonecutter 0.9.8 to 0.10) is part of 0.6.2. Its CI failed: Stonecutter 0.10 returns the properties as strings, so `get` and `getOrNull` take no type argument any more, and loom-back-compat 0.4.2 doesn't find the Loom plugin with it (0.4.3 does), as in c0de net task 8.4. The commit of Dependabot is taken over, the build scripts follow. Built with the GameTests of 1.21.1-fabric on 2026-11-17. | ✅ |

## Sprint 19: Library types

**Goal:** a library can define types of its own besides structs, as c0de paint needs them for its canvases and styles ([c0de paint concept](https://github.com/joernott/minecraft-c0de-paint/blob/main/docs/concept.md), "Changes to c0de"). The interface of the libraries only gets additions, so c0de net keeps working unchanged. Planned on 2026-11-21; not started yet. Version 0.7.0.

| Task | Description | Done |
|---|---|:---:|
| 19.1 | **Concept:**<ul><li>Library types in the concept of the language and of the libraries.</li><li>What a library type defines: name, default value, copy-on-write copies, comparison, text for PRINT and the debugger, saving and loading, memory size.</li><li>What the language allows with them: DIM, parameters (also reference parameters), results, arrays and hashes of them, `=` and `<>`.</li><li>What raises an error: conversion to YAML, JSON and STRING, arithmetic.</li></ul>   Done on 2026-11-21: library types in [Data](concept/04-data.md#library-type) with the new block TYPE_LIBRARY (decided on 2026-11-21: a TYPE block with a decorated pot, `VARIABLE c TYPE c0de_paint/cpaint/CANVAS`, instead of reusing TYPE_STRUCT), the cursor (15), the error codes (UNKNOWN_STRUCT_ENTITY for unknown types, UNKNOWN_LIBRARY for saved values of a missing mod), the YAML format (`typename`) and the block reference; the Java interface (`LibraryType`, `LibraryValue`, `Library.libraryTypes()`, `call.state()`) in `docs/library-development.md`. | ✅ |
| 19.2 | **`core`:**<ul><li>`Library.types()` also returns library types, as a new default method; a new value class carries them.</li><li>The type registry, DIM, parameter and result checks, arrays and hashes, comparisons and the errors.</li><li>Unit tests with a test library that defines a type.</li></ul>   Done on 2026-11-21: `LibraryType`, `LibraryValue` (a new non-sealed kind of `Value`), `Type.Library`, `TypeSpec.Library` and the keyword TYPE (added last, so the numbers of the other keywords stay the same); `Library.libraryTypes()` as a default method; the cursor, the analyzer and the runner register the types of included libraries (a type whose name isn't `library/NAME` is BROKEN_LIBRARY); assignments copy, `=` and `<>` use `equals`, JSON and YAML raise TYPE_MISMATCH; the cursor snapshot saves the text of `save`, and restoring a value whose library is missing raises UNKNOWN_LIBRARY; the YAML format writes `typename`. Unit tests `CursorLibraryTypeTest` (7); all 382 tests of `core` pass. | ✅ |
| 19.3 | **Memory:** the values of library types count against the memory limits of a process with the size they report.   Found on 2026-11-21: not needed. c0de has no memory limits per process; a value takes one slot of the stack whatever its size. A library limits the size of its values itself (c0de paint: `canvasMin` and `canvasMax`). | ✅ |
| 19.4 | **Saving with the cursor:**<ul><li>Values of library types are saved with the cursor in the compact form of their library, and loaded again.</li><li>If the mod of a saved type is missing when the cursor is loaded, the process ends with an error that names the type.</li><li>GameTests: a process survives a restart.</li></ul> |  |
| 19.5 | **Blocks, dialogs and tools:** the dialog of DIM, the validation of the blocks, the labels after AS, the debugger, the program runner and c0de disk (programs exported as YAML) know the types of the included libraries. |  |
| 19.6 | **Waiting functions of other mods:**<ul><li>`waitForValue()` and `waitSteps(n)` already exist.</li><li>New: `NativeCall.state()` and `setState(object)` keep the progress of a function while the cursor stays on its CALL line, so that long work (e.g. rotating a large canvas) is done in parts over several ticks.</li><li>The state isn't saved; after a restart the work starts again.</li><li>Unit tests with a function that counts its parts.</li></ul> |  |
| 19.7 | **Documentation:**<ul><li>`docs/library-development.md`: how a mod defines a library type.</li><li>The guide and the reference, in English and German, explain types from libraries for players.</li></ul> |  |
| 19.8 | Release 0.7.0 for all Minecraft versions. c0de net is tested against it; its dependency range already allows 0.7.0. The documentation of c0de.at for 0.7.0 follows with c0de paint (task 8.4 of c0de paint). |  |

## Risks

| Risk | Mitigation |
|---|---|
| API changes between 1.21.1 and 26.x are large (rendering, recipes, item models, networking). | The interpreter is in `core` and doesn't depend on Minecraft. Minecraft-specific code is kept thin and is concentrated in few classes. |
| Two loaders double the effort for everything that touches the game. | Platform interface, shared data generation and Stonecutter keep the loader-specific code small. GameTests run on both loaders in CI. |
| Cursors running every tick slow down servers with many processes. | Performance tests in sprint 11; the scheduler can limit the number of steps per tick. |
| Running processes depend on loaded chunks and must survive restarts. | Chunk tickets and serialized cursor states (tasks 8.4, 8.7). |
| The persistent vault needs kdbx and Bouncy Castle (several MB) in the jar, and its protocol crosses client and server. | Only the client uses them; the server only stores bytes. The protocol is tested with GameTests and a fake client (16.7). |
| The concept still changes during implementation. | The concept is updated as part of the definition of done; `concept_review.md` records decisions. |

## Decisions

Answers to the open questions of sprint 0 (2026-10-19):

| Question | Decision |
|---|---|
| Access to the dimension | Right-clicking a trigger block with a wrench teleports the player into the dimension; right-clicking black c0de bedrock with an empty hand teleports them back. See [the concept](concept/01-dimension.md#entering-and-leaving-the-dimension). |
| Package name and group ID | `de.joernott.c0de` |
| License | BSD 3-Clause ([LICENSE](../LICENSE)) |
| Publishing | Modrinth only for now. The Modrinth account is linked to the GitHub account. CurseForge can be added later. |
