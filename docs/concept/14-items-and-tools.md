# Items and tools

[c0de concept](../concept.md) › Items and tools

## Debugging tool

The debugging tool looks like a fly swatter with a wooden handle and a leather square at the top.

It is used to
  - start a process with a debug trigger block (by attacking the block),
  - control a debug cursor and inspect cursors (see [Movement](15-cursor.md#movement)),
  - inspect blocks: right-clicking any c0de block with the debugging tool opens a read-only dialog that shows all properties of the block, e.g. the kind, parent, name and index of a LABEL, the type and value of a LITERAL or the PID and trigger coordinates of a PID block.

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

A text marker is used to mark code. It looks like a yellow feather. Left-clicking a line of code with the marker marks the line, left-clicking it again removes the mark. A marked line gets a yellow overlay.

### Crafting

A text marker is crafted shapelessly from a feather and yellow dye.

## Wrench

Every decent tech mod needs a wrench. This is the c0de wrench. It is used to move marked code: the user left-clicks a marked area and then right-clicks a line outside that area. This moves the marked lines in front of (north of) that line.

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

Every decent tech mod needs a hammer. This is the c0de hammer. It is used to delete a single line or marked code: the user left-clicks a marked area or a single line, which removes the marked area or the line. The following lines are moved up (north) automatically.

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

A variable item is *empty* if it has no name and no value. Right-clicking with a variable item in the main hand opens a dialog in which its name and value can be edited.

### Crafting

Two variable items can be crafted shapelessly from a data item, a type item and light blue dye.

## Constant item

The constant item looks like the data item it was crafted from, but with a light purple (color #FF55FF) border. Its front and back show the item of its type.

A constant item has the same properties as a variable item. Right-clicking with a constant item in the main hand opens a dialog in which its name and value can be edited, as long as it has no value yet.

### Crafting

Two constant items can be crafted shapelessly from a data item, a type item and pink dye.
