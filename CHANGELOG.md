# Changelog

All notable changes of the c0de mod. The section of a version is also its changelog on Modrinth.

## Unreleased

- **Long programs**: programs longer than the view distance can be copied onto a disk and built from a disk, also on servers; they are validated while you write them, even when their end is far away. The server loads the parts of the sheet it needs for a while.
- The program runner names the block of TYPE `c0de:type_library_block` in `--layout` (it said `c0de:type_type_block`).

## 0.7.1

Findings from the first test of c0de paint.

- **Disks and library types**: a program with a TYPE block can be copied onto a disk again; the format of the program files knows TYPE and `typename`.
- **Constants stay constants**: constant items given to a trigger block come back unchanged when the process ends, instead of as empty variable items. Only variable items of parameters are used up.
- **Wrong types are red**: a CALL that passes a literal or a variable of the wrong type to a function marks its line red while you write the program, instead of failing only when it runs.

## 0.7.0

Library types: other mods can give c0de types of their own, like the canvases of the coming mod c0de paint.

- **The TYPE block**: a new type block, crafted from a type base and a decorated pot. A LABEL with the full name of a library type follows it, e.g. `VARIABLE picture TYPE c0de_paint/cpaint/CANVAS`; the LABEL dialog lists the types of the included libraries. TYPE also works in parameters, results, struct elements and as the content of arrays and hashes.
- **Library values**: you can't look into them; only the functions of their library work with them. They are copied like all values, `=` and `<>` compare them, and `⊕` writes a short description. They have no literals and no constants, and TO_JSON and TO_YAML can't write them. A process keeps them over a restart of the server.
- **Results as items**: a result of a library type comes out of the trigger block as an item that shows its description; it can't go into another process.
- **TYPE is reserved**: a program that used the name `TYPE` (in capitals) for a variable, constant or function has to rename it.
- **For mod developers**: `Library.libraryTypes()` with `LibraryType` and `LibraryValue`, and `NativeCall.state()` for functions that do their work in parts over several ticks; see the library development guide. Existing libraries, like c0de net, keep working unchanged.

## 0.6.2

Fixes found while the screenshots of c0de.at were taken.

- **Persistent vault – please read**: the password dialog cleared the typed password before the game had encrypted the vault with it. A persistent vault created through the dialog in 0.6.0 or 0.6.1 is therefore not protected by the password you typed, and since this version it can't be opened any more. If OPEN_VAULT says that your password is wrong although it is right, ask an administrator to delete your vault, the file `world/data/c0de_vaults/<your UUID>.kdbx` on the server, and create it again with CREATE_VAULT. Its secrets are lost; keep keys you need somewhere else until then. Vaults created with the parameter `password` are not affected.
- **Wrong passwords**: after a wrong password, the dialog of OPEN_VAULT stays open and says so.
- **The wrench**: right-clicking a trigger block with the wrench leads in front of the sheet of its program again. A trigger block without a program leads to where you left the dimension the last time, and to the first free sheet only the first time.
- **Line breaks**: a text typed in the dialog of a literal can't hold a line break; the guide and the reference show how to get one with CHR 10.
- **Program runner**: it reports lines that are wider than a sheet (they end at x=28), which the game can't build.

## 0.6.1

Handbooks for every mod, and corrections of the documentation.

- **Handbooks of other mods**: other mods ship handbooks of their own as resources and data, without anything in c0de being changed for them: their books, crafting grids, translations and the recipe of their binder. One binder of c0de, the Handbook, serves all of them; it is named after its book and its label has the colour of the book. The creative tab lists the binders of all books. The library development guide has a chapter "Handbooks".
- **Links between books**: a page can link to a page of another book, also of another mod. The guide links into the handbooks of c0de.
- **Documentation**: the example of REGEX_REPLACE doubles its backslashes in the text notation (it gave `3.2.1` instead of `15.11.2026`), and the reference explains why. GET_BLOCK shows its result `data`; REMOVE_BLOCK says that a container spills its contents also without `drop`; COMMAND says what `result` counts and in which language `output` comes; the guide warns that a crash also gives a signal to a comparator, and its vault example hides the key and handles a cancelled password dialog.

## 0.6.0

Security: passwords and keys out of sight.

- **Hidden input**: INPUT has the new parameter `hide`; laptops and tablets then show `*` for what is typed and `***` for the typed line.
- **Hidden items**: variable, constant and literal items can hide their value for good ("Hide the value" in their dialog). Only the trash can removes it. A parameter filled by a hidden item, and WRITE of a sensitive value, stay hidden.
- **Sensitive values**: the tag `security: "sensitive"` on variables, constants and struct elements makes the debugging tool and the program runner show `***` instead of the value.
- **The vault**: every player has a vault of secrets while they are online. The new SECRET block reads a secret like a constant. A program can only use the vault of the player who started it, and only if that player owns its sheet.
- **c0de/security**: a new library with SET_SECRET, DELETE_SECRET, HAS_SECRET, CREATE_VAULT, OPEN_VAULT, HAS_VAULT and IS_VAULT_OPEN.
- **Persistent vault**: secrets can be kept in a KeePass file (KDBX 4) per player. The server stores it, but only the game of the player opens it, with a password that is typed in a dialog and never leaves the game. The files open in KeePass and KeePassXC.
- **Error codes**: VAULT_UNAVAILABLE (314) to VAULT_FAILED (321).
- **Program runner**: the options file can give the program a vault (see docs/runner.md).
- **Block data**: GET_BLOCK returns the data of a block (the items of a chest, the text of a sign) as SNBT in its new result `data`, and PLACE_BLOCK takes it in its new parameter `data`.
- **MOVE_ITEMS** works for every owner who could open both inventories. `count` is optional (default 1, -1 for all), and instead of `id` it takes a `filter` (item IDs or a search like in JEI). It takes time: one stack per tick for each 128 blocks. The settings `enableMoveItems`, `moveItemsStacksPerTick` and `moveItemsBlocksPerTick` configure it.
- **COMMAND**: a new function of c0de/minecraft runs a command like a command block, for administrators (setting `enableCommand`).
- **Regular expressions**: REGEX_MATCH and REGEX_REPLACE in c0de/strings, with replacements and flags like sed.
- **Beta**: from this version on, c0de is published as a beta instead of an alpha.
- **Handbooks without Patchouli**: the guide and the six handbooks are part of c0de and work on every Minecraft version and both loaders. Each one is a red binder with a label of its own colour, crafted from a book and the same item as before, in English and German. The book screen and its Markdown are based on [Lavender](https://github.com/wisp-forest/lavender) and lavender-md (MIT, see NOTICE.md). Every player gets the guide binder once, also players who got the Patchouli guide before. c0de no longer uses Patchouli.
- **Fixed**: the trash can icon of the item dialogs was drawn in the wrong place on Minecraft 1.21.11 and later.
- **Tag dialog**: the language button shows only the language (`security` instead of `Language: security`).
- **Fixed**: c0de for Fabric 1.21.1 reported the version 0.1.0 in C0DE_VERSION and in the log.

## 0.5.0

Feedback from the tutorials of c0de.at.

- **PRINT** writes BOOLEAN values as `TRUE` and `FALSE`, as the handbook says (⊕ still writes `true` and `false`).
- **RETURN** raises INVALID_ARGUMENT for an error the program made with a code below 1000, like RAISE; an error received from a CALL can still be passed on.
- **Program runner**: it knows the types and functions of c0de/minecraft, so programs that include it are validated completely and run until they call the library, which raises INVALID_ARGUMENT ("needs the game"). It reports every problem once, sorted by line. docs/runner.md describes the values of `input`.
- **Schema of the program files**: `c0de-program.schema.json`, attached to the GitHub release, describes the YAML files of the disk drive, so editors can check hand-written files. Every file is checked against it before it is read; a file that doesn't match is refused with a message that names the first problem and where it is.
- **Safe program names**: wherever a program crosses a disk (copying, saving, loading, receiving it on the server, building it), its name must be a valid name. Names with path separators, invisible or right-to-left characters or look-alike letters of other scripts are refused, so a program can't hide its real name or write outside the folder `c0de`.
- **Documentation**: the guide, the reference and the handbooks answer what the tutorials of c0de.at had to find out by trying (indexes, the minus sign, precedence of AND and OR, errors as constants, EXIT and results, TICKS and the time of day, FORMAT, the time zone of TIMER and WEEK, POSITION, ENTITY and ITEM_STACK, FIND_ENTITIES, PID.activator, the instruments of SOUND, SAY, FROM_JSON). PI has a page of its own.

## 0.4.0

Documentation: for the tutorials of c0de.at and for players who read German.

- **C0DE_VERSION**: a built-in constant with the version of the mod. Other mods can register built-in constants of their own, like C0DE_NET_VERSION of c0de net.
- **Program runner**: `c0de-runner-<version>.jar`, attached to the GitHub release, runs and tests c0de programs without Minecraft (see docs/runner.md).
- **German**: the guide and the handbooks are translated into German (docs/de), and the Patchouli books show them in German when the game is set to German.
- **Timer blocks**: the message "Paused by redstone" is translated (the dialog showed its key).
- **3D models**: `c0de-models-<version>.zip`, attached to the GitHub release, has every block of the mod as a glTF model, for the 3D views of programs on c0de.at.

## 0.3.0

Feedback from playing 0.2.0, for all Minecraft versions of 0.2.0.

- **Timer blocks**: a redstone signal pauses them, also when they are placed next to a signal that is already on; their dialog shows "Paused by redstone". Pause a fast timer before you change its program.
- **Disk drive**: a larger dialog that shows its messages completely, and the sound of a floppy drive when it reads or writes a disk.
- **SHEET and PID**: two new built-in constants tell a program about its sheet (number, owner, position) and its process (PID, function, owner, the player who started it, the trigger block, start time).
- **CURSOR**: a built-in constant that tells a program where its cursor is (line, function, depth) and how much of its stack it uses.
- **Debugging tool**: the cursor dialog shows how many slots of the stack are used.
- **Colors**: the constants COLOR_BLACK to COLOR_WHITE name the 16 colors of COLOR.
- **MINE_BLOCK**: a new function of c0de/minecraft breaks a block with the most suitable tool from an inventory, as fast as a player and with the same wear, and puts the drops into another inventory. Every owner may use it.
- **PLACE_BLOCK**: the new parameter `inventory` takes the block from an inventory; with it, every owner may place blocks.
- **Error codes**: INVENTORY_FULL (312) and ITEM_MISSING (313).
- **Boots**: left-click a FUNCTION block with any boots to jump to the other end of its function; right-click it to jump to the PROGRAM line.
- **Pliers**: a new tool, crafted from shears and a stick, moves a line or a marked area one block to the left (left click) or to the right (right click).
- **Note**: the names SHEET, PID, CURSOR, SHEET_INFO, PID_INFO, CURSOR_INFO and COLOR_BLACK to COLOR_WHITE are now reserved; a program that declares one of them must rename it.

## 0.2.0

c0de for all newer Minecraft versions: 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3, each for Fabric and NeoForge.

- **New Minecraft versions**: the mod works the same on 1.21.11, 26.1.2, 26.2 and 26.3; programs saved with the disk drive can be loaded on every version. 26.x needs Java 25, like Minecraft itself.
- **Fixed**: the recipe of the wrench had an invalid category; Minecraft 26.3 refused to load it.
- **Patchouli**: the in-game books are only available on 1.21.1, where Patchouli was tested with the mod.

## 0.1.0

The first alpha for Minecraft 1.21.1, for Fabric and NeoForge.

- **The c0de dimension**: endless sheets of green-and-white computer paper, one program per sheet, with line numbers that show whether every line is correct.
- **The language**: variables and constants of the types BOOLEAN, INTEGER, FLOAT, CHAR, STRING, TIME, ARRAY, HASH, STRUCT and ERROR; expressions; functions with parameters and results; recursion; IF and ELSE; the loops FOR, EACH, WHILE and REPEAT; error handling.
- **Running programs**: trigger blocks, timer blocks and debug trigger blocks start processes anywhere in the world; variable and constant items carry values in and out; a debug cursor can be paused and stepped line by line.
- **Input and output**: PRINT on display walls, laptops and tablets, INPUT from I/O blocks or typed on a terminal, WRITE into I/O blocks, and the screen functions CLS, LOCATE, CSRLIN, POS, COLOR and INKEY of AmigaBASIC.
- **Libraries**: c0de/math, c0de/strings (with FORMAT, JSON and YAML), c0de/time and c0de/minecraft (sounds, chat, levers, buttons, doors, inventories, blocks, entities). Other mods can add libraries.
- **Tools**: the wrench, the text marker, the hammer, the debugging tool, the c0de disk, which carries a program, and the disk drive, which saves programs as YAML files on your computer and loads them.
- **Documentation**: a player guide and six handbooks, also as books in the game if Patchouli is installed.
- **Multiplayer**: sheets belong to the player who builds on them first; the floor shows the number and owner of every sheet, and the wrench gives sheets away. Trigger blocks decide who may start them (Run).
- **Permissions**: functions that change the world are off by default for players without operator rights and can be enabled in `config/c0de.properties`.
