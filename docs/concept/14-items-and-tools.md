# Items and tools

[c0de concept](../concept.md) › Items and tools

## Debugging tool

The debugging tool looks like a fly swatter with a wooden handle and a leather square at the top.

Left-clicking a cursor with the debugging tool also shows its current line in the chat of the player, in the notation of the line. A wrong line is shown in red and an unfinished statement in gold, followed by the problem.

Shift-right-clicking a PID block with the debugging tool aborts its process: the cursor crashes with PROCESS_ABORTED (206), and the process ends like a crashed one, so the items it took from the input inventory go to the output inventory. Only the owner of the trigger block and operators can abort a process.

It is used to
  - start a process with a debug trigger block (by attacking the block),
  - control a debug cursor and inspect cursors (see [Movement](15-cursor.md#movement)),
  - inspect blocks: right-clicking any c0de block with the debugging tool opens a read-only dialog that shows all properties of the block, e.g. the kind, parent, name and index of a LABEL, the type and value of a LITERAL or the PID, trigger coordinates and owner of the process of a PID block. PROGRAM and FUNCTION blocks also show the owner of their sheet, trigger blocks their owner.

### Crafting

A debugging tool is crafted from a red wool block, a stick and a piece of leather:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "  A",
    " B ",
    "C  "
  ],
  "key": {
    "A": "minecraft:leather",
    "B": "minecraft:stick",
    "C": "minecraft:red_wool"
  },
  "result": {
    "id": "c0de:debug_tool",
    "count": 1
  }
}
```

## Marker

A text marker is used to mark code. It looks like the feather of Minecraft, colored yellow. Left-clicking a line of code with the marker marks the line, left-clicking it again removes the mark. A marked line gets a yellow overlay.

The marker remembers the line of its last left click and whether that click marked the line or removed its mark. Right-clicking another line of the same sheet then does the same with all lines from the remembered line to the clicked line: it marks them, or removes their marks. The clicked line is remembered for the next right click, so a selection can be extended step by step.

### Crafting

A text marker is crafted shapelessly from a feather and yellow dye.

## Wrench

Every decent tech mod needs a wrench. This is the c0de wrench. Right-clicking a trigger block with the wrench [teleports the player into the c0de dimension](01-dimension.md#entering-and-leaving-the-dimension). On a sheet, the wrench moves and copies marked code. The marked lines of the sheet are the selection; they don't need to be consecutive. Left-clicking a line that is not marked moves the marked lines in front of (north of) that line, in their order; the other lines close the gaps. Right-clicking a line copies the marked lines in front of it; that line and all lines after it move south. In survival mode, the blocks of the copies are taken from the inventory of the player; if blocks are missing, nothing is copied and the player gets a list of the missing blocks. The marks move with their lines.

Clicking the floor of a line works like clicking a block of the line, for the marker, the wrench and the hammer.

### Crafting

A wrench is crafted from red wool and three iron ingots:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "A A",
    " A ",
    " B "
  ],
  "key": {
    "A": "minecraft:iron_ingot",
    "B": "minecraft:red_wool"
  },
  "result": {
    "id": "c0de:wrench",
    "count": 1
  }
}
```

## Hammer

Every decent tech mod needs a hammer. This is the c0de hammer. It is used to delete a single line or marked code: the user left-clicks a marked area or a single line, which removes the marked area or the line. The following lines up to the next empty line are moved up (north) automatically. In survival mode, the removed blocks go to the inventory of the player.

Empty lines can be deleted too: left-clicking an empty line removes it, and the lines after it move up. Right-clicking a line with the hammer inserts an empty line in front of it: the clicked line and all lines after it move south, up to the next gap of two empty lines, so they don't join the lines after the gap. The marks move with their lines.

The wrench and the hammer can't be used on a sheet while a process of its program is running (see [Protection of running programs](13-running-programs.md#protection-of-running-programs)).

### Crafting

A hammer is crafted from red wool and four iron ingots:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "AAA",
    " A ",
    " B "
  ],
  "key": {
    "A": "minecraft:iron_ingot",
    "B": "minecraft:red_wool"
  },
  "result": {
    "id": "c0de:hammer",
    "count": 1
  }
}
```

## c0de disk

The c0de disk imports and exports programs as YAML files. It looks like a 3.5" floppy disk, a nod to the Amiga.

  - **Export**: Right-clicking the PROGRAM block of a sheet with the c0de disk writes the program to the file `c0de/programs/<program name>.yaml` in the world folder of the server. If the file already exists, the player is asked to confirm overwriting it. Exporting is also possible while processes of the program are running, as it does not change the sheet.
  - **Import**: Right-clicking the floor at the position of the PROGRAM statement (x=5, z=0) of an empty sheet with the c0de disk opens a dialog that lists the YAML files in the `c0de/programs` folder. Selecting a file places the program on the sheet (see [YAML format](17-yaml-format.md)), including the properties of all LABEL, LITERAL and OPERATOR blocks. In survival mode, the required blocks are taken from the inventory of the player; if blocks are missing, nothing is placed and the dialog lists the missing blocks. If the file is invalid or a program with the same name already exists, nothing is placed and the dialog shows an error message.

### Crafting

A c0de disk is crafted shapelessly from a data item, an iron ingot and black dye.

## Data item

The data item is the base for crafting items that contain data. It looks like a blank punch card: a cream-colored (color #FFFEE9) rectangle with the upper right corner cut off at a 45 degree angle and light gray markings on it (color #CCCCCC).

### Crafting

Four data items can be crafted shapelessly from a paper, white dye and yellow dye.

## Type items

A type item looks like the data item it was crafted from, but with a red border.

### Crafting

Each type item can be crafted shapelessly from a data item, red dye and the item that is used to craft the corresponding type block:
  - BOOLEAN: redstone torch
  - INTEGER: sand
  - FLOAT: red sand
  - CHAR: oak plank
  - STRING: string
  - ARRAY: barrel
  - HASH: chest
  - STRUCT: scaffolding
  - TIME: clock
  - ERROR: campfire

## Variable item

The variable item looks like the data item it was crafted from, but with a blue (color #5555FF) border. Its front and back show the item of its type.

A variable item has the following properties:
  - name
  - type
  - subtype (for arrays and hashes) or struct name (for structs)
  - value
  - tags (optional, see [Tags](04-data.md#tags))

A variable item is *empty* if it has no name and no value. Right-clicking with a variable item in the main hand opens a dialog in which its name, type and value can be edited. A small triangle at the end of the type field shows that it has a dropdown list. While the type field has the focus, the dropdown list below it offers the types: the simple types, STRING, ERROR, the struct types of the libraries and arrays and hashes of them, filtered by the entered text. Struct types of a program can be typed. A variable item that was crafted with a type item keeps the kind of its type (e.g. ARRAY); only its subtype can be chosen.

A variable item is *uninitialized* if it has no name, no type and no value. It is used as the target of a result that has no parameter at the same position: the result sets its name, type and value. The dialog has a trash can button that empties all fields; saving the dialog with all fields empty, whether they were emptied with the button or by hand, makes the item uninitialized.

### Crafting

Two variable items can be crafted shapelessly from a data item, a type item and light blue dye. An uninitialized variable item is crafted shapelessly from a data item and light blue dye.

## Constant item

The constant item looks like the data item it was crafted from, but with a light purple (color #FF55FF) border. Its front and back show the item of its type.

A constant item has the same properties as a variable item. Right-clicking with a constant item in the main hand opens a dialog in which its name and value can be edited, as long as it has no value yet.

### Crafting

Two constant items can be crafted shapelessly from a data item, a type item and pink dye. An uninitialized constant item is crafted shapelessly from a data item and pink dye.

## Guide book

If the mod [Patchouli](https://vazkiimods.github.io/Patchouli/) is installed, the c0de guide is available as an in-game book. It is generated from the player guide in the repository (`docs/guide`). Every player gets the book once, when they join a world with the mod for the first time. More books are crafted shapelessly from a book and a PROGRAM block.

Five red handbooks describe every element on a page of its own (a short description, the crafting recipe and an example), generated from `docs/reference`: Data (crafted from a book and a type base), Statements (book and structural base), Tools (book and wrench), Triggers (book and trigger block) and Functions (book and FUNCTION block). Without Patchouli, there are no books and no recipes for them.
