# Libraries

c0de comes with ready-made functions. A few are built in and always there. The others are grouped in libraries, which you load with an INCLUDE line.

## Calling them

All these functions are called with CALL, like your own functions. Built-in functions use their plain name. Library functions use their full name, like `c0de/strings/MID`.

```c0de
CALL c0de/strings/MID
    PARAMETER text name ;
    PARAMETER start 2 ;
    PARAMETER length 3 ;
    RESULT result part
```

A function with one result names it `result`. Positions in texts and lists start at 0.

## Changing values

Functions get copies of their parameters. A function that changes a text, list or hash gives back the changed copy as `result`.

To change a variable, use it both as parameter and as result target:

```c0de
CALL INSERT
    PARAMETER array list ;
    PARAMETER index 0 ;
    PARAMETER value 0 ;
    RESULT result list
```

## Including libraries

INCLUDE lines come right after the NOOP in line 1, before any other statement. They start at x=5, like the PROGRAM line.

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
```

The LABEL dialog next to an INCLUDE block lists all libraries you can choose from.

## Built-in functions

These work without INCLUDE:

- LEN: the length of a text, array or hash
- HAS_KEY: true if a hash has a key
- REMOVE: an array or hash without one element
- INSERT: an array with a new element at a position
- CLEAR: an empty array or hash
- SWAP: swaps two values
- RAISE: raises your own error, code 1000 or higher

## c0de/math

Numbers and random values. It also has the constant `c0de/math/PI`, which you use in an expression like a variable, without CALL.

- ABS, SGN: size and sign of a number
- SQR, EXP, LOG: square root, e to the power, logarithm
- SIN, COS, TAN, ATN: angles in radians
- INT, FIX, ROUND: turn a FLOAT into an INTEGER
- TO_FLOAT: turn an INTEGER into a FLOAT, against NUMERIC_OVERFLOW in big calculations
- DIV: whole-number division
- RANDOM: a random FLOAT from 0 to below 1
- RANDOMIZE: set the start of the random numbers

## Rounding

INT rounds down: -12.85 gives -13. FIX just cuts off the decimals: -12.85 gives -12. ROUND goes to the nearest whole number. A .5 goes to the even neighbor, so 12.5 gives 12 and 13.5 gives 14.

```c0de
CALL c0de/math/ROUND
    PARAMETER value 12.5 ;
    RESULT result rounded
```

## c0de/strings

Text and characters:

- LEFT, RIGHT, MID: parts of a text
- REPLACE: overwrite part of a text
- INSTR: find a text in a text, -1 if not found
- UCASE, LCASE: upper and lower case
- SPACE, FILL: a text of spaces or of one character
- ASC, CHR: characters and their code numbers

Conversions:

- STR: any simple value as text
- FORMAT: a value as text in a format you choose
- VAL: a text as a number
- HEX, OCT: a number in hexadecimal or octal

Regular expressions:

- REGEX_MATCH: does a text match a pattern like `^[0-9]+$`?
- REGEX_REPLACE: replace what a pattern matches, like sed

## FORMAT

FORMAT turns a value into text with a format string. `%d` is a whole number, `%f` a decimal number and `%s` any value. A number between `%` and the letter sets the width.

- `[%4d]` turns 42 into `[  42]`
- `%05d` turns 42 into `00042`
- `%.2f` turns 3.14159 into `3.14`
- `%,d` turns 1234567 into `1,234,567`

For TIME values, `%tF` gives the date and `%tT` the time, like `2026-10-19` and `14:05:09`. Many more codes exist, such as `%tA` for the name of the weekday.

All codes use the same value, so `"%tF %tT"` shows date and time at once. `%f` also takes an INTEGER, but `%d` doesn't take a FLOAT. The text is always English: `.` and `,` in numbers, English names of weekdays and months, in the time zone of the server.

## JSON and YAML

c0de/strings can also turn any value into JSON or YAML text with TO_JSON and TO_YAML. FROM_JSON and FROM_YAML read such text back into a variable.

TAG blocks at the end of a VARIABLE line or a struct element set the key names used in JSON or YAML.

## c0de/time

Real-world time and date, in the time zone of the server:

- NOW: the current time as a TIME value
- YEAR, MONTH, DAY: parts of the date
- HOUR, MINUTE, SECOND, MILLISECOND: parts of the time
- WEEKDAY: 1 for Monday up to 7 for Sunday
- WEEK: the week of the year
- TIMER: seconds since midnight
- SLEEP: wait for a number of game ticks

The date and time functions use the current time, or a TIME value you pass as `time`. SLEEP waits between 1 and 72000 ticks.

TIMER starts at 0 at midnight. WEEK gives the ISO week: on 1 January it can still be week 52 or 53 of the year before, and no function gives that year.

## c0de/minecraft

This library lets programs touch the world around the trigger block:

- TICKS: the game time of the world, not the time of day
- BEEP, SOUND: play notes like a note block
- SAY: write a message to the chat
- GET_BLOCK: read a block, its redstone power and its data
- GET_INVENTORY: read the items in a chest or other container

- GET_LEVER, SET_LEVER: read or flip levers
- GET_BUTTON, PRESS_BUTTON: read or press buttons
- GET_PRESSURE_PLATE: read pressure plates
- GET_OPEN, SET_OPEN: doors, trapdoors and fence gates
- FIND_ENTITIES: find mobs and items nearby

## Changing the world

Some functions change the world more:

- DELETE_ITEMS: remove items from containers
- PLACE_BLOCK, REMOVE_BLOCK: place or break blocks
- SPAWN_ITEM, SPAWN_ENTITY: create items and mobs
- TELEPORT: move an entity
- COMMAND: run a command like a command block

Positions are given as a `c0de/minecraft/POSITION` struct with x, y, z and an optional dimension. An empty dimension means the dimension of the trigger block. Programs can never change the blocks on the sheets of the c0de dimension.

## Owner and permissions

Programs act on behalf of the owner of the trigger block. The world-changing functions above only work if the owner is an operator, or if the server allows them in the file `config/c0de.properties`.

Otherwise they raise PERMISSION_DENIED. Players can only be teleported if the owner is an operator, and only operators can run commands with COMMAND or give command blocks their data.

Three functions work for every owner, because they only do what a player can do:

- MOVE_ITEMS moves items between two chests the owner could open. The items travel, one stack per tick, and a filter like `$logs` or `minecraft:wheat` picks them.
- MINE_BLOCK breaks a block with the best tool from a chest (`tool_inventory`). The tool wears out and it takes as long as for a player. The drops go into another chest (`output_inventory`), or fall on the ground. If that chest is full, it raises INVENTORY_FULL.
- PLACE_BLOCK with the parameter `inventory` takes the block from a chest. If the chest doesn't contain it, it raises ITEM_MISSING.

## Making music

A short example: play a bell note at the trigger block.

```c0de
CALL c0de/minecraft/SOUND
    PARAMETER note 12 ;
    PARAMETER instrument "bell" ;
```

`note` goes from 0 to 24, like the clicks on a note block. Without `instrument`, SOUND uses the harp.

## c0de/security

Programs sometimes need a password or a key, e.g. for a web service. c0de keeps them out of sight in three ways:

- INPUT with `PARAMETER hide TRUE ;` shows `*` on the laptop while you type.
- Variable, constant and literal items can hide their value for good (**Hide the value** in their dialog).
- The tag `security: "sensitive"` on a variable or constant shows `***` in the debugging tool.

Hiding protects against people who look at your screen. Real secrets belong into your vault.

## Secrets in the vault

Every player has a vault while they are online. SET_SECRET puts a secret into it, and a SECRET line reads it, like a constant. Only the owner of the sheet can use the vault, and only when they start the program themselves, e.g. with a button.

A persistent vault keeps secrets when you leave. CREATE_VAULT makes one, OPEN_VAULT opens it. Your game asks for the password, and only your game can open the file; the server keeps it, but can't read it.

```c0de
PROGRAM keys
NOOP
INCLUDE c0de/security
NOOP
FUNCTION setup
BEGIN
    VARIABLE exists BOOLEAN
    VARIABLE key STRING
    CALL c0de/security/HAS_VAULT
        RESULT result exists
    IF exists ;
    BEGIN
        CALL c0de/security/OPEN_VAULT
    END
    ELSE
    BEGIN
        CALL c0de/security/CREATE_VAULT
    END IF
    CALL INPUT
        PARAMETER variable key ;
        PARAMETER hide TRUE ;
    CALL c0de/security/SET_SECRET
        PARAMETER name "apiKey" ;
        PARAMETER value key ;
        PARAMETER persistent TRUE ;
END FUNCTION
```

Another program then reads the key with `SECRET apiKey STRING`, after OPEN_VAULT.
