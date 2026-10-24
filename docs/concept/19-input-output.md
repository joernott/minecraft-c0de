# Input and output

[c0de concept](../concept.md) › Input and output

Programs talk to players through screens and items: PRINT writes text on the screens of displays, laptops and tablets, WRITE puts values into I/O blocks as variable items, and INPUT reads a value from an I/O block or from what a player types on a laptop or tablet.

## Binding

Display blocks, I/O blocks, laptops and tablets are bound to a program and one of its functions, like a [trigger block](13-running-programs.md#trigger-block): right-clicking the block (or using the tablet) opens a dialog with a Program button and a Function button, which cycle through the programs and their functions, and ✔, which binds the device and closes the dialog. A process works with all devices that are bound to its program and its entry function. If several processes of the same program and function run at the same time, they share the devices.

The devices are ordered by the time they were bound: the device bound first comes first. Binding a device again moves it to the end.

## Functions

PRINT, WRITE and INPUT are [built-in functions](08-builtin-functions.md#built-in-functions), called with a CALL statement like any other function:

```
CALL INPUT
    PARAMETER variable count ;
CALL PRINT
    PARAMETER value count * 2 ;
```

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| PRINT | value (any type) | – | Writes `value` on the bound screens and ends the line. | PRINT |
| WRITE | value (any type), name STRING (optional) | – | Puts `value` into the bound I/O blocks as a variable item. Waits for an uninitialized variable item. | WRITE |
| INPUT | variable (a variable, by reference) | – | Reads a value into `variable`. Waits for a value. | INPUT |

### PRINT

`PRINT` writes `value` and ends the line, on the screen of every bound display wall, laptop and tablet. Values are written in their text form: strings and characters without quotes, numbers and times as literals, BOOLEAN values as `TRUE` and `FALSE`, arrays, hashes and structs as JSON. To print several values on one line, programs join them with ⊕ or use LOCATE. Without bound screens, PRINT does nothing.

### WRITE

`WRITE` writes `value` into the I/O blocks. Each bound I/O block with an uninitialized variable item in its input inventory gets one: the item is taken from the input inventory, and a variable item with the value is put into the output inventory. Its name is `name` if it is given; otherwise the name of the variable if `value` is a single label without index (`PARAMETER value count ;` makes an item `count`), otherwise `output`. Its type is the type of the value. An I/O block whose output inventory is full is skipped.

If no bound I/O block has an uninitialized variable item, the cursor waits on the CALL line until a player puts one into one of them. Laptops and tablets don't take part in WRITE.

### INPUT

`INPUT` reads a value into the variable given as `variable`. Like the `destination` of FROM_JSON, the parameter is passed by reference: its expression MUST be a single label without index that names a declared variable, not a constant or loop variable; otherwise the CALL statement is invalid (SYNTAX_ERROR_CALL). The cursor looks for the value in this order:

1. The input inventories of the bound I/O blocks, in the order of binding, each from its first slot: a variable or constant item with the name of the variable and its type, or a literal item with a value of its type.
2. A line typed on a bound laptop or tablet. The text is read like the value of a LITERAL block for simple types and STRING, and as JSON for ARRAY, HASH and STRUCT. Text that doesn't fit the type is refused with a message on the terminal, which keeps waiting.

If neither has a value, the cursor waits on the CALL line, and the input lines of the bound laptops and tablets become available. The used item is taken from the input inventory, and an item goes into the output inventory: an uninitialized variable item for a variable item, the unchanged constant item for a constant item, and the literal item without its value for a literal item. If the output inventory is full, the item is dropped on the I/O block.

## Screen functions

These built-in functions are taken from AmigaBASIC. They work on all bound screens; functions that return something answer for the screen of the device bound first (0 or the empty string without screens). Rows and columns start at 0.

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| CLS | – | – | Clears the screens; the cursor moves to row 0, column 0. | CLS |
| LOCATE | row INTEGER, column INTEGER (default 0) | – | Moves the cursor of the screens. Positions outside a screen are moved to its nearest edge. | LOCATE |
| CSRLIN | – | result INTEGER | The row of the cursor. | CSRLIN |
| POS | – | result INTEGER | The column of the cursor. | POS |
| COLOR | foreground INTEGER, background INTEGER (default 0) | – | The colors of the text printed from now on: the 16 colors of Minecraft text, 0 (black) to 15 (white). Background 0 is the screen color. A number outside 0 to 15 raises INVALID_ARGUMENT. | COLOR |
| INKEY | – | result STRING | The oldest key a player typed on a bound laptop or tablet while no INPUT was waiting, or the empty string. Doesn't wait. | INKEY$ |

## Screens

Every display wall, laptop and tablet has a screen of its own: a grid of characters, each with a foreground and a background color, and a cursor. Text is written at the cursor; at the end of a row it continues in the next row, and below the last row the screen scrolls up by one row. All screens bound to a program and function get the same output, each in its own size. The text is shown in the monospace font [Monocraft](https://github.com/IdreesInc/Monocraft) (SIL Open Font License).

Screens keep their content when the world is saved, until the next CLS or until the device is bound again.

## Display block

A display block shows a screen of 16 columns and 8 rows. Display blocks placed next to each other in a flat rectangle, all facing the same direction, form a display wall that shows one screen across all blocks: a wall of 4 × 2 blocks has 64 columns and 16 rows. A wall can stand upright or lie flat.

Its front is a borderless, very dark gray (#141414) face that shows the screen; the other sides look like iron blocks. Left-clicking a face with the wrench rotates the block around the axis through that face. A display block placed next to another one takes its direction.

Right-clicking a display block with an empty hand opens its dialog, which looks like that of a trigger block: light gray, the player inventory on the left, and on the right the Program and Function buttons, ✔ and one camouflage slot. A full block without a block entity in the camouflage slot camouflages the sides of the display block; taking it out removes the camouflage. Breaking the display block drops the camouflage block.

Binding any block of a wall binds the whole wall; a block that joins a wall takes the binding of the wall. The camouflage belongs to each block on its own.

Eight display blocks are crafted from a glowstone dust in the middle, a redstone dust on its left and seven iron nuggets.

## I/O block

The I/O block looks like a hopper in the colors of c0de. It has an input inventory and an output inventory of 18 slots (two rows) each; the input inventory accepts variable, constant and literal items. Right-clicking it opens its dialog, which looks like that of a trigger block: the input and output inventories on the left, the player inventory below them, and the Program and Function buttons and ✔ on the right. Hoppers and pipes can fill its input and empty its output like those of a trigger block.

An I/O block is crafted shapelessly from a data item and a hopper.

## Literal item

A literal item is a card with a gray border that holds a type and a value, but no name. Right-clicking with it opens a dialog for its type and value. An *empty* literal item has a type but no value. Two literal items are crafted shapelessly from a data item and light gray dye.

## Laptop

A laptop is a block that looks like an open laptop; its screen faces the player who placed it. Left-clicking it with the wrench turns it clockwise (seen from above). Right-clicking it opens its terminal: the screen of the laptop (48 columns and 20 rows; the rows that scroll off the top stay in a history of 200 rows, with a scroll bar and the mouse wheel), and below it an input line, which is only available while an INPUT of a bound process waits. Above the screen, two buttons pick the program and function of the binding, and ✔ binds the terminal and closes it. Keys typed while no INPUT waits are kept for INKEY. A laptop doesn't take part in WRITE.

The terminals of laptops and tablets are stored with the world (`data/c0de_terminals.dat`), so processes reach them even if the laptop is in an unloaded chunk or the tablet in a chest. Breaking a laptop deletes its terminal.

A laptop is crafted shapelessly from a display block and an I/O block, or from a tablet.

## Tablet

A tablet is a laptop as an item: left-clicking with the tablet in the main hand opens its terminal. The item keeps the ID of its terminal; its binding, screen and history are stored with the world like those of laptops. A tablet is crafted from a laptop, and a laptop from a tablet; the crafted device gets a new, unbound terminal.
