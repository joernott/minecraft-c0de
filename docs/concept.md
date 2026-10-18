# c0de

## Abstract

c0de is a simple interpreted programming language implemented as a Minecraft mod. The mod adds its own dimension in which players place blocks to assemble a program. All of these blocks can be crafted from vanilla materials. Programs are run by triggering special blocks that can be placed in any dimension.

## Introduction

c0de is inspired by the programming language BASIC and by the early Lego Mindstorms, where users programmed the "Brick" in an IDE by connecting colorful blocks that looked like Lego bricks. In the same spirit, a c0de program is written by placing c0de blocks in a special Minecraft dimension.

Programs can be imported and exported as a textual representation in a YAML file with the [c0de disk](#c0de-disk). The file can be edited in an IDE, but where is the fun in that?

### Requirement keywords

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in RFC 2119 and summarized below.

  - MUST: This word, or the terms "REQUIRED" or "SHALL", means that the definition is an absolute requirement of the specification.
  - MUST NOT: This phrase, or the phrase "SHALL NOT", means that the definition is an absolute prohibition of the specification.
  - SHOULD: This word, or the adjective "RECOMMENDED", means that there may be valid reasons in particular circumstances to ignore a particular item, but the full implications must be understood and carefully weighed before choosing a different course.
  - SHOULD NOT: This phrase, or the phrase "NOT RECOMMENDED", means that there may be valid reasons in particular circumstances when the particular behavior is acceptable or even useful, but the full implications should be understood and the case carefully weighed before implementing any behavior described with this label.
  - MAY: This word, or the adjective "OPTIONAL", means that an item is truly optional. One vendor may choose to include the item because a particular marketplace requires it or because the vendor feels that it enhances the product, while another vendor may omit the same item. An implementation which does not include a particular option MUST be prepared to interoperate with another implementation which does include the option, though perhaps with reduced functionality. In the same vein, an implementation which does include a particular option MUST be prepared to interoperate with another implementation which does not include the option (except, of course, for the feature the option provides).

### Glossary

After this nod to the famous terminology definition used in many RFCs, we return to our regular program:

  - **sheet**: An area of the c0de dimension that is two chunks (32 blocks) wide in x direction and extends from z=0 up to the maximum z coordinate. Sheet n covers the absolute x coordinates 32·n to 32·n+31 (n ≥ 0). A sheet holds at most one program.
  - **relative x coordinate**: The x coordinate relative to the western border of a sheet (absolute x modulo 32). Unless stated otherwise, x coordinates in this document are relative, y and z coordinates are absolute.
  - **code layer**: The layer y=-3, directly above the floor. All blocks of a program are placed on this layer.
  - **line**: All blocks of a sheet on the code layer that share the same z coordinate and lie between x=5 and x=28. A line is identified by its z coordinate. A line holds at most 24 blocks. This limit is intentional: together with the indentation, it encourages small functions and simple expressions.
  - **empty line**: A line that contains no blocks at all (only air). A line containing a NOOP block is *not* empty, even though it looks empty because the NOOP block is transparent.
  - **keyword**: A language element such as PROGRAM, VARIABLE or IF that is represented by its own block. Keywords are written in capital letters in this document.
  - **keyword block**: The first (westernmost) block of a line. It determines which statement the line belongs to.
  - **indentation**: The x coordinate of the keyword block of a line. A line is *indented by one block* relative to another line if the x coordinate of its keyword block is exactly one greater.
  - **statement**: One or more lines that together form a language construct as described in this document. A statement is valid if all of its lines comply with the rules for that statement.
  - **continuation line**: A line that belongs to the statement started in a line before it, e.g. the second line of an expression or the PARAMETER lines of a CALL statement.
  - **program**: The statements on one sheet. A program MUST start at z=0 with a PROGRAM statement that MUST be directly followed by a line containing a NOOP statement. A program ends at the first empty line. A program always runs south (from lower to higher z coordinates). See [Programs](#programs).
  - **process**: A running instance of a program. Processes are started by trigger blocks (see [Running programs](#running-programs)). A trigger block MUST NOT start another process while a process it started is still running. Several processes of the same program MAY run at the same time. They are independent of each other: every process has its own cursor and stack, so processes don't share any variables.
  - **process ID (PID)**: An integer that equals the y coordinate of the PID block and of the cursor of a process. Valid PIDs range from 0 to the maximum build height of the c0de dimension. Every program has its own sheet, so a PID is only unique within its program. A process is globally identified by the combination of program name and PID.
  - **cursor**: An entity that moves along the cursor rail (x=4) of a sheet and marks the line that is currently being processed. See [The cursor](#the-cursor).
  - **before / directly before**: A line with a lower z coordinate in the same program / the line at z-1.
  - **after / directly after**: A line with a higher z coordinate in the same program / the line at z+1.
  - **followed by / directly followed by**: A block in the same line with a higher x coordinate / the block at x+1 in the same line.
  - **code block**: A sequence of lines that starts with a BEGIN line and ends with the matching END line. See [Code blocks and scopes](#code-blocks-and-scopes).
  - **scope**: The region of a program in which a name is visible. The program itself forms the global scope, every code block forms a nested scope.
  - **unique**: A name is unique if it has not been defined in the current scope or in any of its parent scopes and is neither a keyword nor the name of a built-in type, constant or function.

## The c0de dimension

### Properties

The c0de dimension has the following properties:
  - no mob spawning
  - no sun and no daylight cycle
  - no weather
  - a cloudless blue sky
  - a constant ambient light level of 12
  - a floor at y=-4 made of the following unbreakable and uncraftable blocks:
    - white c0de bedrock (texture similar to white concrete, with an off-white color #F5F7F3)
    - light green c0de bedrock (texture similar to concrete, with a very bright green color #D2EBD7)
    - black c0de bedrock (texture similar to black concrete, color #000000)
    - cursor c0de bedrock (texture similar to gray concrete with a single rail in its middle, always running in z direction)
  - the code layer at y=-3, directly on top of the floor
  - a configurable maximum build height between y=15 and y=2031 (default y=255). Minecraft requires the bottom and the height of a dimension to be multiples of 16, so the dimension starts at y=-16 and the maximum build height MUST be a value of 16·k−1. The maximum build height limits the number of processes that can run for one program at the same time (PIDs 0 up to the maximum build height, 256 by default).

### Terrain generation

The floor of the dimension resembles 32 block (2 chunk) wide greenbar paper ("Tabellierpapier") running in north-south direction, starting at x=0, z=0.

All chunks with a negative x or a negative z coordinate have a floor consisting only of black c0de bedrock.

Chunks with positive x and z coordinates form a 2 chunk wide pattern:

![floor_c0de](./images/floor_c0de.svg)

This creates endless sheets of greenbar paper next to each other. Each sheet is 2 chunks wide in x direction and endless (up to the maximum z coordinate) in z direction. The relative x coordinate 4 of every sheet is made of cursor c0de bedrock and forms the cursor rail. Code is placed between x=5 and x=28.

## The c0de language

### Lines, statements and indentation

Every line starts with a keyword block, except for continuation lines and the content lines of [initializers](#initializing-complex-values) and [struct declarations](#struct). The keyword block determines the statement. The blocks following it (labels, types, literals, operators, …) complete the line.

A statement MAY span several lines. Lines that belong to a statement but are not its first line are either continuation lines, which are indented by one block relative to the first line, or form a code block, which starts at the same indentation as the first line.

Indentation follows these rules:
  - Lines in the global scope (PROGRAM, STRUCT, FUNCTION and global VARIABLE and CONSTANT statements) have their keyword block at x=5.
  - INCLUDE lines are indented by one block relative to the PROGRAM line (x=6).
  - A BEGIN line has the same indentation as the line of the statement that requires the code block.
  - All lines inside a code block are indented by one block relative to the BEGIN line, except for the matching END line, which has the same indentation as the BEGIN line.
  - Continuation lines (PARAMETER and RESULT lines of FUNCTION and CALL statements, additional lines of an expression) are indented by one block relative to the first line of their statement.

### Overview of keywords

| Keyword | Purpose | Section |
|---|---|---|
| NOOP | does nothing, separates and disables lines | [Doing nothing](#doing-nothing) |
| LABEL | names things and refers to them | [Naming with labels](#naming-with-labels) |
| PROGRAM, INCLUDE, EXIT | program header, libraries, ending a process | [Programs](#programs) |
| BEGIN, END, BREAK | code blocks, leaving loops | [Code blocks and scopes](#code-blocks-and-scopes) |
| BOOLEAN, INTEGER, FLOAT, CHAR, STRING, ARRAY, HASH, STRUCT, ERROR | data types | [Data types](#data-types) |
| VARIABLE, CONSTANT, LITERAL | values | [Variables, constants and literals](#variables-constants-and-literals) |
| OPERATOR (operators, parentheses, `;`) | expressions | [Expressions](#expressions), [Operators](#operators) |
| ASSIGN | changing a variable | [Assigning values](#assigning-values) |
| FUNCTION, PARAMETER, RESULT, RETURN, CALL | functions | [Functions](#functions) |
| IF, ELSE, FOR, EACH, WHILE, REPEAT | conditions and loops | [Structural statements](#structural-statements) |

A summary of all blocks, their colors and recipes can be found in the [block reference](#appendix-block-reference).

### Doing nothing

The NOOP (no operation) statement is the ideal of many people and organizations, especially governments. It is fast, efficient and does nothing. A valid NOOP statement is a single line whose keyword block is a NOOP block.

A line that starts with a NOOP block is skipped completely, including all blocks following the NOOP block. Placing a NOOP block in front of a line therefore disables ("comments out") that line. NOOP lines are also used to visually separate parts of a program, e.g. before every FUNCTION statement.

#### Minecraft block

The NOOP block is a fully transparent block with thin metallic corners and wooden edges between those corners.

##### Crafting

![recipe_noop_block](./images/recipe_noop_block-2.png)

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "ABA",
    "B B",
    "ABA"
  ],
  "key": {
    "A": "minecraft:iron_ingot",
    "B": "minecraft:stick"
  },
  "result": {
    "id": "c0de:noop_block",
    "count": 64
  }
}
```

### Naming with labels

Labels name programs, libraries, types, variables, constants, functions, parameters, results and struct elements, and they refer to those things later on. A valid name follows these rules:
  - It consists of one or more of the letters a-z and A-Z, the digits 0-9 and the underscore (_).
  - It MUST start with a letter.
  - It MUST NOT be a keyword. Names are compared to keywords case-insensitively.
  - It MUST be unique when it is defined.

Library names and qualified names of library elements are the only exception: they consist of several valid names separated by slashes, e.g. `c0de/net` or `c0de/net/send`.

A LABEL has the following properties:
  - **kind**: What the label names or refers to: PROGRAM, LIBRARY, TYPE, VARIABLE, CONSTANT, FUNCTION, PARAMETER, RESULT, ELEMENT (an element of a struct or a key of a hash initializer) or REFERENCE (a use of an already defined variable or constant).
  - **parent**: If the kind of the label is FUNCTION, this property MUST be set. It defines the parent of the function name:
    - for functions defined in the program, this is the program name
    - for built-in functions, this is an empty string
    - for functions coming from an include, it is the name of the mod followed by a slash and the name of the library in the mod, e.g. `c0de/net`.
  - **name**: Each label MUST have a name. The combination of parent and name MUST be unique.
  - **index**: The index MAY be used with complex types (STRING, ARRAY, HASH, STRUCT, ERROR) to reference one element of a value. It is a list of up to 8 index entries. Each entry is either a literal value (an INTEGER position for strings and arrays, a STRING key for hashes, an element name for structs and errors) or the name of an INTEGER or STRING variable or constant in scope whose value is used. Written as text, an index looks like `matrix[i][2]`, `inventory["diamond"]` or `error.code`.

A LABEL block MUST directly follow one of the following blocks:
  - a PROGRAM, INCLUDE, STRUCT, VARIABLE, CONSTANT, FUNCTION, PARAMETER, RESULT, CALL, ASSIGN, FOR, EACH or RETURN block
  - a STRUCT type block when a struct type is used for a variable, constant, parameter, result or element
  - an END block that is followed by a FUNCTION block (the label MAY repeat the function name there)
  - another LABEL block in a CALL, EACH or CALL RESULT line

A LABEL MAY also be the first block of a content line of a struct declaration or a hash or struct initializer, and it MAY be part of an expression, where it refers to a variable or constant.

#### Minecraft block

A LABEL block is white (color #FFFFFF) and shows the last part of its name (the part after the last slash) in black on all sides when it is positioned correctly and contains a valid name. Otherwise it is gray (color #AAAAAA). The index is not displayed on the block; all properties of a label can be inspected with the [debugging tool](#debugging-tool).

When a LABEL is placed, the game initializes its kind and parent properties from its position as far as possible.

##### Dialog

Right-clicking a LABEL block with an empty hand opens a dialog. Its layout depends on the position of the block:
  - next to a PROGRAM, STRUCT, VARIABLE, CONSTANT, FUNCTION, PARAMETER (in a FUNCTION statement), RESULT (in a FUNCTION statement), FOR or EACH block, or at the start of a struct or hash content line, an input field for a new name is shown
  - next to an INCLUDE block, a dropdown list of available libraries is shown
  - next to a CALL block, a dropdown list of the available functions (built-in, from included libraries and from the program) is shown
  - next to a PARAMETER or RESULT block in a CALL statement, a dropdown list of the parameters or results of the called function is shown
  - next to a STRUCT type block, a dropdown list of the known struct types is shown
  - in all other places (expressions, ASSIGN, RETURN, the error label of a CALL, the iterable of an EACH), a dropdown list of the variables and constants in scope is shown, together with an editor for the index

The input field shows the current name, which can be edited. If the name is valid, the OK button (a green check mark) can be clicked. If it is invalid, the button is disabled. Pressing Escape or clicking outside of the dialog closes it without making changes.

##### Crafting

One LABEL block can be crafted shapelessly from a NOOP block and a paper.

### Programs

A program is a list of statements placed on a sheet of greenbar paper. Programs are created by placing c0de blocks on that "paper" between the relative x coordinates 5 and 28. A program always starts at absolute z=0 and relative x=5 with a valid PROGRAM statement, followed by a NOOP statement. Programs always run in ascending z direction (north to south).

A program ends at the first empty line. A process MAY be ended prematurely by an EXIT statement.

A program has the following layout:
  1. a PROGRAM statement at z=0
  2. a NOOP statement at z=1
  3. zero or more INCLUDE statements
  4. any number of STRUCT, global VARIABLE, global CONSTANT and FUNCTION statements and NOOP lines in any order. A name can only be used after it has been defined.

The cursor processes the program from top to bottom until the entry function ends. As a consequence, global variables and constants declared after the entry function are never initialized, and functions declared after the entry function can't be called from it. This is intended: everything a function uses MUST be declared before it.

Executable statements (ASSIGN, CALL, IF, FOR, …) are only allowed inside functions. A process always runs one function of the program, the *entry function*, which is selected in the trigger block.

#### Program statement

A PROGRAM statement consists of the PROGRAM keyword directly followed by a LABEL containing the program name. The program name MUST be unique within the c0de dimension. The line directly after the PROGRAM statement MUST contain a NOOP statement.

#### Include statement

After the NOOP line, one or more INCLUDE statements MAY follow. An INCLUDE statement consists of the INCLUDE keyword directly followed by a LABEL specifying the library to include. A library name starts with the name of the mod that provides it, e.g. `c0de`, followed by a slash and the name of the library, e.g. `c0de/minecraft` or `c0de/net`. INCLUDE statements are always indented by one block relative to the PROGRAM statement (x=6). All INCLUDE statements MUST come before the first STRUCT, VARIABLE, CONSTANT or FUNCTION statement.

#### Exit statement

An EXIT statement ends the process immediately, no matter how deeply the cursor is nested in functions and code blocks. It consists of the EXIT keyword followed by an expression that evaluates to an INTEGER between 0 and 15. This value is the exit code of the process and is returned to the trigger block that started the process. EXIT statements are only allowed inside code blocks of functions.

The exit code 15 is also used for processes that crashed. Program authors MAY use `EXIT 15 ;` to signal a fatal run-time error in the same way.

#### Minecraft blocks

##### Program block

A PROGRAM block in a valid PROGRAM statement is dark gray (color #555555) with the picture of a Minecraft book on all six sides. If the statement is not valid, it is black (color #000000).

###### Crafting

A PROGRAM block can be crafted shapelessly from a NOOP block and a book.

##### Include block

An INCLUDE block in a valid INCLUDE statement is dark gray (color #555555) with the picture of a map on all six sides. If the statement is not valid, it is black (color #000000).

###### Crafting

An INCLUDE block can be crafted shapelessly from a NOOP block and an empty map.

##### Exit block

An EXIT block in a valid EXIT statement is dark gray (color #555555) with the picture of a Minecraft iron door on all six sides. If the statement is not valid, it is black (color #000000).

###### Crafting

Eight EXIT blocks can be crafted from an iron door surrounded by eight NOOP blocks.

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
    "B": "minecraft:iron_door"
  },
  "result": {
    "id": "c0de:exit_block",
    "count": 8
  }
}
```

### Code blocks and scopes

The language uses lexical scoping. A name can only be referenced after it has been defined and only if it was defined in the same scope or in a parent scope.

The global scope contains NOOP statements, struct types, global constants, global variables and functions. All other scopes are created by code blocks.

A valid code block consists of a BEGIN statement, one or more statements and the matching END statement. Code blocks MUST NOT contain STRUCT or FUNCTION statements. A code block MUST directly follow
  - the header of a FUNCTION statement (after its last PARAMETER or RESULT line),
  - a STRUCT statement,
  - an IF, ELSE, FOR, EACH, WHILE or REPEAT line, or
  - a VARIABLE or CONSTANT line of a complex type, where it contains the initial value (see [Initializing complex values](#initializing-complex-values)).

Variables and constants declared inside a code block can only be used inside that block and are forgotten when the block ends. A code block has access to all variables and constants of its parent scopes that have been declared before it.

#### Beginning a code block

The BEGIN statement consists of a single BEGIN keyword. It MUST have the same indentation as the line of the statement that requires the code block.

##### Minecraft block

A BEGIN block is gold (color #FFAA00) if there is no matching valid END statement and yellow (color #FFFF55) if there is one. The top side of a BEGIN block shows a trapezoid, whose long side faces in positive z direction. The trapezoid is yellow on a gold block and gold on a yellow block.

###### Crafting

Eight BEGIN blocks can be crafted from a yellow wool block surrounded by eight NOOP blocks.

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
    "B": "minecraft:yellow_wool"
  },
  "result": {
    "id": "c0de:begin_block",
    "count": 8
  }
}
```

One BEGIN block can also be crafted shapelessly from one END block.

###### Placement

A BEGIN block can only be placed as part of a statement that requires a code block (see the list above). It MUST be placed at the same x coordinate as the keyword block of that statement. The lines following the BEGIN line MUST be indented by one block, and the matching END block MUST be placed at the same x coordinate as the BEGIN block.

#### Ending a code block

The END statement consists of the END keyword at the same indentation as its matching BEGIN statement. It MUST be directly followed by the keyword block of the statement that required the code block, e.g. `END FUNCTION`, `END WHILE` or `END VARIABLE`. It ends the scope started by the BEGIN statement.

There are two exceptions:
  - The END of the first code block of an IF statement that has an ELSE part stands alone; the following line MUST be the ELSE line. Only the last END of the whole condition is followed by an IF block (see [Conditional execution](#conditional-execution)).
  - The END of a nested initializer stands alone (see [Initializing complex values](#initializing-complex-values)).

The END of a REPEAT loop is additionally followed by the loop condition, the END of a FUNCTION MAY additionally be followed by a LABEL repeating the function name.

##### Minecraft block

The END block has the same colors as the BEGIN block, but the long side of its trapezoid faces in negative z direction.

###### Crafting

One END block can be crafted shapelessly from one BEGIN block.

###### Placement

An END block can only be placed at the same x coordinate as the matching BEGIN block. It MUST NOT be placed directly after the BEGIN block, as a code block MUST contain at least one statement (a NOOP statement is sufficient).

#### Leaving a loop prematurely

A BREAK statement prematurely ends the innermost loop (FOR, EACH, WHILE or REPEAT) that contains it, including all code blocks nested inside that loop. It consists of the keyword BREAK, which MAY be followed by an expression that evaluates to a BOOLEAN. If that expression evaluates to false, the BREAK statement is ignored. A BREAK statement outside of a loop is invalid.

##### Minecraft block

The BREAK block has the same colors as the BEGIN and END blocks but shows a broken stick in a circle instead of a trapezoid.

###### Crafting

One BREAK block can be crafted shapelessly from a stick and a NOOP block.

### Data types

A data type defines which values a LITERAL, CONSTANT, VARIABLE, PARAMETER or RESULT can have. A type block always follows the LABEL of a constant, variable, parameter, result, struct element or FOR loop variable.

#### Minecraft item

The type base is an item, not a block. It looks like a red cube with a black question mark on every side. It is used to craft the type blocks.

##### Crafting

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

#### Simple data types

##### Boolean

A boolean is declared by the BOOLEAN keyword. It is the equivalent of a Java boolean. Its default value is false.

###### Minecraft block

A TYPE_BOOLEAN block is a red block with a white octagon on every side that contains a redstone torch. It can be crafted shapelessly from a type base and a redstone torch.

##### Integer

An integer is declared by the INTEGER keyword. It is the equivalent of a Java long (a signed 64 bit integer). Its default value is 0.

###### Minecraft block

A TYPE_INTEGER block is a red block with a white octagon on every side that contains a heap of yellow sand. The sand looks like sugar, but yellow. It can be crafted shapelessly from a type base and a sand block. Alternatively, it can be crafted shapelessly from a TYPE_FLOAT block and yellow dye.

##### Float

A float is declared by the FLOAT keyword. It is the equivalent of a Java double. Its default value is 0.0.

###### Minecraft block

A TYPE_FLOAT block is a red block with a white octagon on every side that contains a heap of orange sand. The sand looks like sugar, but orange. It can be crafted shapelessly from a type base and a red sand block. Alternatively, it can be crafted shapelessly from a TYPE_INTEGER block and orange dye.

##### Character

A character is declared by the CHAR keyword. It contains a single Unicode character (code point). Its default value is the space character.

###### Minecraft block

A TYPE_CHAR block is a red block with a white octagon on every side that contains an oak plank. It can be crafted shapelessly from a type base and an oak plank.

#### Complex data types

##### String

A string is declared by the STRING keyword. It is the equivalent of a Java String. The STRING keyword MAY be directly followed by an INTEGER literal that limits its maximum length. Its default value is the empty string. A single character of a string can be read with an INTEGER index starting at 0.

###### Minecraft block

A TYPE_STRING block is a red block with a white octagon on every side that contains a Minecraft string. It can be crafted shapelessly from a type base and a string.

##### Array

An array is declared by the ARRAY keyword. It is the equivalent of a Java list with a numerical index. The index always starts at 0, and there is no need to declare the size of an array: it grows and shrinks automatically. Its default value is the empty array.

The ARRAY keyword MUST be directly followed by another type keyword (the *subtype*) specifying its contents. If the subtype is STRUCT, it MUST be directly followed by a LABEL containing the name of a previously declared struct.

Multidimensional arrays can be created by declaring arrays of arrays. There MUST NOT be more than 8 dimensions.

###### Minecraft block

A TYPE_ARRAY block is a red block with a white octagon on every side that contains a Minecraft barrel. It can be crafted shapelessly from a type base and a barrel.

##### Hash

A hash is declared by the HASH keyword. It is the equivalent of an associative array with a STRING key. There is no need to declare the size of a hash: it grows and shrinks automatically. Its default value is the empty hash.

The HASH keyword MUST be directly followed by another type keyword (the *subtype*) specifying its contents. If the subtype is STRUCT, it MUST be directly followed by a LABEL containing the name of a previously declared struct.

Multidimensional hashes can be created by declaring hashes of hashes. There MUST NOT be more than 8 dimensions.

###### Minecraft block

A TYPE_HASH block is a red block with a white octagon on every side that contains a Minecraft chest. It can be crafted shapelessly from a type base and a chest, or alternatively from a type base and a trapped chest.

##### Struct

A struct is a data structure containing one or more named elements. The STRUCT keyword is used in two ways:
  - **Declaring a struct type**: A STRUCT statement in the global scope (x=5) consists of the STRUCT keyword directly followed by a LABEL with the unique name of the new type. No other block may follow. A code block MUST directly follow the STRUCT line. Each line of that code block consists of a LABEL with the name of an element, directly followed by a simple or complex type. If the type of an element is STRUCT, it MUST be directly followed by another LABEL containing the name of a struct that has been declared before. This is the only way of nesting structs. The code block ends with `END STRUCT`.
  - **Using a struct type**: In a VARIABLE, CONSTANT, PARAMETER, RESULT or element declaration and as subtype of an ARRAY or HASH, the STRUCT type block MUST be directly followed by a LABEL containing the name of a previously declared struct.

Elements of a struct value are accessed by the index of a label, e.g. `position.x`. The default value of a struct contains the default values of all its elements.

###### Minecraft block

A TYPE_STRUCT block is a red block with a white octagon on every side that contains a scaffolding block. It can be crafted shapelessly from a type base and a scaffolding block.

#### Special data types

Currently, there is only one special data type: ERROR. Every function implicitly returns an error. An error is a predefined struct that contains the following elements:
  - an INTEGER `code`, which is 0 if there was no error
  - a STRING `message`, which is empty if there was no error
  - a STRING `context`, which contains the name of the function in which the error was raised
  - an INTEGER `line`, which contains the z coordinate of the line that produced the error

User-defined error codes MUST be 1000 or higher. Codes below 1000 are reserved for the [error codes](#error-codes) of the language.

###### Minecraft block

A TYPE_ERROR block is a red block with a white octagon on every side that contains a burning campfire. It can be crafted shapelessly from a type base and a campfire.

### Variables, constants and literals

#### Variable

A variable definition is a statement that consists of a VARIABLE keyword directly followed by a valid, unique LABEL and a type. If that type is STRUCT, it MUST be directly followed by a LABEL containing the name of a previously declared struct. If it is ARRAY or HASH, it MUST be directly followed by its subtype.

A variable of a simple type or of type STRING MAY be initialized by following the type with a LITERAL of the same type. A variable of type ARRAY, HASH or STRUCT MAY be initialized by a code block directly after the VARIABLE line (see [Initializing complex values](#initializing-complex-values)). A variable that is not initialized contains the default value of its type.

##### Minecraft block

A VARIABLE block in a valid VARIABLE statement is blue (color #5555FF) with a white square on every side. Otherwise its color is dark blue (color #0000AA).

It can be crafted shapelessly from a type base and a bone block. It can also be crafted shapelessly from a CONSTANT block.

#### Constant

A constant definition is a statement that consists of a CONSTANT keyword directly followed by a valid, unique LABEL and a type. Its declaration follows the same rules as a variable declaration, but it MUST be initialized with a value. The value of a constant is immutable: a constant MUST NOT be the target of an ASSIGN statement, a CALL result or a FOR or EACH loop.

##### Minecraft block

A CONSTANT block in a valid CONSTANT statement is blue (color #5555FF) with a white diamond on every side. Otherwise its color is dark blue (color #0000AA).

It can be crafted shapelessly from a type base and three bones. It can also be crafted shapelessly from a VARIABLE block.

#### Initializing complex values

To initialize an ARRAY, HASH or STRUCT, the line directly after the VARIABLE or CONSTANT line contains a BEGIN block at the same x coordinate as the VARIABLE or CONSTANT block. The initializer ends with `END VARIABLE` or `END CONSTANT`. Its content lines are indented by one block:
  - **Array**: Each line contains one or more LITERALs. They are read from left to right and from top to bottom and appended to the array. Each literal MUST have the subtype of the array.
  - **Hash**: Each line contains exactly one LABEL with the key of an element, directly followed by a LITERAL with the value of that element. The literal MUST have the subtype of the hash.
  - **Struct**: Each line contains exactly one LABEL with the name of an element declared in the struct type, directly followed by a LITERAL of the type of that element. Elements that are not listed keep their default values.

Elements of a complex type are initialized by a nested initializer, up to a depth of 8:
  - In an array, a nested element consists of a line that contains only a BEGIN block, the content lines of the element (indented by one more block) and a line that contains only an END block.
  - In a hash or struct, a nested element consists of a line that contains only the LABEL with the key or element name, followed by a line with a BEGIN block at the same x coordinate as the label, the content lines and a line that contains only an END block.

Only literals can be used in initializers. Expressions are not allowed.

#### Literal

A literal is similar to a constant without a name. It has a type and a value and can be used in expressions, initializers and wherever a value is expected. The type of a literal is BOOLEAN, INTEGER, FLOAT, CHAR or STRING.

##### Minecraft block

A LITERAL block is light gray (color #CCCCCC) when it has a value that matches its type. Otherwise its color is gray (color #AAAAAA). Its value is written on all six sides.

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

### Expressions

An expression is a combination of literals, variables, constants and operators that can be evaluated to a result.

A primitive expression is
  - a LITERAL,
  - a LABEL referring to a variable or constant (optionally with an index).

A complex expression is
  - a unary operator directly followed by an expression,
  - an expression followed by a binary operator and another expression, or
  - an opening parenthesis, an expression and a closing parenthesis.

An expression is read from left to right (in ascending x order). Operations are executed in the order of their [precedence](#precedence). Operators with the same precedence are executed from left to right, except for exponentiation (^), which is executed from right to left.

The end of an expression MUST be marked by an OPERATOR block with the end-of-expression symbol `;`. This allows several expressions in one line (e.g. in a FOR statement). An expression MAY span several lines: if a line ends before the `;` block is reached, the expression continues on the following continuation line, which is indented by one block relative to the first line of the statement.

Function calls are not part of expressions. A function is always called with a [CALL statement](#calling-functions).

#### Type rules

  - Arithmetic operators accept INTEGER and FLOAT operands. If one operand is a FLOAT, the result is a FLOAT. Division (/) always returns a FLOAT. Modulus (`\`) requires INTEGER operands.
  - Comparison operators require operands of the same type. INTEGER and FLOAT can be compared with each other. CHAR and STRING values are compared lexicographically. Only = and ≠ can be used with BOOLEAN, ARRAY, HASH, STRUCT and ERROR operands.
  - Logical operators require BOOLEAN operands and return a BOOLEAN.
  - Bitwise operators require INTEGER operands and return an INTEGER.
  - The concatenation operator (⊕) converts both operands to their text representation and returns a STRING.

Applying an operator to operands of the wrong type raises a TYPE_MISMATCH error.

### Operators

#### Minecraft block

All operators, the parentheses and the end-of-expression symbol (`;`) are represented by a single OPERATOR block. Its symbol is chosen in a dialog that opens when the block is right-clicked with an empty hand. The dialog shows a dropdown with all operator symbols, `(`, `)` and `;`. A newly crafted OPERATOR block has no symbol yet.

An OPERATOR block in a valid expression is red (color #FF5555) and has a white circle on all sides that contains its symbol in dark red (color #AA0000). If the expression is incomplete or the block has no symbol yet, the block is dark red (color #AA0000).

##### Crafting

Eight OPERATOR blocks can be crafted from a red wool block surrounded by eight NOOP blocks.

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
    "B": "minecraft:red_wool"
  },
  "result": {
    "id": "c0de:operator_block",
    "count": 8
  }
}
```

#### Grouping

Parts of an expression can be grouped with parentheses ( and ). Parentheses can be nested up to a depth of 8 levels.

#### Arithmetic operators

  - Addition: +
  - Subtraction: - (also unary negation)
  - Multiplication: *
  - Division: /
  - Exponentiation: ^
  - Modulus (remainder): `\`

#### Comparison operators

  - Equal: =
  - Not equal: ≠
  - Greater than: >
  - Less than: <
  - Greater or equal: ≥
  - Less or equal: ≤

The equal sign is only a comparison operator. Values are assigned with the [ASSIGN statement](#assigning-values).

#### Logical operators

  - NOT (unary)
  - AND
  - OR
  - XOR

#### String operators

  - Concatenation: ⊕

#### Bitwise operators

  - Bitwise and: &
  - Bitwise or: |
  - Bitwise exclusive or: ⊻
  - Bitwise negation: ¬ (unary)
  - Shift left: ←
  - Shift right: → (arithmetic shift, keeps the sign)

#### Precedence

From highest to lowest:
  1. Parentheses: ( )
  2. Bitwise negation: ¬
  3. Logical negation: NOT
  4. Exponentiation: ^
  5. Unary minus: - (e.g. -X)
  6. Multiplication and division: *, /
  7. Modulus: `\`
  8. Addition and subtraction: +, -
  9. Shift: ←, →
  10. String concatenation: ⊕
  11. Comparison: =, ≠, <, >, ≤, ≥
  12. Bitwise and: &
  13. Bitwise exclusive or: ⊻
  14. Bitwise or: |
  15. Logical AND
  16. Logical XOR
  17. Logical OR

### Assigning values

An ASSIGN statement assigns a value to a variable. The variable MUST be defined before it can be assigned a value.

An ASSIGN statement consists of the ASSIGN keyword, directly followed by a LABEL referring to the target variable (optionally with an index), directly followed by an expression.

The value of the expression MUST match the type of the target. An INTEGER value MAY be assigned to a FLOAT target and a CHAR value MAY be assigned to a STRING target; all other combinations of different types raise a TYPE_MISMATCH error. Complex values are copied: after `ASSIGN a b;`, changing `a` does not change `b`.

Assigning to the index of an array that equals the length of the array appends a new element. Assigning to a key of a hash that does not exist yet adds a new element.

#### Minecraft block

ASSIGN blocks are aqua (color #55FFFF). If the ASSIGN statement is not valid, the block is dark aqua (color #00AAAA). All six sides show a black equilateral triangle with one corner pointing towards positive x.

##### Crafting

Eight ASSIGN blocks can be crafted from a cyan wool block surrounded by eight NOOP blocks.

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
    "B": "minecraft:cyan_wool"
  },
  "result": {
    "id": "c0de:assign_block",
    "count": 8
  }
}
```

### Functions

#### Declaring functions

A function is a multi-line statement in the global scope. The line directly before it MUST be a NOOP line. It starts with a line that contains the FUNCTION keyword at x=5, directly followed by a valid, unique LABEL with the function name.

The following lines MAY define zero or more parameters. Each parameter line starts with the PARAMETER keyword, indented by one block relative to the FUNCTION keyword. The PARAMETER keyword MUST be directly followed by a valid LABEL and a type. If the type is a simple type or STRING, the parameter MAY have a default value, defined by following the type with a LITERAL.

The calling entity MUST set the value of every parameter that has no default value. Parameters are always passed by value: they contain a copy of the value they were called with and behave like variables declared in the scope of the function.

After the parameters, zero or more result lines MAY be defined. Each result line starts with the RESULT keyword, indented by one block relative to the FUNCTION keyword. It MUST be directly followed by a LABEL and a type. A result behaves like a variable declared in the scope of the function and is initialized with the default value of its type. When the function ends, the last value assigned to a result is returned to the calling entity.

After that, a valid code block containing the statements of the function MUST follow. Its BEGIN block is at x=5. The END keyword of that block MUST be directly followed by a FUNCTION keyword, which MAY be followed by a LABEL repeating the function name. The code block of a function can contain nested code blocks, but neither STRUCT nor FUNCTION statements.

#### Returning from functions

The code block of a function MAY contain RETURN statements. A RETURN statement immediately ends all code blocks nested in the code block of the function and ends the function. It consists of the RETURN keyword, which MAY be directly followed by a LABEL referring to a previously defined variable or constant of type ERROR. That error is returned to the calling entity. Without such a label, the function returns an error with code 0.

#### Calling functions

A CALL statement calls a function. Its first line contains the CALL keyword, directly followed by a LABEL containing the name of the function. A second LABEL MAY follow, referring to a variable of type ERROR that receives the error returned by the called function.

A called function MUST be a built-in function, a function of an included library or a program function that has been declared before the CALL statement. A function MAY also call itself, which enables recursion.

The lines directly after the CALL line provide the parameters. They are indented by one block relative to the CALL keyword. Each parameter line consists of the PARAMETER keyword, directly followed by a LABEL with the name of a parameter of the called function, directly followed by an expression that provides its value. Every parameter without a default value MUST be provided, parameters with a default value MAY be provided. Each parameter MUST NOT be provided more than once.

After the parameter lines (or directly after the CALL line, if no parameters are provided), result lines MAY follow, indented the same way. Each result line consists of the RESULT keyword, directly followed by a LABEL with the name of a result of the called function, directly followed by a LABEL referring to a variable that receives the value of that result. Results that are not listed are discarded.

If the called function returns an error with a code other than 0 and the CALL statement has no error label, the error is raised in the calling function (see [Error handling](#error-handling)).

#### Minecraft blocks

##### Function block

The FUNCTION block is a structural block. It is light gray (color #AAAAAA) when it is directly followed by a LABEL block with a valid, unique name and dark gray (color #555555) otherwise. All sides show a white circle with the letter ƒ (symbol for function) in it. It can only be placed at x=5 and only if the line directly before it (z-1) contains a NOOP statement.

###### Crafting

A FUNCTION block can be crafted shapelessly from a NOOP block and a stone block.

##### Parameter block

The PARAMETER block is a dependent block of the FUNCTION and CALL blocks. It can only be placed in the line directly after a FUNCTION or CALL line or after another PARAMETER line, indented by one block relative to the FUNCTION or CALL block. It looks like a hopper. Its color is dark purple (color #AA00AA) when the parameter line is incomplete and light purple (color #FF55FF) when it is complete.

###### Crafting

Eight PARAMETER blocks can be crafted from a hopper surrounded by eight NOOP blocks.

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
    "B": "minecraft:hopper"
  },
  "result": {
    "id": "c0de:parameter_block",
    "count": 8
  }
}
```

One PARAMETER block can also be crafted shapelessly from one RESULT block.

##### Result block

The RESULT block is a dependent block of the FUNCTION and CALL blocks. It can only be placed in the line directly after a FUNCTION or CALL line, a PARAMETER line or another RESULT line, indented by one block relative to the FUNCTION or CALL block. It looks like a hopper pointing upwards. Its color is dark purple (color #AA00AA) when the result line is incomplete and light purple (color #FF55FF) when it is complete.

###### Crafting

One RESULT block can be crafted shapelessly from one PARAMETER block.

##### Return block

The RETURN block is dark purple (color #AA00AA) when the RETURN statement is invalid and light purple (color #FF55FF) when it is valid. All six sides show a spruce door.

###### Crafting

Eight RETURN blocks can be crafted from a spruce door surrounded by eight NOOP blocks.

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
    "B": "minecraft:spruce_door"
  },
  "result": {
    "id": "c0de:return_block",
    "count": 8
  }
}
```

##### Call block

The CALL block is a structural block. It is light gray (color #AAAAAA) when it forms a valid CALL statement and dark gray (color #555555) otherwise. All sides show a white circle with the symbol 📞 (telephone) in it.

###### Crafting

A CALL block can be crafted shapelessly from a NOOP block and a smooth stone block.

### Built-in functions

The built-in functions are always available without an INCLUDE statement. Their parent is the empty string. They are called with a CALL statement like any other function, using named PARAMETER and RESULT lines:

```
CALL MID
    PARAMETER text name ;
    PARAMETER start 2 ;
    PARAMETER length 3 ;
    RESULT result part
```

Most of them are taken from AmigaBASIC. AmigaBASIC functions that deal with files, graphics, memory, input devices, the screen, menus or speech output have no equivalent in c0de. Functions that interact with Minecraft are part of the [c0de/minecraft library](#the-c0deminecraft-library). The `$` suffix of the AmigaBASIC string function names is dropped, as `$` is not allowed in names.

Conventions:
  - A function with a single result names it `result`.
  - Some parameters accept several types, written as `INTEGER|FLOAT`. Where the result type is written as "like `value`", the result has the type of that parameter.
  - Parameters are passed by value, so functions that change a string, array or hash return the changed value as `result`. To change a variable, use the same variable as parameter and result target.
  - Positions in strings and arrays start at 0, unlike AmigaBASIC, where string positions start at 1.
  - An invalid argument (e.g. the square root of a negative number) raises INVALID_ARGUMENT unless stated otherwise.

#### Numeric functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| ABS | value INTEGER\|FLOAT | result like `value` | Absolute value. | ABS |
| SGN | value INTEGER\|FLOAT | result INTEGER | -1 if `value` < 0, 0 if `value` = 0, 1 if `value` > 0. | SGN |
| SQR | value INTEGER\|FLOAT | result FLOAT | Square root. `value` MUST NOT be negative. | SQR |
| EXP | value INTEGER\|FLOAT | result FLOAT | e raised to the power of `value`. | EXP |
| LOG | value INTEGER\|FLOAT | result FLOAT | Natural logarithm. `value` MUST be greater than 0. | LOG |
| SIN | value INTEGER\|FLOAT | result FLOAT | Sine of an angle in radians. | SIN |
| COS | value INTEGER\|FLOAT | result FLOAT | Cosine of an angle in radians. | COS |
| TAN | value INTEGER\|FLOAT | result FLOAT | Tangent of an angle in radians. | TAN |
| ATN | value INTEGER\|FLOAT | result FLOAT | Arc tangent in radians. | ATN |
| INT | value FLOAT | result INTEGER | Largest integer less than or equal to `value` (rounds down: -12.85 → -13). | INT |
| FIX | value FLOAT | result INTEGER | Removes the decimal places without rounding (-12.85 → -12). | FIX |
| ROUND | value FLOAT | result INTEGER | Rounds to the nearest integer, .5 to the nearest even integer (12.5 → 12, 13.5 → 14). | CINT, CLNG |
| TO_FLOAT | value INTEGER | result FLOAT | Converts an integer to a float. | CDBL, CSNG |
| DIV | dividend INTEGER, divisor INTEGER | result INTEGER | Integer division, rounded towards 0. A divisor of 0 raises DIVISION_BY_ZERO. | – |
| RANDOM | – | result FLOAT | Next random number between 0 (inclusive) and 1 (exclusive). Each process has its own random number generator. | RND |
| RANDOMIZE | seed INTEGER | – | Sets the seed of the random number generator of the process, so that the same seed always produces the same sequence. | RANDOMIZE |

#### String and character functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| LEN | value STRING\|ARRAY\|HASH | result INTEGER | Number of characters of a string or number of elements of an array or hash. | LEN, UBOUND |
| LEFT | text STRING, length INTEGER | result STRING | The first `length` characters of `text`. | LEFT$ |
| RIGHT | text STRING, length INTEGER | result STRING | The last `length` characters of `text`. | RIGHT$ |
| MID | text STRING, start INTEGER, length INTEGER (default: rest of the text) | result STRING | `length` characters of `text`, beginning at position `start`. | MID$ function |
| REPLACE | text STRING, start INTEGER, insert STRING | result STRING | Overwrites the characters of `text` beginning at position `start` with `insert`. The length of `text` does not change; characters of `insert` that would extend it are dropped. | MID$ statement |
| INSTR | text STRING, search STRING, start INTEGER (default 0) | result INTEGER | Position of the first occurrence of `search` in `text`, searching from position `start`. -1 if it is not found. | INSTR |
| UCASE | text STRING | result STRING | `text` with all lowercase letters converted to uppercase. | UCASE$ |
| LCASE | text STRING | result STRING | `text` with all uppercase letters converted to lowercase. | – |
| SPACE | count INTEGER | result STRING | A string of `count` spaces. | SPACE$ |
| FILL | count INTEGER, char CHAR | result STRING | A string of `count` copies of `char`. | STRING$ |
| ASC | char CHAR | result INTEGER | The Unicode code point of `char`. | ASC |
| CHR | code INTEGER | result CHAR | The character with the Unicode code point `code`. | CHR$ |
| STR | value BOOLEAN\|INTEGER\|FLOAT\|CHAR | result STRING | Text representation of `value`, the same as used by the ⊕ operator. | STR$ |
| VAL | text STRING | value FLOAT, isInteger BOOLEAN | Converts a text to a number. `isInteger` is true if the text contains an integer number, which can be converted with FIX without loss. Raises INVALID_ARGUMENT if the text is not a number. | VAL |
| HEX | value INTEGER | result STRING | Hexadecimal representation of `value`. | HEX$ |
| OCT | value INTEGER | result STRING | Octal representation of `value`. | OCT$ |

#### Array and hash functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| HAS_KEY | hash HASH, key STRING | result BOOLEAN | True if `hash` contains `key`. | – |
| REMOVE | container ARRAY\|HASH, index INTEGER (arrays) or key STRING (hashes) | result like `container` | `container` without the element. Following array elements move down by one. A missing index raises INDEX_OUT_OF_BOUNDS, a missing key raises UNKNOWN_KEY. | – |
| INSERT | array ARRAY, index INTEGER, value (subtype of `array`) | result like `array` | `array` with `value` inserted at position `index`. Following elements move up by one. `index` MUST be between 0 and the length of the array. | – |
| CLEAR | container ARRAY\|HASH | result like `container` | An empty array or hash of the same type. | ERASE |
| SWAP | a (any type), b (same type as `a`) | a, b | Returns the two values swapped. | SWAP |

#### Time functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| TIMER | – | result FLOAT | Seconds since midnight (real time of the server). | TIMER |
| DATE | – | result STRING | Current date of the server in the format `YYYY-MM-DD`. | DATE$ |
| TIME | – | result STRING | Current time of the server in the format `hh:mm:ss`. | TIME$ |
| SLEEP | ticks INTEGER | – | Pauses the cursor for `ticks` game ticks. `ticks` MUST be between 1 and 72000 (one hour). | SLEEP |

#### Error functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| RAISE | code INTEGER, message STRING (default empty) | – | Returns an error with `code` and `message` to the caller, which raises it or stores it in the error label of the CALL statement. `code` MUST be 1000 or higher. The `context` and `line` of the error are those of the CALL statement. | ERROR |

### The c0de/minecraft library

All functions that interact with Minecraft are part of the optional library `c0de/minecraft`, which is provided by the c0de mod. A program that uses them MUST include the library with `INCLUDE c0de/minecraft`. Its functions are called by their qualified names:

```
CALL c0de/minecraft/SOUND
    PARAMETER note 12 ;
    PARAMETER instrument "bell" ;
```

The library follows the same conventions as the built-in functions.

#### Types

The library provides the following struct types:

| Type | Elements | Description |
|---|---|---|
| `c0de/minecraft/POSITION` | x INTEGER, y INTEGER, z INTEGER, dimension STRING | A block position. An empty `dimension` means the dimension of the trigger block. Otherwise it contains a dimension ID such as `minecraft:the_nether`. |
| `c0de/minecraft/ITEM_STACK` | id STRING, count INTEGER | The content of an inventory slot. An empty slot has the id `""` and the count 0. |
| `c0de/minecraft/ENTITY` | uuid STRING, type STRING, name STRING, position STRUCT `c0de/minecraft/POSITION` | An entity. `position` is the block position the entity is in. |

#### Owner and permissions

Every trigger block has an owner: the player who configured it last. All functions of the library act on behalf of this player.

Functions that change the world are restricted. A function of a restricted group can only be used if the owner is an administrator (operator with permission level 2 or higher) or if the group is enabled in the configuration file of the mod:

| Configuration key | Default | Functions |
|---|---|---|
| `allowInventoryChanges` | false | MOVE_ITEMS, DELETE_ITEMS |
| `allowBlockChanges` | false | PLACE_BLOCK, REMOVE_BLOCK |
| `allowSpawning` | false | SPAWN_ITEM, SPAWN_ENTITY |
| `allowTeleporting` | false | TELEPORT |

Calling a restricted function without the permission raises PERMISSION_DENIED. Independent of the configuration,
  - a player can only be teleported if the owner is an administrator,
  - blocks on the sheets of the c0de dimension can't be changed by any function (PERMISSION_DENIED),
  - functions never load chunks: accessing a position in a chunk that is not loaded raises CHUNK_NOT_LOADED,
  - changes SHOULD be made as the owner, so that spawn protection and protection mods apply to them.

#### Functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| TICKS | – | result INTEGER | Game time of the dimension of the trigger block in ticks. | – |
| BEEP | – | – | Plays a short "pling" (the note block bell sound) at the trigger block. | BEEP |
| SOUND | note INTEGER, instrument STRING (default `harp`) | – | Plays a note at the trigger block like a note block: `note` is between 0 and 24, `instrument` is the name of a note block instrument (e.g. `harp`, `bass`, `bell`, `flute`). | SOUND |
| SAY | message STRING, player STRING (default empty) | – | Writes `message` to the chat, prefixed with `[<program>/<PID>]`. If `player` is set, only that player receives the message (INVALID_ARGUMENT if the player is not online), otherwise all players within the radius set by the configuration key `sayRadius` (default 64 blocks) around the trigger block. | SAY (speech output) |
| GET_BLOCK | position POSITION | id STRING, properties HASH STRING, power INTEGER | The block at `position`: its ID (e.g. `minecraft:oak_door`), its block state properties as text (e.g. `facing` → `north`) and the redstone signal strength it receives (0–15). | – |
| GET_INVENTORY | position POSITION | result ARRAY STRUCT ITEM_STACK | The content of the inventory of the block at `position`, one element per slot. Raises INVALID_ARGUMENT if the block has no inventory. | – |
| GET_LEVER | position POSITION | result BOOLEAN | True if the lever at `position` is on. | – |
| SET_LEVER | position POSITION, on BOOLEAN | – | Switches the lever at `position` on or off. | – |
| GET_BUTTON | position POSITION | result BOOLEAN | True if the button at `position` is pressed. | – |
| PRESS_BUTTON | position POSITION | – | Presses the button at `position`, like a player would. | – |
| GET_PRESSURE_PLATE | position POSITION | result INTEGER | The signal strength (0–15) of the pressure plate at `position`. Pressure plates can only be read; they are pressed by entities. | – |
| GET_OPEN | position POSITION | result BOOLEAN | True if the door, trapdoor or fence gate at `position` is open. | – |
| SET_OPEN | position POSITION, open BOOLEAN | – | Opens or closes the door, trapdoor or fence gate at `position`. Iron doors and iron trapdoors, which players can't open by hand, require the permission `allowBlockChanges`. | – |
| MOVE_ITEMS | from POSITION, to POSITION, id STRING, count INTEGER | result INTEGER | Moves up to `count` items with the ID `id` from the inventory at `from` to the inventory at `to`. Returns the number of items that were moved. Restricted. | – |
| DELETE_ITEMS | position POSITION, id STRING, count INTEGER | result INTEGER | Removes up to `count` items with the ID `id` from the inventory at `position`. Returns the number of removed items. Restricted. | – |
| PLACE_BLOCK | position POSITION, id STRING, properties HASH STRING (default empty) | – | Places the block `id` with the given block state properties at `position`, which MUST contain air or a replaceable block such as grass or water. Restricted. | – |
| REMOVE_BLOCK | position POSITION, drop BOOLEAN (default true) | – | Removes the block at `position`. If `drop` is true, the block drops as an item like when it is mined. Restricted. | – |
| SPAWN_ITEM | position POSITION, id STRING, count INTEGER | result STRING | Spawns an item entity with `count` items of `id` at `position` and returns its UUID. Restricted. | – |
| SPAWN_ENTITY | position POSITION, type STRING | result STRING | Spawns an entity of the type `type` (e.g. `minecraft:pig`) at `position` and returns its UUID. Restricted. | – |
| FIND_ENTITIES | position POSITION, radius INTEGER, type STRING (default empty) | result ARRAY STRUCT ENTITY | All entities (including items) within `radius` blocks (at most 64) of `position`, optionally only those of the type `type`. | – |
| TELEPORT | uuid STRING, position POSITION | – | Teleports the entity or item with the UUID `uuid` to `position`. Raises INVALID_ARGUMENT if no such entity is loaded. Restricted. | – |

In the table, POSITION, ITEM_STACK and ENTITY stand for the struct types `c0de/minecraft/POSITION`, `c0de/minecraft/ITEM_STACK` and `c0de/minecraft/ENTITY`.

For the functions that work with a specific kind of block (levers, buttons, pressure plates, doors, trapdoors, fence gates, inventories), a block of the wrong kind at `position` raises INVALID_ARGUMENT.

### Structural statements

Structural statements are conditions and loops. They are only allowed inside code blocks of functions.

#### Minecraft item

The structural base item is used to craft the structural blocks.

##### Crafting

Eight structural base items can be crafted from a green wool block surrounded by eight NOOP blocks.

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
    "B": "minecraft:green_wool"
  },
  "result": {
    "id": "c0de:structural_base",
    "count": 8
  }
}
```

#### Conditional execution

A condition is a structural statement that starts with an IF keyword directly followed by an expression that evaluates to a BOOLEAN. A code block MUST directly follow the IF line. That code block is only executed if the expression evaluates to true.

That code block MAY be followed by a line containing only the ELSE keyword (at the same indentation as the IF keyword) and another code block, which is only executed if the expression evaluated to false.

The last END keyword of a condition MUST be directly followed by an IF block. If the condition has an ELSE part, the END of the first code block stands alone.

```
IF x > 0 ;
BEGIN
    …
END
ELSE
BEGIN
    …
END IF
```

There is no ELSE IF. Conditions are chained by nesting another IF statement in the ELSE code block.

##### Minecraft blocks

The IF block is dark green (color #00AA00) when the IF statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white diamond with a green question mark (?) in it.

The ELSE block is dark green (color #00AA00) when the IF statement it belongs to is incomplete and green (color #55FF55) when it is complete. All six sides show a white diamond with an inverted green question mark (¿) in it.

###### Crafting

An IF block can be crafted shapelessly from a structural base item and wheat seeds. It can also be crafted shapelessly from an ELSE block.

An ELSE block can be crafted shapelessly from a structural base item and wheat. It can also be crafted shapelessly from an IF block.

#### Counting loop

A FOR loop is a multi-line structural statement that starts with the FOR keyword, directly followed by a LABEL with the unique name of the loop variable and its type, which MUST be INTEGER or FLOAT. These are followed by an expression that defines the start value, an expression that defines the end value and, optionally, an expression that defines the increment. If the increment is omitted, it is 1. The increment MUST NOT be 0. A code block MUST directly follow the FOR line. Its END keyword MUST be directly followed by a FOR keyword.

The loop variable is only visible inside the loop and MUST NOT be the target of an ASSIGN statement or a CALL result. The start, end and increment values are evaluated once, before the loop starts. The end value is inclusive: with a positive increment, the loop runs as long as the loop variable is less than or equal to the end value, with a negative increment as long as it is greater than or equal to the end value.

```
FOR i INTEGER 1 ; 10 ; 2 ;
BEGIN
    …
END FOR
```

##### Minecraft block

The FOR block is dark green (color #00AA00) when the FOR statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white circle bordered by a circular arrow around a # sign.

###### Crafting

A FOR block can be crafted shapelessly from a structural base item and a dirt block. It can also be crafted shapelessly from an EACH block.

#### Iterating loop

An EACH loop is a multi-line structural statement that starts with the EACH keyword, directly followed by a LABEL with the unique name of the loop variable, directly followed by a LABEL referring to a previously defined iterable variable or constant, i.e. an ARRAY or a HASH. A code block MUST directly follow the EACH line. Its END keyword MUST be directly followed by an EACH keyword.

For an array, the loop variable has the subtype of the array and receives the elements in the order of their index. For a hash, the loop variable is a STRING and receives the keys of the hash in ascending lexicographic order; the value is accessed with the key as index. The loop iterates over a copy of the iterable, so changing the iterable inside the loop does not affect the iteration. The loop variable MUST NOT be the target of an ASSIGN statement or a CALL result.

##### Minecraft block

The EACH block is dark green (color #00AA00) when the EACH statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white circle bordered by a circular arrow around a stack of cobblestone.

###### Crafting

An EACH block can be crafted shapelessly from a structural base item and a cobblestone block. It can also be crafted shapelessly from a FOR block.

#### Conditional loops

There are two conditional loop statements. The WHILE statement evaluates its expression before entering the loop. If it is already false, the code block of the loop is never entered.

The REPEAT statement evaluates its expression at the end of the loop, so its code block is executed at least once.

A WHILE loop starts with a line containing the WHILE keyword, directly followed by an expression that evaluates to a BOOLEAN. A code block MUST directly follow. Its END keyword is directly followed by a WHILE keyword. The code block is executed as long as the expression evaluates to true.

A REPEAT loop starts with a line containing only the REPEAT keyword. A code block MUST directly follow. Its END keyword is directly followed by the REPEAT keyword and an expression that evaluates to a BOOLEAN. The code block is executed again as long as the expression evaluates to true.

##### Minecraft blocks

The WHILE block is dark green (color #00AA00) when the WHILE statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white circle bordered by two circular arrows around a question mark.

The REPEAT block is dark green (color #00AA00) when the REPEAT statement is incomplete and green (color #55FF55) when it is complete. All six sides show a square bordered by two angled arrows around a question mark.

###### Crafting

A WHILE block can be crafted shapelessly from a structural base item and a diorite block. It can also be crafted shapelessly from a REPEAT block.

A REPEAT block can be crafted shapelessly from a structural base item and a granite block. It can also be crafted shapelessly from a WHILE block.

## Running programs

### Trigger blocks

Processes are started by trigger blocks, which can be placed in any dimension. In the c0de dimension, they can only be placed in the area with negative x or z coordinates (the black area outside of the sheets). There are three kinds of trigger blocks:
  - A **trigger block** starts a process when it receives a redstone pulse.
  - A **timer block** starts a process at a configurable interval.
  - A **debug trigger block** starts a process with a [debug cursor](#movement) when a player attacks it with a debugging tool.

A trigger block can't start a process if
  - it is not configured yet,
  - the program or function it has been configured with has been deleted since,
  - a process it started is still running,
  - no process ID is available for the program, i.e. all y coordinates between 0 and the maximum build height above the PID position of the program are occupied,
  - the input inventory does not contain an item for every parameter of the function that has no default value,
  - the function has more results than parameters and the input inventory does not contain enough empty variable items to make up the difference,
  - there are items in its output inventory, or
  - (timer block only) it receives a redstone signal. A redstone signal pauses a timer block.

Each trigger block has an owner (the player who configured it last, see [Owner and permissions](#owner-and-permissions)), a property that contains the PID while a process is running, two properties that specify the program and the function to run, and a property that contains the exit code and the error of the last process. A timer block also has a property specifying the interval between activations. The user can set these properties in a dialog that opens when the block is right-clicked. The dialog has the following elements:
  - a display of the PID of the currently running process
  - a display of the exit code and, if the last process crashed, its error code, message, context and line
  - a dropdown to select the name of the program to run. It is locked while a process is running and is populated with the names of the programs in the c0de dimension.
  - a dropdown to select the function to run. It is locked while a process is running or if no program has been selected. It is populated with the functions of the selected program. Changing the program empties and repopulates this dropdown.
  - (timer block only) an input field for the interval in ticks
  - a chest-sized input inventory. Only variable and constant items (not to be confused with VARIABLE and CONSTANT blocks) can be inserted here.
  - a chest-sized output inventory
  - a single-slot inventory that accepts a single block item. Placing a block there changes the texture of the trigger block to that of the inserted block, camouflaging it.

### Starting a process

Starting a process places a PID block on the cursor rail next to the PROGRAM statement (x=4, z=0 relative to the sheet). Its y coordinate is determined by searching for the first air block from y=0 up to the maximum build height. This y coordinate is the PID. A cursor is spawned in the line after it (x=4, y=PID, z=1) and initialized with the data it needs from the trigger block: the dimension and coordinates of the trigger block, the name of the program and the name of the function to run.

The chunks of a sheet with a running process MUST stay loaded until the process has ended.

### Protection of running programs

While at least one process of a program is running, its sheet MUST NOT be changed. In particular,
  - no block can be placed on or removed from the sheet (x=0 to x=31, from the floor up to the maximum build height), neither by players nor by any other means such as pistons, explosions, fluids, commands of other mods or block-placing entities,
  - the dialogs of LABEL, LITERAL and OPERATOR blocks on the sheet open read-only,
  - the wrench and the hammer can't be used on the sheet.

The text marker can still be used, as it does not change the program. Players who try to change a protected sheet get a message that names the running processes (program name and PIDs).

### Exchanging data with a process

Data is passed into a process as variable and constant items in the input inventory of the trigger block. Each item is assigned to the parameter of the entry function whose name matches the name property of the item. Its type MUST match the type of the parameter.

When a process ends normally, one initialized variable item per result of the entry function is moved from the stack of the cursor to the output inventory. If the function has fewer results than parameters, empty variable items are added to make up the difference, so the number of items going into and out of a process is the same. All other items of the input inventory are moved to the output inventory.

If there is exactly one block with an inventory adjacent to the trigger block and that inventory has sufficient capacity, all items in the output inventory are transferred to it automatically.

### Ending a process

A process ends when
  - the END of the entry function is reached,
  - a RETURN statement is executed in the entry function,
  - an EXIT statement is executed,
  - the cursor crashes, or
  - the cursor reaches an empty line (which means the entry function was not found; the cursor crashes with UNKNOWN_FUNCTION).

When a process ends, the exit code (0 if no EXIT statement was executed, 15 if the cursor crashed) and, if the cursor crashed, the error are stored in the trigger block. A comparator reading the trigger block outputs a signal strength equal to the exit code of the last process until the next process is started. If the cursor crashed, all items of the input inventory are moved to the output inventory unchanged and no result items are created. The cursor entity and the PID block are removed from the c0de dimension.

### Minecraft blocks

#### Trigger block

A non-camouflaged trigger block looks like a transparent block with copper corners and a golden ball in its center. When camouflaged, it looks like the block it is camouflaged as.

##### Crafting

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "ABA",
    "BCB",
    "ABA"
  ],
  "key": {
    "A": "minecraft:copper_ingot",
    "B": "minecraft:glass",
    "C": "minecraft:gold_ingot"
  },
  "result": {
    "id": "c0de:trigger_block",
    "count": 8
  }
}
```

#### Timer block

A non-camouflaged timer block looks like a transparent block with copper corners and a Minecraft clock in its center. When camouflaged, it looks like the block it is camouflaged as.

##### Crafting

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "ABA",
    "BCB",
    "ABA"
  ],
  "key": {
    "A": "minecraft:copper_ingot",
    "B": "minecraft:glass",
    "C": "minecraft:clock"
  },
  "result": {
    "id": "c0de:timer_block",
    "count": 8
  }
}
```

#### Debug trigger block

A non-camouflaged debug trigger block looks like a transparent block with copper corners and a red ball in its center. When camouflaged, it looks like the block it is camouflaged as.

##### Crafting

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "ABA",
    "BCB",
    "ABA"
  ],
  "key": {
    "A": "minecraft:copper_ingot",
    "B": "minecraft:glass",
    "C": "minecraft:redstone_block"
  },
  "result": {
    "id": "c0de:debug_trigger_block",
    "count": 1
  }
}
```

#### PID block

A PID block is an almost black block (color #222222). Its northern and southern faces show its own y coordinate (the PID) in green (color #55FF55), its eastern and western faces show the dimension and coordinates of the trigger block that started the process. This block can't be crafted and can't be destroyed by any entity.

### Minecraft items

#### Debugging tool

The debugging tool looks like a fly swatter with a wooden handle and a leather square at the top.

It is used to
  - start a process with a debug trigger block (by attacking the block),
  - control a debug cursor and inspect cursors (see [Movement](#movement)),
  - inspect blocks: right-clicking any c0de block with the debugging tool opens a read-only dialog that shows all properties of the block, e.g. the kind, parent, name and index of a LABEL, the type and value of a LITERAL or the PID and trigger coordinates of a PID block.

##### Crafting

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

#### Marker

A text marker is used to mark code. It looks like a yellow feather. Left-clicking a line of code with the marker marks the line, left-clicking it again removes the mark. A marked line gets a yellow overlay.

##### Crafting

A text marker is crafted shapelessly from a feather and yellow dye.

#### Wrench

Every decent tech mod needs a wrench. This is the c0de wrench. It is used to move marked code: the user left-clicks a marked area and then right-clicks a line outside that area. This moves the marked lines in front of (north of) that line.

##### Crafting

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

#### Hammer

Every decent tech mod needs a hammer. This is the c0de hammer. It is used to delete a single line or marked code: the user left-clicks a marked area or a single line, which removes the marked area or the line. The following lines are moved up (north) automatically.

The wrench and the hammer can't be used on a sheet while a process of its program is running (see [Protection of running programs](#protection-of-running-programs)).

##### Crafting

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

#### c0de disk

The c0de disk imports and exports programs as YAML files. It looks like a 3.5" floppy disk, a nod to the Amiga.

  - **Export**: Right-clicking the PROGRAM block of a sheet with the c0de disk writes the program to the file `c0de/programs/<program name>.yaml` in the world folder of the server. If the file already exists, the player is asked to confirm overwriting it. Exporting is also possible while processes of the program are running, as it does not change the sheet.
  - **Import**: Right-clicking the floor at the position of the PROGRAM statement (x=5, z=0) of an empty sheet with the c0de disk opens a dialog that lists the YAML files in the `c0de/programs` folder. Selecting a file places the program on the sheet (see [YAML format](#appendix-yaml-format)), including the properties of all LABEL, LITERAL and OPERATOR blocks. In survival mode, the required blocks are taken from the inventory of the player; if blocks are missing, nothing is placed and the dialog lists the missing blocks. If the file is invalid or a program with the same name already exists, nothing is placed and the dialog shows an error message.

##### Crafting

A c0de disk is crafted shapelessly from a data item, an iron ingot and black dye.

#### Data item

The data item is the base for crafting items that contain data. It looks like a blank punch card: a cream-colored (color #FFFEE9) rectangle with the upper right corner cut off at a 45 degree angle and light gray markings on it (color #CCCCCC).

##### Crafting

Four data items can be crafted shapelessly from a paper, white dye and yellow dye.

#### Type items

A type item looks like the data item it was crafted from, but with a red border.

##### Crafting

Each type item can be crafted shapelessly from a data item, red dye and the item that is used to craft the corresponding type block:
  - BOOLEAN: redstone torch
  - INTEGER: sand
  - FLOAT: red sand
  - CHAR: oak plank
  - STRING: string
  - ARRAY: barrel
  - HASH: chest
  - STRUCT: scaffolding
  - ERROR: campfire

#### Variable item

The variable item looks like the data item it was crafted from, but with a blue (color #5555FF) border. Its front and back show the item of its type.

A variable item has the following properties:
  - name
  - type
  - subtype (for arrays and hashes) or struct name (for structs)
  - value

A variable item is *empty* if it has no name and no value. Right-clicking with a variable item in the main hand opens a dialog in which its name and value can be edited.

##### Crafting

Two variable items can be crafted shapelessly from a data item, a type item and light blue dye.

#### Constant item

The constant item looks like the data item it was crafted from, but with a light purple (color #FF55FF) border. Its front and back show the item of its type.

A constant item has the same properties as a variable item. Right-clicking with a constant item in the main hand opens a dialog in which its name and value can be edited, as long as it has no value yet.

##### Crafting

Two constant items can be crafted shapelessly from a data item, a type item and pink dye.

## The cursor

The cursor is an entity that is created when a process starts. It is placed directly after the PID block it belongs to (x=4, y=PID, z=1 relative to the sheet).

### Movement

There are two kinds of cursors: a regular cursor and a debug cursor. The regular cursor moves automatically, the debug cursor is controlled by a player with a debugging tool.

  - A regular cursor moves to the next line every tick, if it is allowed to.
  - A debug cursor starts in the PAUSED state. Shift-left-clicking it with a debugging tool switches it to the RUNNING state, in which it moves every tick like a regular cursor; shift-left-clicking it again pauses it. Left-clicking a paused debug cursor with a debugging tool moves it by one line.
  - Right-clicking any cursor with a debugging tool opens a dialog that shows its properties and the content of its stack.

When a cursor arrives on a new line, it reads all blocks of the line (and of its continuation lines) and processes them. It is not allowed to move on before the processing is complete.

Usually, the cursor moves to the line directly after the statement it has processed. Some statements make it jump: when the cursor skips a function or a code block, follows a CALL or returns from a function, or when a loop is repeated, it moves directly to the target line in one step.

### Properties

Some standard Minecraft properties of the cursor entity carry relevant data:
  - the PID is its y coordinate
  - the line it is processing is its z coordinate

The cursor has the following specific properties:
  - the dimension and coordinates of the trigger block that started the process
  - the name of the program
  - the name of the entry function
  - the name of the function it is currently in
  - a list of available functions
  - an associative array of available types
  - an associative array of available library constants
  - a stack with 2048 slots, each of which can hold one stack item. A larger stack can be configured in the mod settings. The stack size is the only limit for the depth of nested function calls and recursion.
  - its state: RUNNING, PAUSED (debug cursors only) or CRASHED
  - the last keyword it processed

### The stack

The stack holds the following stack items:
  - **variable items** and **constant items** for every declared variable, constant, parameter, result and loop variable
  - **function items** (call frames) for every function that is currently running. Function items exist only on the stack, they are not Minecraft items. The cursor pushes a function item whenever it enters a function. When it leaves the function, it copies the result values to the variables of the calling function and removes everything from the stack down to and including the function item. A function item holds the name of the function, the line of its FUNCTION statement, the line the cursor returns to, the mapping of the results to the variables of the caller and the error label of the CALL statement.
  - **block markers** for every open code block. A block marker holds the keyword of the statement that opened the code block, the lines of its BEGIN and END statements and, for loops, the loop state (end value, increment, remaining elements).

When the cursor resolves a name, it searches the stack from the top down to the topmost function item, then the global variables and constants that were declared before the FUNCTION statement of the current function, then the library constants and the built-in names. If the name is not found, the cursor raises UNKNOWN_NAME. Pushing an item onto a full stack crashes the cursor with STACK_OVERFLOW.

### Error handling

Errors are either *raised* or make the cursor *crash*.

A **crash** ends the process immediately (see [Ending a process](#ending-a-process)). The cursor state is set to CRASHED. All syntax errors and the errors marked with "crash" in the [error code table](#error-codes) cause a crash.

A **raised** error ends the current function as if a RETURN statement with that error had been executed. The `context` of the error is the name of the current function and its `line` is the z coordinate of the line in which it was raised. The error is passed to the caller:
  - If the CALL statement has an error label, the error is stored in that variable and the caller continues normally.
  - Otherwise, the error is raised again in the caller.
  - If the error is raised in the entry function, the cursor crashes with that error.

A function raises a user-defined error by assigning a code of 1000 or higher to a variable of type ERROR and returning it with RETURN.

### Processing statements

#### When a cursor is created

When a cursor is created, the list of available functions is initialized with the built-in functions, the list of types with the built-in types and the list of constants with the built-in constants (among them the names of all [error codes](#error-codes)). The PROGRAM statement has already been validated by the trigger block, so the cursor starts in the line directly after it, which MUST contain a NOOP statement.

#### When a cursor reaches a new line

The cursor first checks the indentation of the line according to the rules in [Lines, statements and indentation](#lines-statements-and-indentation):
  - Lines beginning with PROGRAM, STRUCT or FUNCTION MUST have their keyword block at x=5. Global VARIABLE and CONSTANT lines (outside of functions) MUST also have their keyword block at x=5.
  - INCLUDE lines MUST have their keyword block at x=6.
  - PARAMETER and RESULT blocks MUST have an x coordinate that is one greater than the x coordinate of the FUNCTION or CALL block they belong to.
  - BEGIN blocks MUST always be at the same x coordinate as the keyword block of the statement that requires them.
  - The keyword blocks of all lines between a BEGIN line and its matching END line MUST have an x coordinate that is one greater than the x coordinate of the BEGIN block, unless they are continuation lines or belong to a nested code block.
  - END blocks MUST always be at the same x coordinate as the matching BEGIN block.

If the cursor encounters a wrong indentation, it crashes with SYNTAX_ERROR_INDENTATION.

The cursor then checks whether the statement is allowed at that position:
  - INCLUDE lines MUST come before any STRUCT, VARIABLE, CONSTANT or FUNCTION line.
  - PARAMETER lines are only allowed directly after a FUNCTION line, a CALL line or another PARAMETER line.
  - RESULT lines are only allowed directly after a FUNCTION line, a CALL line, a PARAMETER line or another RESULT line.
  - An END block MUST NOT appear without a matching BEGIN block before it.
  - A BEGIN block MUST only appear as part of a statement that requires a code block.
  - An ELSE line MUST only appear directly after the standalone END line of the first code block of an IF statement.
  - Lines that start with FUNCTION, STRUCT or INCLUDE MUST NOT be placed inside a code block.
  - Lines that start with CALL, ASSIGN, IF, ELSE, FOR, EACH, WHILE, REPEAT, BREAK, RETURN or EXIT MUST be placed inside the code block of a function.
  - BREAK lines MUST be placed inside a loop of the current function.

If a line violates one of these rules, the cursor crashes with SYNTAX_ERROR_PLACEMENT. If the first block of a line is not a keyword block and the line is neither a continuation line nor a content line of a struct declaration or initializer, the cursor crashes with SYNTAX_ERROR_UNKNOWN_STATEMENT.

When the cursor encounters a BEGIN statement, it scans ahead for the matching END statement, which is the first END block at the same x coordinate. If there is none or the END statement is not followed by the keyword block it requires, the cursor crashes with SYNTAX_ERROR_BEGIN_WITHOUT_END.

#### Skipping NOOP lines

When a cursor reaches a line that starts with a NOOP block, the line is skipped as if it were not there.

#### When a cursor reaches an empty line

The program ends at the first empty line. If the cursor reaches it, the entry function has not been executed, and the cursor crashes with UNKNOWN_FUNCTION.

#### When a cursor reaches an INCLUDE statement

When a cursor reaches an INCLUDE statement, it reads the LABEL next to it and tries to read the elements provided by that library. If the label is missing or not initialized, the cursor crashes with SYNTAX_ERROR_PLACEMENT. If the library is not available, it crashes with UNKNOWN_LIBRARY.

It first reads the list of types provided by the library. The list MAY be empty but MUST be defined. The types are added to the types known by the cursor with names in the format `mod/library/type`. If the cursor can't read that list, it crashes with BROKEN_LIBRARY.

It then reads the list of constants provided by the library. The list MAY be empty but MUST be defined. The constants are added to the constants known by the cursor with names in the format `mod/library/constant`. If the cursor can't read that list, it crashes with BROKEN_LIBRARY.

As a last step, it reads the list of functions provided by the library. The list MAY be empty but MUST be defined. The functions are added to the functions known by the cursor with names in the format `mod/library/function`. If the cursor can't read that list, it crashes with BROKEN_LIBRARY.

#### When a cursor reaches a STRUCT statement

The cursor starts a new type definition in its list of types. The name of the type is read from the LABEL directly following the STRUCT keyword. If the name is not valid or not unique, or any other block follows the label, the cursor crashes with SYNTAX_ERROR_STRUCT.

The cursor then moves to the next line. If that line does not contain a BEGIN block at the x coordinate of the STRUCT keyword (x=5), the cursor crashes with SYNTAX_ERROR_STRUCT.

It then reads the lines following the BEGIN line and adds an element to the type definition for each of them. It expects a LABEL with the name of the element at x=6, directly followed by a type block at x=7. If these requirements are not met or the element name is used twice, the cursor crashes with SYNTAX_ERROR_STRUCT. If the type is ARRAY or HASH, a subtype is expected at x=8. If the type (or subtype) is STRUCT, a LABEL containing the name of an already known struct is expected directly after it. If that label is missing or not initialized, the cursor crashes with SYNTAX_ERROR_STRUCT. If the referenced struct is not known, the cursor crashes with UNKNOWN_STRUCT_ENTITY.

When the cursor reaches the line containing the END keyword, it expects the END block at the same x coordinate as the BEGIN block, directly followed by a STRUCT block. If the line is formatted incorrectly, the cursor crashes with SYNTAX_ERROR_STRUCT. The struct type is now known and the cursor moves on to the next line.

#### When a cursor reaches a VARIABLE statement

When a cursor reaches a line starting with a VARIABLE block, it creates a new variable item. It reads the LABEL directly following the VARIABLE block to set its name property. If there is no LABEL directly following the VARIABLE block or the name is invalid, not unique or not initialized, the cursor crashes with SYNTAX_ERROR_VARIABLE.

The LABEL MUST be directly followed by a type block, which sets the type property of the variable item. If the type is missing, the cursor crashes with SYNTAX_ERROR_VARIABLE.

If the type is ARRAY or HASH, another type block MUST directly follow the first one. It sets the subtype of the array or hash. If the subtype is missing, the cursor crashes with SYNTAX_ERROR_VARIABLE. If the type or subtype is STRUCT, a LABEL with the name of a known struct MUST directly follow; if it is missing, the cursor crashes with SYNTAX_ERROR_VARIABLE, if the struct is unknown, with UNKNOWN_STRUCT_ENTITY.

If the type of the variable is STRING, an INTEGER LITERAL MAY directly follow the type block to limit its length.

If the type of the variable is a simple type or STRING, a LITERAL of that type MAY follow. If it exists, its value initializes the variable. If the literal has a different type, the cursor crashes with SYNTAX_ERROR_TYPE_MISMATCH. If a STRING literal exceeds the length limit, the cursor crashes with STRING_TOO_LONG. Without a literal, the variable is initialized with the default value of its type.

If the type of the variable is ARRAY, HASH or STRUCT, the next line MAY contain a BEGIN block at the x coordinate of the VARIABLE block. In that case, the value of the variable is initialized with the data defined in the lines between the BEGIN and the matching `END VARIABLE` statement, as described in [Initializing complex values](#initializing-complex-values):
  - For an array, every line MAY contain one or more LITERAL blocks, which are read from left to right and appended to the array.
  - For a hash, every line MUST contain exactly one LABEL specifying the key, directly followed by a LITERAL specifying the value. A corresponding element is added to the hash. If the key is used twice, the cursor crashes with SYNTAX_ERROR_VARIABLE.
  - For a struct, every line MUST contain exactly one LABEL with the name of an element of the struct type, directly followed by a LITERAL with its value. If the struct type has no element with that name, the cursor crashes with UNKNOWN_STRUCT_ENTITY.
  - Nested initializers are processed recursively.

If a literal is not initialized or its type does not match the expected type, the cursor crashes with SYNTAX_ERROR_TYPE_MISMATCH. If the content lines do not follow these rules, it crashes with SYNTAX_ERROR_VARIABLE.

If the variable item was created successfully, it is pushed onto the stack. The cursor moves on to the line after the VARIABLE line or, if there was an initializer, after its END line.

#### When a cursor reaches a CONSTANT statement

A CONSTANT statement is processed like a VARIABLE statement, with the following differences:
  - The cursor creates a constant item instead of a variable item.
  - Formatting errors raise SYNTAX_ERROR_CONSTANT instead of SYNTAX_ERROR_VARIABLE.
  - A constant of a simple type or STRING MUST be initialized by a LITERAL. If the literal is missing, the cursor crashes with SYNTAX_ERROR_CONSTANT.
  - A constant of type ARRAY, HASH or STRUCT MUST be initialized by a code block ending with `END CONSTANT`. If the code block is missing, the cursor crashes with SYNTAX_ERROR_CONSTANT.

#### When a cursor reaches a FUNCTION statement

When the cursor reaches a line starting with a FUNCTION block, it reads the function name from the LABEL block directly following it. If the LABEL is missing, not initialized or contains an invalid or non-unique name, the cursor crashes with SYNTAX_ERROR_FUNCTION.

The cursor creates a new entry in its list of functions and initializes it with the name of the function and the line of the FUNCTION statement.

The cursor then reads the following lines until it encounters the BEGIN block and records the parameters (PARAMETER lines) and results (RESULT lines) in that entry. If a parameter or result has no LABEL, the LABEL is not initialized or the name is used twice in this function, the cursor crashes with SYNTAX_ERROR_FUNCTION. The LABEL MUST be followed by a type (and, if required, a subtype and struct name). If the type is missing, the cursor crashes with SYNTAX_ERROR_FUNCTION. If a default value does not match the type of the parameter, the cursor crashes with SYNTAX_ERROR_TYPE_MISMATCH.

When the BEGIN block of the function is reached, the cursor checks whether the function is the entry function. If it is not, the cursor jumps to the line after the matching `END FUNCTION` line. The function is now known and can be called by functions declared after it.

If it is the entry function, the cursor
  1. pushes a function item onto the stack. The function item has no return line: when the function ends, the process ends.
  2. pushes a block marker for the code block of the function.
  3. takes one variable or constant item for every parameter from the input inventory of the trigger block, matched by name, and pushes it onto the stack. Constant items are converted to variable items. If there is no item for a parameter that has no default value, the cursor crashes with FUNCTION_MISSING_INPUT. If an item has a different type than its parameter, the cursor crashes with TYPE_MISMATCH. Parameters with a default value and without a matching item are initialized with their default value.
  4. pushes one variable item, initialized with the default value of its type, for every result. If the function has more results than parameters, one empty variable item per missing item is consumed from the input inventory. If there are not enough empty variable items, the cursor crashes with FUNCTION_MISSING_INPUT.
  5. moves all remaining items of the input inventory to the output inventory.
  6. moves on to the first line of the code block.

If the stack is full when pushing an item, the cursor crashes with STACK_OVERFLOW.

#### When a cursor reaches a BEGIN statement

A BEGIN statement is usually processed as part of the statement that requires it. When the cursor enters a code block, it pushes a block marker onto the stack and moves on to the line directly after the BEGIN line.

#### When a cursor reaches an END statement

When the cursor reaches an END statement, it pops all items from the stack down to and including the block marker of the code block, which removes all variables and constants declared inside the block. What happens next depends on the keyword that follows the END block:
  - **IF (or a standalone END of an IF code block)**: the cursor moves on to the line after the end of the whole IF statement, i.e. it skips the ELSE part if there is one.
  - **FOR, EACH, WHILE, REPEAT**: see the respective loop statement.
  - **FUNCTION**: the function ends, see [Returning from a function](#when-a-cursor-returns-from-a-function).

The END lines of STRUCT, VARIABLE and CONSTANT statements are processed as part of these statements.

#### When a cursor reaches an ASSIGN statement

The cursor reads the LABEL directly following the ASSIGN block. If it is missing or not initialized, the cursor crashes with SYNTAX_ERROR_ASSIGN. It resolves the label to a variable; if the name can't be found, it raises UNKNOWN_NAME. If the label refers to a constant or a loop variable, the cursor crashes with SYNTAX_ERROR_ASSIGN.

The cursor then evaluates the expression following the label (see [Evaluating expressions](#evaluating-expressions)). If the value does not match the type of the target, it raises TYPE_MISMATCH.

If the label has an index, the cursor evaluates the index entries from left to right:
  - An INTEGER index into an array MUST be between 0 and the length of the array. If it equals the length, a new element is appended. Otherwise, the cursor raises INDEX_OUT_OF_BOUNDS.
  - A STRING index into a hash sets or adds the element with that key.
  - An element name of a struct or error MUST exist in the type. Otherwise, the cursor raises UNKNOWN_STRUCT_ENTITY.
  - Strings can't be changed through an index. The cursor raises TYPE_MISMATCH.

If the target is a STRING with a length limit and the new value is longer, the cursor raises STRING_TOO_LONG.

Finally, the cursor stores a copy of the value in the variable item and moves on to the line after the statement.

#### When a cursor reaches a CALL statement

The cursor reads the LABEL directly following the CALL block and looks up the function in its list of functions. If the label is missing or not initialized, the cursor crashes with SYNTAX_ERROR_CALL. If the function is not known (yet), it crashes with UNKNOWN_FUNCTION. If a second LABEL follows, it MUST refer to a variable of type ERROR; otherwise the cursor crashes with SYNTAX_ERROR_CALL.

The cursor then reads the PARAMETER and RESULT continuation lines. If a line refers to a parameter or result that the function does not have, or refers to it twice, the cursor crashes with SYNTAX_ERROR_CALL. If a parameter without a default value is not provided, it crashes with FUNCTION_MISSING_INPUT. If the target of a RESULT line is a constant or a loop variable, it crashes with SYNTAX_ERROR_CALL.

The cursor evaluates the parameter expressions from top to bottom in the scope of the caller. If a value does not match the type of the parameter, it raises TYPE_MISMATCH.

For a function of the program, the cursor then
  1. pushes a function item that holds the return line (the line after the last continuation line of the CALL statement), the mapping of the results to the target variables and the error label,
  2. pushes a variable item for every parameter (with the provided or default value) and for every result (with the default value of its type),
  3. jumps to the BEGIN line of the called function, pushes a block marker for its code block and moves on to the first line of the code block.

[Built-in](#built-in-functions) and library functions are executed directly by the mod. They receive the parameter values and return result values and an error the same way as program functions. SLEEP keeps the cursor on the CALL line for the requested number of ticks before it continues. Afterwards, the cursor continues as described in [Returning from a function](#when-a-cursor-returns-from-a-function).

If the stack is full when pushing an item, the cursor crashes with STACK_OVERFLOW.

#### When a cursor reaches a RETURN statement

If a LABEL follows the RETURN block, the cursor resolves it. It MUST refer to a variable or constant of type ERROR; otherwise the cursor crashes with SYNTAX_ERROR_FUNCTION. Its value is the error returned by the function. Without a label, the function returns an error with code 0. Any other block after the RETURN block makes the cursor crash with SYNTAX_ERROR_FUNCTION.

The cursor then returns from the function.

#### When a cursor returns from a function

A function returns when the cursor reaches its `END FUNCTION` line, executes a RETURN statement or an error is raised in it. The cursor
  1. reads the current values of all result variables of the function,
  2. pops all items from the stack down to and including the function item, which also closes all open code blocks of the function,
  3. if the function is the entry function, ends the process: the result values are written to variable items in the output inventory of the trigger block (see [Exchanging data with a process](#exchanging-data-with-a-process)). If the returned error has a code other than 0, the cursor crashes with that error instead.
  4. otherwise, assigns the result values to the target variables of the RESULT lines of the CALL statement (raising TYPE_MISMATCH if a type does not match) and handles the returned error as described in [Error handling](#error-handling),
  5. moves to the return line.

#### When a cursor reaches an EXIT statement

The cursor evaluates the expression following the EXIT block. If it is missing, the cursor crashes with SYNTAX_ERROR_EXIT. If the value is not an INTEGER between 0 and 15, the cursor crashes with INVALID_EXIT_CODE.

The process then ends with this exit code. The current values of the results of the entry function are written to the output inventory of the trigger block as if the entry function had returned normally.

#### When a cursor reaches a BREAK statement

If an expression follows the BREAK block, the cursor evaluates it. If its value is not a BOOLEAN, the cursor raises TYPE_MISMATCH. If it is false, the cursor moves on to the next line.

Otherwise, the cursor pops all items from the stack down to and including the block marker of the innermost loop, which also closes all code blocks nested in the loop and removes the loop variable. It then moves on to the line after the END line of that loop.

#### When a cursor reaches an IF statement

The cursor evaluates the expression following the IF block. If it is missing, the cursor crashes with SYNTAX_ERROR_CONDITION. If its value is not a BOOLEAN, the cursor raises TYPE_MISMATCH.

The cursor then checks the structure of the condition: the next line MUST be a BEGIN line. Its matching END is either followed by an IF block, or it stands alone and the next line is an ELSE line, followed by a BEGIN line whose matching END is followed by an IF block. Otherwise, the cursor crashes with SYNTAX_ERROR_CONDITION.

  - If the value is true, the cursor enters the first code block.
  - If the value is false and there is an ELSE part, the cursor jumps to the BEGIN line after the ELSE line and enters the second code block.
  - If the value is false and there is no ELSE part, the cursor jumps to the line after the `END IF` line.

#### When a cursor reaches an ELSE statement

An ELSE line is never reached by moving on from the line before it, because the end of the first code block of an IF statement jumps to the end of the whole statement. If the cursor reaches an ELSE line in any other way, it crashes with SYNTAX_ERROR_PLACEMENT.

#### When a cursor reaches a FOR statement

The cursor reads the LABEL directly following the FOR block, which defines the loop variable. If it is missing, not initialized or not unique, the cursor crashes with SYNTAX_ERROR_LOOP. The label MUST be directly followed by a TYPE_INTEGER or TYPE_FLOAT block; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

It then evaluates the start, end and optional increment expressions. If there are fewer than two or more than three expressions, the cursor crashes with SYNTAX_ERROR_LOOP. If a value is not numeric or a FLOAT value is used with an INTEGER loop variable, it raises TYPE_MISMATCH. If the increment is 0, it raises INVALID_LOOP_STEP.

The next line MUST be a BEGIN line whose matching END is followed by a FOR block; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

The cursor pushes a block marker holding the end value and the increment and the loop variable initialized with the start value. It then checks the loop condition (loop variable ≤ end value for a positive increment, loop variable ≥ end value for a negative increment):
  - If it is true, the cursor pushes a block marker for the code block and moves on to the first line of the code block.
  - If it is false, the cursor pops the loop variable and the loop marker and jumps to the line after the `END FOR` line.

When the cursor reaches the `END FOR` line, it closes the code block, adds the increment to the loop variable and checks the loop condition again. If it is true, it jumps back into the code block (to the line after the BEGIN line, pushing a new block marker). If it is false, it pops the loop variable and the loop marker and moves on to the line after the `END FOR` line.

#### When a cursor reaches an EACH statement

The cursor reads the LABEL directly following the EACH block, which defines the loop variable. If it is missing, not initialized or not unique, the cursor crashes with SYNTAX_ERROR_LOOP. It then reads the second LABEL and resolves it. If it is missing, the cursor crashes with SYNTAX_ERROR_LOOP. If the name can't be found, it raises UNKNOWN_NAME. If the value is neither an ARRAY nor a HASH, it raises TYPE_MISMATCH.

The next line MUST be a BEGIN line whose matching END is followed by an EACH block; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

The cursor pushes a block marker holding a copy of the elements of an array or the sorted keys of a hash. If there are no elements, it pops the marker and jumps to the line after the `END EACH` line. Otherwise, it pushes the loop variable initialized with the first element or key, pushes a block marker for the code block and moves on to the first line of the code block.

When the cursor reaches the `END EACH` line, it closes the code block. If there are more elements, it sets the loop variable to the next one and jumps back into the code block. Otherwise, it pops the loop variable and the loop marker and moves on to the line after the `END EACH` line.

#### When a cursor reaches a WHILE statement

The cursor evaluates the expression following the WHILE block. If it is missing, the cursor crashes with SYNTAX_ERROR_LOOP. If the value is not a BOOLEAN, it raises TYPE_MISMATCH.

The next line MUST be a BEGIN line whose matching END is followed by a WHILE block; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

  - If the value is true, the cursor pushes a block marker for the loop and moves on to the first line of the code block.
  - If it is false, the cursor jumps to the line after the `END WHILE` line.

When the cursor reaches the `END WHILE` line, it closes the code block and jumps back to the WHILE line, which is processed again.

#### When a cursor reaches a REPEAT statement

If any block follows the REPEAT block, the cursor crashes with SYNTAX_ERROR_LOOP. The next line MUST be a BEGIN line whose matching END is followed by a REPEAT block and an expression; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

The cursor pushes a block marker for the loop and moves on to the first line of the code block.

When the cursor reaches the `END REPEAT` line, it closes the code block and evaluates the expression following the REPEAT block. If the value is not a BOOLEAN, it raises TYPE_MISMATCH.
  - If the value is true, the cursor pushes a new block marker and jumps back to the first line of the code block.
  - If it is false, the cursor moves on to the line after the `END REPEAT` line.

#### Evaluating expressions

To evaluate an expression, the cursor reads the blocks from left to right, continuing on continuation lines, until it reaches an OPERATOR block with the symbol `;`. If there is no such block, the cursor crashes with SYNTAX_ERROR_EXPRESSION.

  - A LITERAL block provides its value. If it is not initialized, the cursor crashes with SYNTAX_ERROR_EXPRESSION.
  - A LABEL block is resolved to a variable or constant (raising UNKNOWN_NAME if it can't be found), then its index entries are applied from left to right. An array index outside of the array raises INDEX_OUT_OF_BOUNDS, a missing hash key raises UNKNOWN_KEY, an unknown struct element raises UNKNOWN_STRUCT_ENTITY.
  - OPERATOR blocks are processed according to the precedence of their symbols (e.g. with the shunting-yard algorithm). An OPERATOR block without a symbol, unbalanced parentheses, more than 8 levels of parentheses, missing operands and two operands without an operator between them make the cursor crash with SYNTAX_ERROR_EXPRESSION.
  - Operands of the wrong type raise TYPE_MISMATCH, a division or modulus by 0 raises DIVISION_BY_ZERO, an INTEGER result that does not fit into 64 bits raises NUMERIC_OVERFLOW.

### Error codes

| Code | Name | Handling | Description |
|---:|---|---|---|
| 0 | OK | – | No error. |
| 100 | SYNTAX_ERROR_INDENTATION | crash | A line has a wrong indentation. |
| 101 | SYNTAX_ERROR_PLACEMENT | crash | A line is not allowed in this context or code block. |
| 102 | SYNTAX_ERROR_BEGIN_WITHOUT_END | crash | A BEGIN line has no matching END line, or the END line is not followed by the required keyword. |
| 103 | SYNTAX_ERROR_UNKNOWN_STATEMENT | crash | A line starts with a block that is not a keyword. |
| 104 | SYNTAX_ERROR_STRUCT | crash | A part of a struct declaration is wrong. |
| 105 | SYNTAX_ERROR_VARIABLE | crash | A part of a variable declaration is wrong. |
| 106 | SYNTAX_ERROR_CONSTANT | crash | A part of a constant declaration is wrong. |
| 107 | SYNTAX_ERROR_TYPE_MISMATCH | crash | A constant, variable or parameter is initialized with a literal that does not match its type. |
| 108 | SYNTAX_ERROR_FUNCTION | crash | A part of a function declaration or a RETURN statement is wrong. |
| 109 | SYNTAX_ERROR_CALL | crash | A part of a CALL statement is wrong. |
| 110 | SYNTAX_ERROR_ASSIGN | crash | A part of an ASSIGN statement is wrong, or it assigns to a constant or loop variable. |
| 111 | SYNTAX_ERROR_CONDITION | crash | A part of an IF statement is wrong. |
| 112 | SYNTAX_ERROR_LOOP | crash | A part of a FOR, EACH, WHILE or REPEAT statement is wrong. |
| 113 | SYNTAX_ERROR_EXPRESSION | crash | An expression is malformed. |
| 114 | SYNTAX_ERROR_EXIT | crash | An EXIT statement has no expression. |
| 200 | UNKNOWN_LIBRARY | crash | An included library does not exist. |
| 201 | BROKEN_LIBRARY | crash | A library could not be read or does not contain the required elements. |
| 202 | UNKNOWN_FUNCTION | crash | A called function or the entry function is not known (yet). |
| 203 | STACK_OVERFLOW | crash | The stack of the cursor is full and a new item was pushed onto it. |
| 204 | FUNCTION_MISSING_INPUT | crash | A function has been called and not all mandatory parameters have been provided, or the trigger block did not provide enough empty variable items for the results. |
| 205 | INVALID_EXIT_CODE | crash | The value of an EXIT expression is not an INTEGER between 0 and 15. |
| 300 | UNKNOWN_NAME | raise | A label refers to a variable or constant that is not in scope. |
| 301 | UNKNOWN_STRUCT_ENTITY | raise | A declaration references a struct that has not been declared (yet), or an index references an element the struct does not have. Raised when it occurs at run time, crash when it occurs in a declaration. |
| 302 | TYPE_MISMATCH | raise | A value does not have the type required at this place. |
| 303 | DIVISION_BY_ZERO | raise | A division or modulus by 0. |
| 304 | NUMERIC_OVERFLOW | raise | An INTEGER result does not fit into 64 bits. |
| 305 | INDEX_OUT_OF_BOUNDS | raise | An array or string index is outside of the valid range. |
| 306 | UNKNOWN_KEY | raise | A hash does not contain the requested key. |
| 307 | STRING_TOO_LONG | raise | A value exceeds the length limit of a STRING. Crash when it occurs in a declaration. |
| 308 | INVALID_LOOP_STEP | raise | The increment of a FOR loop is 0. |
| 309 | INVALID_ARGUMENT | raise | A built-in or library function received a parameter value outside of its valid range. |
| 310 | CHUNK_NOT_LOADED | raise | A library function accessed a position in a chunk that is not loaded. |
| 311 | PERMISSION_DENIED | raise | A library function is not permitted for the owner of the trigger block. |
| ≥1000 | user-defined | raise | Errors raised by programs. |

## Appendix: YAML format

Programs are exported and imported with the [c0de disk](#c0de-disk) as YAML files. A file has three top-level keys:

| Key | Content |
|---|---|
| `PROGRAM` | The name of the program. |
| `INCLUDE` | An array with the names of all included libraries. MAY be omitted if there are none. |
| `code` | An array of statement objects with all other statements of the program, in their order on the sheet. |

The NOOP line after the PROGRAM statement is not written to the file; it is always placed on import.

### Statement objects

Every statement is an object with the property `keyword`. Statements that have a LABEL directly after their keyword also have a `name` property. Depending on the statement, more properties follow. If a statement contains a code block, the statements of that code block are written as a nested array in the property `code`. BEGIN and END lines are not written; they follow from the nesting.

| keyword | Properties |
|---|---|
| NOOP | `content` (optional): the blocks of a disabled line, written as text |
| STRUCT | `name`, `code`: one ELEMENT object per element |
| VARIABLE, CONSTANT | `name`, `type`, `subtype` (ARRAY and HASH), `struct` (struct name, if the type or subtype is STRUCT), `length` (STRING length limit), `value` (simple types and STRING), `code` (initializer of an ARRAY, HASH or STRUCT) |
| ELEMENT | Element of a struct declaration: `name`, `type`, `subtype`, `struct`. Element of a hash or struct initializer: `name` (key or element name) and `value` or a nested `code` |
| LITERAL | Line of an array initializer: `values` (an array of values) or a nested `code` for a nested initializer |
| FUNCTION | `name`, `parameters`, `results`, `code` |
| PARAMETER | In a FUNCTION: `name`, `type`, `subtype`, `struct`, `value` (default value). In a CALL: `name`, `expression` |
| RESULT | In a FUNCTION: `name`, `type`, `subtype`, `struct`. In a CALL: `name`, `target` |
| CALL | `name` (function name), `error` (error label, optional), `parameters`, `results` |
| RETURN | `name` (error label, optional) |
| ASSIGN | `name` (target, with index), `expression` |
| EXIT | `expression` |
| BREAK | `expression` (optional) |
| IF | `expression`, `code`, `else` (optional, the statements of the ELSE code block) |
| FOR | `name`, `type`, `from`, `to`, `step` (optional), `code` |
| EACH | `name`, `iterable`, `code` |
| WHILE | `expression`, `code` |
| REPEAT | `code`, `expression` |

`parameters` and `results` are arrays of PARAMETER and RESULT objects.

### Values and expressions

Literal values are written as YAML scalars that match their type: `true` or `false` for BOOLEAN, numbers for INTEGER and FLOAT (a FLOAT always has a decimal point, e.g. `1.0`), and strings for CHAR and STRING.

Expressions (`expression`, `from`, `to`, `step`) are written as a single string of tokens separated by spaces, without the final `;`:
  - Operators use their symbols (`+`, `≠`, `⊕`, …) or names (`NOT`, `AND`, `OR`, `XOR`).
  - Literals are written as `TRUE`, `FALSE`, `42`, `3.14`, `'a'` (CHAR) or `"text"` (STRING).
  - Labels are written as their names with index, e.g. `values[i]` or `position.x`.

Labels with an index (`name` of ASSIGN, `target`, `iterable`) use the same notation.

### Import

On import, the c0de disk lays the program out on the sheet: it computes the indentation and inserts the BEGIN and END lines. Expressions that don't fit into a line are continued on continuation lines. If a line can't be laid out within x=5 to x=28, the import fails with an error message. Optional parts that are not stored in the file, such as the function name after `END FUNCTION`, are not placed.

On export, the program MUST be structurally valid (all BEGIN and END blocks match, every line starts with a known keyword). Otherwise, the export fails and the disk names the first invalid line.

### Example

```yaml
PROGRAM: doorbell
INCLUDE:
  - c0de/minecraft
code:
  - keyword: CONSTANT
    name: RINGS
    type: INTEGER
    value: 3
  - keyword: NOOP
  - keyword: FUNCTION
    name: ring
    parameters:
      - keyword: PARAMETER
        name: visitor
        type: STRING
        value: ""
    results:
      - keyword: RESULT
        name: count
        type: INTEGER
    code:
      - keyword: FOR
        name: i
        type: INTEGER
        from: "1"
        to: "RINGS"
        code:
          - keyword: CALL
            name: c0de/minecraft/BEEP
          - keyword: CALL
            name: SLEEP
            parameters:
              - keyword: PARAMETER
                name: ticks
                expression: "10"
          - keyword: ASSIGN
            name: count
            expression: "count + 1"
      - keyword: IF
        expression: "visitor ≠ \"\""
        code:
          - keyword: CALL
            name: c0de/minecraft/SAY
            parameters:
              - keyword: PARAMETER
                name: message
                expression: "visitor ⊕ \" is at the door\""
        else:
          - keyword: CALL
            name: c0de/minecraft/SAY
            parameters:
              - keyword: PARAMETER
                name: message
                expression: "\"Somebody is at the door\""
```

## Appendix: Block reference

| Block | Valid color | Invalid color | Symbol | Recipe |
|---|---|---|---|---|
| NOOP | transparent | – | metal corners, wooden edges | 4 iron ingots + 4 sticks → 64 |
| LABEL | white #FFFFFF | gray #AAAAAA | name | NOOP + paper |
| PROGRAM | dark gray #555555 | black #000000 | book | NOOP + book |
| INCLUDE | dark gray #555555 | black #000000 | map | NOOP + empty map |
| EXIT | dark gray #555555 | black #000000 | iron door | 8 NOOP around iron door → 8 |
| BEGIN / END | yellow #FFFF55 | gold #FFAA00 | trapezoid | 8 NOOP around yellow wool → 8 BEGIN; BEGIN ↔ END |
| BREAK | yellow #FFFF55 | gold #FFAA00 | broken stick | NOOP + stick |
| type base (item) | – | – | question mark | 8 NOOP around red carpet → 8 |
| TYPE_BOOLEAN | red | – | redstone torch | type base + redstone torch |
| TYPE_INTEGER | red | – | yellow sand | type base + sand; TYPE_FLOAT + yellow dye |
| TYPE_FLOAT | red | – | orange sand | type base + red sand; TYPE_INTEGER + orange dye |
| TYPE_CHAR | red | – | oak plank | type base + oak plank |
| TYPE_STRING | red | – | string | type base + string |
| TYPE_ARRAY | red | – | barrel | type base + barrel |
| TYPE_HASH | red | – | chest | type base + chest or trapped chest |
| TYPE_STRUCT | red | – | scaffolding | type base + scaffolding |
| TYPE_ERROR | red | – | campfire | type base + campfire |
| VARIABLE | blue #5555FF | dark blue #0000AA | square | type base + bone block; CONSTANT |
| CONSTANT | blue #5555FF | dark blue #0000AA | diamond | type base + 3 bones; VARIABLE |
| LITERAL | light gray #CCCCCC | gray #AAAAAA | value | 8 NOOP around white wool → 8 |
| OPERATOR (all operators, ( ) and ;) | red #FF5555 | dark red #AA0000 | symbol chosen in a dialog | 8 NOOP around red wool → 8 |
| ASSIGN | aqua #55FFFF | dark aqua #00AAAA | triangle | 8 NOOP around cyan wool → 8 |
| FUNCTION | light gray #AAAAAA | dark gray #555555 | ƒ | NOOP + stone |
| PARAMETER | light purple #FF55FF | dark purple #AA00AA | hopper | 8 NOOP around hopper → 8; RESULT |
| RESULT | light purple #FF55FF | dark purple #AA00AA | upward hopper | PARAMETER |
| RETURN | light purple #FF55FF | dark purple #AA00AA | spruce door | 8 NOOP around spruce door → 8 |
| CALL | light gray #AAAAAA | dark gray #555555 | 📞 | NOOP + smooth stone |
| structural base (item) | – | – | – | 8 NOOP around green wool → 8 |
| IF | green #55FF55 | dark green #00AA00 | ? | structural base + wheat seeds; ELSE |
| ELSE | green #55FF55 | dark green #00AA00 | ¿ | structural base + wheat; IF |
| FOR | green #55FF55 | dark green #00AA00 | circular arrow, # | structural base + dirt; EACH |
| EACH | green #55FF55 | dark green #00AA00 | circular arrow, cobblestone | structural base + cobblestone; FOR |
| WHILE | green #55FF55 | dark green #00AA00 | two circular arrows, ? | structural base + diorite; REPEAT |
| REPEAT | green #55FF55 | dark green #00AA00 | square, angled arrows, ? | structural base + granite; WHILE |
| trigger block | – | – | golden ball | copper, glass, gold ingot → 8 |
| timer block | – | – | clock | copper, glass, clock → 8 |
| debug trigger block | – | – | red ball | copper, glass, redstone block → 1 |
| PID block | #222222 | – | PID | not craftable |
