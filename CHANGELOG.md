# Changelog

All notable changes of the c0de mod. The section of a version is also its changelog on Modrinth.

## 0.3.0 (unreleased)

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
