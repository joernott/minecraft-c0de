# Data types, variables and tags

[c0de concept](../concept.md) › Data types, variables and tags

## Data types

A data type defines which values a LITERAL, CONSTANT, VARIABLE, PARAMETER or RESULT can have. A type block always follows the LABEL of a constant, variable, parameter, result, struct element or FOR loop variable.

### Minecraft item

The type base is an item, not a block. It looks like a red cube with a black question mark. It is used to craft the type blocks.

#### Crafting

Eight type base items can be crafted from a red carpet surrounded by eight NOOP blocks.

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "AAA",
    "ABA",
    "AAA"
  ],
  "key": {
    "A": "c0de:noop_block",
    "B": "minecraft:red_carpet"
  },
  "result": {
    "id": "c0de:type_base",
    "count": 8
  }
}
```

### Simple data types

#### Boolean

A boolean is declared by the BOOLEAN keyword. It is the equivalent of a Java boolean. Its default value is false.

##### Minecraft block

A TYPE_BOOLEAN block is a red slab; lying on its top, as if in an invisible item frame, is a redstone torch. It can be crafted shapelessly from a type base and a redstone torch.

#### Integer

An integer is declared by the INTEGER keyword. It is the equivalent of a Java long (a signed 64 bit integer). Its default value is 0.

##### Minecraft block

A TYPE_INTEGER block is a red slab; lying on its top, as if in an invisible item frame, is a heap of yellow sand. The sand looks like sugar, but yellow. It can be crafted shapelessly from a type base and a sand block. Alternatively, it can be crafted shapelessly from a TYPE_FLOAT block and yellow dye.

#### Float

A float is declared by the FLOAT keyword. It is the equivalent of a Java double. Its default value is 0.0.

##### Minecraft block

A TYPE_FLOAT block is a red slab; lying on its top, as if in an invisible item frame, is a heap of orange sand. The sand looks like sugar, but orange. It can be crafted shapelessly from a type base and a red sand block. Alternatively, it can be crafted shapelessly from a TYPE_INTEGER block and orange dye.

#### Character

A character is declared by the CHAR keyword. It contains a single Unicode character (code point). Its default value is the space character.

##### Minecraft block

A TYPE_CHAR block is a red slab; lying on its top, as if in an invisible item frame, is an oak plank. It can be crafted shapelessly from a type base and an oak plank.

#### Time

A point in time is declared by the TIME keyword. It contains a Unix timestamp in milliseconds: the number of milliseconds since 1970-01-01 00:00:00 UTC as a signed 64 bit integer. Negative values are points in time before 1970. Its default value is 0 (1970-01-01 00:00:00 UTC).

A TIME value is shown as text in the ISO 8601 format in UTC with milliseconds, e.g. `2026-10-19T12:05:09.250Z`. This text representation is used by the ⊕ operator, by STR and by TO_JSON and TO_YAML. TIME values can be compared, and milliseconds can be added and subtracted (see [Type rules](05-expressions.md#type-rules)). The parts of a TIME value are read with the functions of the [c0de/time library](11-lib-time.md), and it is formatted with [FORMAT](10-lib-strings.md#format).

##### Minecraft block

A TYPE_TIME block is a red slab; lying on its top, as if in an invisible item frame, is a Minecraft clock. It can be crafted shapelessly from a type base and a clock.

### Complex data types

#### String

A string is declared by the STRING keyword. It is the equivalent of a Java String. The STRING keyword MAY be directly followed by an INTEGER literal that limits its maximum length. Its default value is the empty string. A single character of a string can be read with an INTEGER index starting at 0.

##### Minecraft block

A TYPE_STRING block is a red slab; lying on its top, as if in an invisible item frame, is a Minecraft string. It can be crafted shapelessly from a type base and a string.

#### Array

An array is declared by the ARRAY keyword. It is the equivalent of a Java list with a numerical index. The index always starts at 0, and there is no need to declare the size of an array: it grows and shrinks automatically. Its default value is the empty array.

The ARRAY keyword MUST be directly followed by another type keyword (the *subtype*) specifying its contents. If the subtype is STRUCT, it MUST be directly followed by a LABEL containing the name of a previously declared struct.

Multidimensional arrays can be created by declaring arrays of arrays. There MUST NOT be more than 8 dimensions.

##### Minecraft block

A TYPE_ARRAY block is a red slab; lying on its top, as if in an invisible item frame, is a Minecraft barrel. It can be crafted shapelessly from a type base and a barrel.

#### Hash

A hash is declared by the HASH keyword. It is the equivalent of an associative array with a STRING key. There is no need to declare the size of a hash: it grows and shrinks automatically. Its default value is the empty hash.

The HASH keyword MUST be directly followed by another type keyword (the *subtype*) specifying its contents. If the subtype is STRUCT, it MUST be directly followed by a LABEL containing the name of a previously declared struct.

Multidimensional hashes can be created by declaring hashes of hashes. There MUST NOT be more than 8 dimensions.

##### Minecraft block

A TYPE_HASH block is a red slab; lying on its top, as if in an invisible item frame, is a Minecraft chest. It can be crafted shapelessly from a type base and a chest, or alternatively from a type base and a trapped chest.

#### Struct

A struct is a data structure containing one or more named elements. The STRUCT keyword is used in two ways:
  - **Declaring a struct type**: A STRUCT statement in the global scope (x=5) consists of the STRUCT keyword directly followed by a LABEL with the unique name of the new type. No other block may follow. A code block MUST directly follow the STRUCT line. Each line of that code block consists of a LABEL with the name of an element, directly followed by a simple or complex type. If the type of an element is STRUCT, it MUST be directly followed by another LABEL containing the name of a struct that has been declared before. This is the only way of nesting structs. An element line MAY end with one or more [TAG](#tags) blocks that define the key name of the element in JSON or YAML. The code block ends with `END STRUCT`.
  - **Using a struct type**: In a VARIABLE, CONSTANT, PARAMETER, RESULT or element declaration and as subtype of an ARRAY or HASH, the STRUCT type block MUST be directly followed by a LABEL containing the name of a previously declared struct.

Elements of a struct value are accessed by the index of a label, e.g. `position.x`. The default value of a struct contains the default values of all its elements.

##### Minecraft block

A TYPE_STRUCT block is a red slab; lying on its top, as if in an invisible item frame, is a scaffolding block. It can be crafted shapelessly from a type base and a scaffolding block.

### Special data types

Currently, there is only one special data type: ERROR. Every function implicitly returns an error. An error is a predefined struct that contains the following elements:
  - an INTEGER `code`, which is 0 if there was no error
  - a STRING `message`, which is empty if there was no error
  - a STRING `context`, which contains the name of the function in which the error was raised
  - an INTEGER `line`, which contains the z coordinate of the line that produced the error

User-defined error codes MUST be 1000 or higher. Codes below 1000 are reserved for the [error codes](16-error-codes.md) of the language.

##### Minecraft block

A TYPE_ERROR block is a red slab; lying on its top, as if in an invisible item frame, is a burning campfire. It can be crafted shapelessly from a type base and a campfire.

### Built-in constants

Every program knows the following constants without an INCLUDE. Their names can't be used for other declarations.
  - The names of all [error codes](16-error-codes.md), e.g. `DIVISION_BY_ZERO` (INTEGER).
  - `COLOR_BLACK` to `COLOR_WHITE` (INTEGER): the numbers 0 to 15 of the colors of [COLOR](19-input-output.md#screen-functions).
  - `SHEET` of the built-in struct type `SHEET_INFO`: the sheet of the program the process runs.
  - `PID` of the built-in struct type `PID_INFO`: the process itself.
  - `CURSOR` of the built-in struct type `CURSOR_INFO`: the cursor at the moment the constant is read.

SHEET and PID are filled when a process starts and keep their values while it runs, also across a restart of the server. During validation and without a process (e.g. in tests of the interpreter), they contain the default values of their types. Like every constant, they can't be changed; they can be copied into a variable of their type, e.g. `VARIABLE info STRUCT SHEET_INFO`.

| Element of SHEET_INFO | Type | Content |
|---|---|---|
| number | INTEGER | The number of the sheet (0 for the sheet at x=0 to 31). |
| program | STRING | The name of the program. |
| owner | STRING | The name of the owner of the sheet, empty if it has none. |
| x, y, z | INTEGER | The position of the floor block at relative x=0, z=0 of the sheet, which shows its information. |

| Element of PID_INFO | Type | Content |
|---|---|---|
| pid | INTEGER | The PID of the process. |
| program | STRING | The name of the program. |
| function | STRING | The name of the entry function. |
| owner | STRING | The name of the owner of the trigger block, on whose behalf c0de/minecraft acts. |
| activator | STRING | The name of the player who started the process (pressed the button, stood on the pressure plate, clicked with the debugging tool), empty for redstone, mobs and timer blocks. |
| x, y, z | INTEGER | The position of the trigger block. |
| dimension | STRING | The dimension of the trigger block, e.g. `minecraft:overworld`. |
| debug | BOOLEAN | True for a process of a debug trigger block. |
| started | INTEGER | The game time of the dimension of the trigger block when the process started, in ticks (see TICKS of [c0de/minecraft](12-lib-minecraft.md)). |

Unlike the other constants, CURSOR changes while the process runs: every time it is read, it describes the cursor as it is in that moment. It can't be changed by the program either.

| Element of CURSOR_INFO | Type | Content |
|---|---|---|
| line | INTEGER | The line the cursor processes, i.e. the line that reads CURSOR. |
| function | STRING | The function the cursor is in. |
| depth | INTEGER | The number of functions on the stack: 1 in the entry function, 2 in a function it called, and so on. |
| stack_used | INTEGER | The number of used slots of the stack (variables, functions, code blocks and loops). |
| stack_size | INTEGER | The number of slots of the stack (setting `stackSize`); a process that needs more crashes with STACK_OVERFLOW. |

## Variables, constants and literals

### Variable

A variable definition is a statement that consists of a VARIABLE keyword directly followed by a valid, unique LABEL and a type. If that type is STRUCT, it MUST be directly followed by a LABEL containing the name of a previously declared struct. If it is ARRAY or HASH, it MUST be directly followed by its subtype.

A variable of a simple type or of type STRING MAY be initialized by following the type with a LITERAL of the same type. A variable of type ARRAY, HASH or STRUCT MAY be initialized by a code block directly after the VARIABLE line (see [Initializing complex values](#initializing-complex-values)). A variable that is not initialized contains the default value of its type.

The VARIABLE line MAY end with one or more [TAG](#tags) blocks, which define where the value of the variable is located in a JSON or YAML document.

#### Minecraft block

A VARIABLE block looks like a blue chest, as high as a slab. The [line number](01-dimension.md#line-numbers) shows whether the statement is valid.

It can be crafted shapelessly from a type base and a bone block. It can also be crafted shapelessly from a CONSTANT block.

### Constant

A constant definition is a statement that consists of a CONSTANT keyword directly followed by a valid, unique LABEL and a type. Its declaration follows the same rules as a variable declaration (including tags), but it MUST be initialized with a value. The value of a constant is immutable: a constant MUST NOT be the target of an ASSIGN statement, a CALL result or a FOR or EACH loop.

#### Minecraft block

A CONSTANT block looks like a blue barrel, as high as a slab. The [line number](01-dimension.md#line-numbers) shows whether the statement is valid.

It can be crafted shapelessly from a type base and three bones. It can also be crafted shapelessly from a VARIABLE block.

### Initializing complex values

To initialize an ARRAY, HASH or STRUCT, the line directly after the VARIABLE or CONSTANT line contains a BEGIN block at the same x coordinate as the VARIABLE or CONSTANT block. The initializer ends with `END VARIABLE` or `END CONSTANT`. Its content lines are indented by one block:
  - **Array**: Each line contains one or more LITERALs. They are read from left to right and from top to bottom and appended to the array. Each literal MUST have the subtype of the array. As in ASSIGN statements, an INTEGER literal MAY be used for a FLOAT and a CHAR literal for a STRING.
  - **Hash**: Each line contains exactly one LABEL with the key of an element, directly followed by a LITERAL with the value of that element. The literal MUST have the subtype of the hash.
  - **Struct**: Each line contains exactly one LABEL with the name of an element declared in the struct type, directly followed by a LITERAL of the type of that element. Elements that are not listed keep their default values.

Elements of a complex type are initialized by a nested initializer, up to a depth of 8:
  - In an array, a nested element consists of a line that contains only a BEGIN block, the content lines of the element (indented by one more block) and a line that contains only an END block.
  - In a hash or struct, a nested element consists of a line that contains only the LABEL with the key or element name, followed by a line with a BEGIN block at the same x coordinate as the label, the content lines and a line that contains only an END block.

Only literals can be used in initializers. Expressions are not allowed.

### Literal

A literal is similar to a constant without a name. It has a type and a value and can be used in expressions, initializers and wherever a value is expected. The type of a literal is BOOLEAN, INTEGER, FLOAT, CHAR, STRING or TIME. The value of a TIME literal is entered in the ISO 8601 format, e.g. `2026-10-19T12:05:09Z`, `2026-10-19T12:05:09.250Z` or `2026-10-19T14:05:09+02:00`. The milliseconds are optional. Without an offset, the time zone of the server is used.

#### Minecraft block

A LITERAL block is gray (color #AAAAAA) and as high as a slab; seen from above, it is an octagon, which distinguishes it from a LABEL block. Its value is written on its top.

Right-clicking a LITERAL block with an empty hand opens a dialog with a dropdown for the type and an input field for the value. The OK button is only enabled if the value is valid for the selected type.

Eight LITERAL blocks can be crafted from a white wool block surrounded by eight NOOP blocks.

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "AAA",
    "ABA",
    "AAA"
  ],
  "key": {
    "A": "c0de:noop_block",
    "B": "minecraft:white_wool"
  },
  "result": {
    "id": "c0de:literal_block",
    "count": 8
  }
}
```

## Tags

Tags are inspired by the struct tags of the Go programming language (`json:"name"`). They control how values are converted to and from JSON and YAML by the functions TO_JSON, TO_YAML, FROM_JSON and FROM_YAML of the [c0de/strings library](10-lib-strings.md).

A tag has two properties:
  - **language**: The language the tag applies to: `json` or `yaml`.
  - **name**: The name that is used in that language.

A TAG block MAY be placed
  - at the end of an element line of a STRUCT declaration, after the type (and subtype or struct name), or
  - at the end of a VARIABLE or CONSTANT line, after the type and the optional literal.

Several TAG blocks MAY follow each other, but there MUST NOT be more than one tag per language in a line. Tags MUST be placed before a COMMENT block at the end of the line.

In the text notation of the examples, a TAG block is written as `TAG(language: "name")`.

### Tags of struct elements

The tag of a struct element replaces the name of the element as key in the given language. Without a tag for a language, the element name is used. The name of an element tag MUST NOT be empty and MUST NOT contain a dot. The name `-` means that the element is skipped: it is not written by TO_JSON or TO_YAML and not read by FROM_JSON or FROM_YAML.

```
STRUCT player
BEGIN
    playerName STRING TAG(json: "name") TAG(yaml: "name")
    secret STRING TAG(json: "-")
END STRUCT
```

### Tags of variables and constants

The tag of a variable or constant defines the path of its value in a JSON or YAML document. The path consists of one or more keys separated by dots, e.g. `server.port`. A key MUST NOT be empty.
  - **TO_JSON / TO_YAML**: If the converted value is a variable or constant with a tag for the language, a matching structure of nested objects is created around the value. A variable `port` of type INTEGER with the value 25565 and the tag `json: server.port` is converted to `{"server": {"port": 25565}}`.
  - **FROM_JSON / FROM_YAML**: Only the value at exactly that path is extracted into the variable. With the same tag, the document `{"server": {"port": 25565, "motd": "Hello"}}` sets the variable to 25565.

A key that consists only of digits addresses the element with that index of an array (sequence), e.g. `servers.0.port`. When reading, such a key refers to a key of an object (mapping) if the value at that position is an object. When writing, it always creates an array, whose elements before the index are written as `null`. All other keys refer to object keys (JSON) or mapping keys (YAML).

### Minecraft block

A TAG block is a dark brown slab (color #553311) with a white price tag on its top, labeled with its language (`json` or `yaml`).

Right-clicking a TAG block with an empty hand opens a dialog with a dropdown for the language and an input field for the name. The OK button is only enabled if the name is valid for the position of the tag.

#### Crafting

Eight TAG blocks can be crafted from a brown wool block surrounded by eight NOOP blocks.

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "AAA",
    "ABA",
    "AAA"
  ],
  "key": {
    "A": "c0de:noop_block",
    "B": "minecraft:brown_wool"
  },
  "result": {
    "id": "c0de:tag_block",
    "count": 8
  }
}
```
