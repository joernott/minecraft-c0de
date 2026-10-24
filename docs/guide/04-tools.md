# Tools and Items

c0de comes with a few tools for writing and testing programs, and with items that carry data. This chapter explains what each one does and how to craft it.

## Wrench

Right-click a trigger block with the wrench to enter the c0de dimension. You arrive in front of the program of that trigger block, or of the first free sheet.

On a sheet, the wrench moves and copies lines you marked with the text marker. Left-click a line that is not marked: the marked lines move in front of it. The gaps close by themselves.

Right-click a line to copy the marked lines in front of it. That line and all lines after it move south. In survival mode, the copied blocks come from your inventory.

Recipe: three iron ingots in a V shape (top left, top right, middle) above a red wool block.

## Text marker

The text marker looks like a yellow feather. Left-click a line to mark it, left-click it again to remove the mark. Marked lines get a yellow overlay.

Right-click another line of the same sheet to mark everything from your last left-clicked line to this one. If the last left click removed a mark, the right click removes the marks instead.

Each right click is remembered too, so you can grow a selection step by step.

Recipe: a feather and yellow dye, in any shape.

## Hammer

Left-click a line with the hammer to delete it. If you click a marked area, the whole area is deleted. The lines below move up to close the gap. In survival mode, the blocks go to your inventory.

Right-click a line to insert an empty line in front of it. The clicked line and the lines after it move south.

Clicking the floor of a line works like clicking a block of the line. This is true for the marker, the wrench and the hammer.

Recipe: three iron ingots in a row, one iron ingot below the middle and a red wool block below that.

## Debugging tool

The debugging tool looks like a fly swatter. Right-click any c0de block with it to see all its properties. For a red line number, it tells you what is wrong.

Hit a debug trigger block with it to start a process with a red **debug cursor**. The debug cursor starts paused.

- Left-click the paused cursor: it moves one line.
- Shift-left-click the cursor: it runs on its own, or pauses again.
- Right-click any cursor: see its properties and its stack.
- Shift-right-click a PID block: abort the process.

Recipe: a red wool block bottom left, a stick in the middle and a leather top right.

## c0de disk

The c0de disk looks like an old floppy disk. It saves programs as YAML files and loads them again.

To save, right-click the PROGRAM block with the disk. The file goes to the folder `c0de/programs` in the world folder of the server. Saving also works while the program runs.

To load, right-click the floor at the start of an empty sheet (x=5, line 0). Pick a file from the list. The program is placed on the sheet. In survival mode, the blocks come from your inventory.

Recipe: a data item, an iron ingot and black dye, in any shape.

## Data item

The data item looks like a blank punch card. It is the base for all items that carry data.

Recipe: a paper, white dye and yellow dye, in any shape. You get four data items.

## Type items

A type item looks like a data item with a red border. It stands for a data type and is used to craft variable and constant items of that type.

Recipe: a data item, red dye and the item of the type, in any shape:

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

## Variable items

A variable item has a blue border. It carries a name, a type and a value into or out of a process. Hold it and right-click to edit it.

Recipe: a data item, a type item and light blue dye give two variable items of that type.

An **uninitialized** variable item has no name, type or value. A process writes a result into it. Recipe: a data item and light blue dye.

The trash can button in the dialog empties all fields. Saving with all fields empty makes the item uninitialized again.

## Constant items

A constant item has a pink border. It works like a variable item, but once it has a value, it can't be changed.

Recipe: a data item, a type item and pink dye give two constant items. A data item and pink dye give an uninitialized constant item.

## The NOOP block

The NOOP block is the base of almost every language block. It is a clear frame with metal corners.

Recipe: iron ingots in the four corners and sticks in the four edges, with the center empty. You get 64 NOOP blocks.

## Eight around one

Many blocks are crafted from eight NOOP blocks around one item. You get eight blocks:

- COMMENT: glass
- LITERAL: white wool
- OPERATOR: red wool
- ASSIGN: cyan wool
- TAG: brown wool
- BEGIN: yellow wool
- EXIT: iron door
- PARAMETER: hopper
- RETURN: spruce door

The **type base** (red carpet) and the **structural base** (green wool) are made the same way. They are items for crafting other blocks.

## NOOP plus one

These blocks are crafted from one NOOP block and one item, in any shape:

- LABEL: paper
- PROGRAM: book
- INCLUDE: empty map
- FUNCTION: stone
- CALL: smooth stone
- BREAK: stick

## Type blocks

Type blocks are red slabs. Each is crafted from a type base and an item, in any shape:

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

VARIABLE is a type base and a bone block. CONSTANT is a type base and three bones.

## Structural blocks

Conditions and loops are crafted from a structural base and an item, in any shape:

- IF: wheat seeds
- ELSE: wheat
- FOR: dirt
- EACH: cobblestone
- WHILE: diorite
- REPEAT: granite

## Swapping blocks

Some blocks can be turned into their partner. Put one alone into the crafting grid:

- BEGIN and END
- PARAMETER and RESULT
- VARIABLE and CONSTANT
- IF and ELSE, FOR and EACH, WHILE and REPEAT
- INTEGER and FLOAT, with yellow or orange dye

## Trigger blocks

All three trigger blocks share one pattern: copper ingots in the corners, glass on the edges and one item in the center.

- trigger block: gold ingot, gives 8
- timer block: clock, gives 8
- debug trigger block: redstone block, gives 1

## Guide book

If the mod Patchouli is installed, this guide is also a book in the game. You get it once, the first time you join a world with c0de.

To make another copy, craft a book and a PROGRAM block together, in any order.

## Handbooks

With Patchouli, there are five more red books: handbooks with one page per block, item or function. Each page has a short description, the crafting recipe and an example.

- **Data**: crafted from a book and a type base
- **Statements**: a book and a structural base
- **Tools**: a book and a wrench
- **Triggers**: a book and a trigger block
- **Functions**: a book and a FUNCTION block
