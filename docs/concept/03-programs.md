# Programs and code blocks

[c0de concept](../concept.md) › Programs and code blocks

## Programs

A program is a list of statements placed on a sheet of greenbar paper. Programs are created by placing c0de blocks on that "paper" between the relative x coordinates 5 and 28. A program always starts at absolute z=0 and relative x=5 with a valid PROGRAM statement, followed by a NOOP statement. Programs always run in ascending z direction (north to south).

A program ends at the first empty line. A process MAY be ended prematurely by an EXIT statement.

A program has the following layout:
  1. a PROGRAM statement at z=0
  2. a NOOP statement at z=1
  3. zero or more INCLUDE statements
  4. any number of STRUCT, global VARIABLE, global CONSTANT and FUNCTION statements and NOOP lines in any order. A name can only be used after it has been defined.

The cursor processes the program from top to bottom until the entry function ends. As a consequence, global variables and constants declared after the entry function are never initialized, and functions declared after the entry function can't be called from it. This is intended: everything a function uses MUST be declared before it.

### Validation while writing

While a player writes a program, the game checks it after every change, without running it: the syntax as described in the [cursor chapter](15-cursor.md) and the names (unknown or duplicate variables, constants, functions, libraries and struct types, changed constants and calls with unknown or missing parameters or results). Every block of a line with a problem shows its invalid color; the reason is shown by the [debugging tool](14-items-and-tools.md#debugging-tool). If a statement is so broken that its following lines can't be assigned to it (e.g. a BEGIN without END), all of them are invalid. Lines after the empty line that ends the program don't belong to it and are invalid as well. Types of expressions and values are only checked when the program runs.

Program names are unique in the c0de dimension. If a second sheet uses a name that is already used, its PROGRAM line is invalid. A sheet is also validated when the chunk of its PROGRAM line is loaded, so programs written with older versions of the mod are registered as soon as a player comes near them; the program that was registered first keeps its name.

Some mistakes are prevented when a block is placed on a sheet, and the player gets the reason:
  - On a sheet, c0de blocks can only be placed on the floor (the code layer) between x=5 and x=28.
  - A NOOP line can't contain other blocks, and a NOOP block can only be placed in an empty line.
  - A FUNCTION line starts at x=5 directly after a NOOP line (FUNCTION can also be placed after END).
  - END can't be placed directly below its BEGIN, because a code block can't be empty.
  - A keyword that starts a line is indented correctly compared with the keyword line directly above it: one block further right below a BEGIN line, one block further left below PARAMETER, RESULT and INCLUDE lines, and at the same x below all other lines. END and ELSE may be further left to close code blocks. Lines below empty lines, comment lines and content lines are not checked, so a program can still be written in any order.
  - PARAMETER and RESULT lines directly follow their FUNCTION or CALL line, indented by one block, or another PARAMETER line; RESULT lines may also follow RESULT lines.

Executable statements (ASSIGN, CALL, IF, FOR, …) are only allowed inside functions. A process always runs one function of the program, the *entry function*, which is selected in the trigger block.

### Program statement

A PROGRAM statement consists of the PROGRAM keyword directly followed by a LABEL containing the program name. The program name MUST be unique within the c0de dimension. The line directly after the PROGRAM statement MUST contain a NOOP statement.

### Include statement

After the NOOP line, one or more INCLUDE statements MAY follow. An INCLUDE statement consists of the INCLUDE keyword directly followed by a LABEL specifying the library to include. A library name starts with the name of the mod that provides it, e.g. `c0de`, followed by a slash and the name of the library, e.g. `c0de/minecraft` or `c0de/net`. INCLUDE statements are always indented by one block relative to the PROGRAM statement (x=6). All INCLUDE statements MUST come before the first STRUCT, VARIABLE, CONSTANT or FUNCTION statement.

### Exit statement

An EXIT statement ends the process immediately, no matter how deeply the cursor is nested in functions and code blocks. It consists of the EXIT keyword followed by an expression that evaluates to an INTEGER between 0 and 15. This value is the exit code of the process and is returned to the trigger block that started the process. EXIT statements are only allowed inside code blocks of functions.

The exit code 15 is also used for processes that crashed. Program authors MAY use `EXIT 15 ;` to signal a fatal run-time error in the same way.

### Minecraft blocks

#### Program block

A PROGRAM block in a valid PROGRAM statement is dark gray (color #555555) with the picture of a Minecraft book on all six sides. If the statement is not valid, it is black (color #000000).

##### Crafting

A PROGRAM block can be crafted shapelessly from a NOOP block and a book.

#### Include block

An INCLUDE block in a valid INCLUDE statement is dark gray (color #555555) with the picture of a map on all six sides. If the statement is not valid, it is black (color #000000).

##### Crafting

An INCLUDE block can be crafted shapelessly from a NOOP block and an empty map.

#### Exit block

An EXIT block in a valid EXIT statement is dark gray (color #555555) with the picture of a Minecraft iron door on all six sides. If the statement is not valid, it is black (color #000000).

##### Crafting

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

## Code blocks and scopes

The language uses lexical scoping. A name can only be referenced after it has been defined and only if it was defined in the same scope or in a parent scope.

The global scope contains NOOP statements, struct types, global constants, global variables and functions. All other scopes are created by code blocks.

A valid code block consists of a BEGIN statement, one or more statements and the matching END statement. Code blocks MUST NOT contain STRUCT or FUNCTION statements. A code block MUST directly follow
  - the header of a FUNCTION statement (after its last PARAMETER or RESULT line),
  - a STRUCT statement,
  - an IF, ELSE, FOR, EACH, WHILE or REPEAT line, or
  - a VARIABLE or CONSTANT line of a complex type, where it contains the initial value (see [Initializing complex values](04-data.md#initializing-complex-values)).

Variables and constants declared inside a code block can only be used inside that block and are forgotten when the block ends. A code block has access to all variables and constants of its parent scopes that have been declared before it.

### Beginning a code block

The BEGIN statement consists of a single BEGIN keyword. It MUST have the same indentation as the line of the statement that requires the code block.

#### Minecraft block

A BEGIN block is gold (color #FFAA00) if there is no matching valid END statement and yellow (color #FFFF55) if there is one. The top side of a BEGIN block shows a trapezoid, whose long side faces in positive z direction. The trapezoid is yellow on a gold block and gold on a yellow block.

##### Crafting

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

##### Placement

A BEGIN block can only be placed as part of a statement that requires a code block (see the list above). It MUST be placed at the same x coordinate as the keyword block of that statement. The lines following the BEGIN line MUST be indented by one block, and the matching END block MUST be placed at the same x coordinate as the BEGIN block.

### Ending a code block

The END statement consists of the END keyword at the same indentation as its matching BEGIN statement. It MUST be directly followed by the keyword block of the statement that required the code block, e.g. `END FUNCTION`, `END WHILE` or `END VARIABLE`. It ends the scope started by the BEGIN statement.

There are two exceptions:
  - The END of the first code block of an IF statement that has an ELSE part stands alone; the following line MUST be the ELSE line. Only the last END of the whole condition is followed by an IF block (see [Conditional execution](07-structural-statements.md#conditional-execution)).
  - The END of a nested initializer stands alone (see [Initializing complex values](04-data.md#initializing-complex-values)).

The END of a REPEAT loop is additionally followed by the loop condition, the END of a FUNCTION MAY additionally be followed by a LABEL repeating the function name.

#### Minecraft block

The END block has the same colors as the BEGIN block, but the long side of its trapezoid faces in negative z direction.

##### Crafting

One END block can be crafted shapelessly from one BEGIN block.

##### Placement

An END block can only be placed at the same x coordinate as the matching BEGIN block. It MUST NOT be placed directly after the BEGIN block, as a code block MUST contain at least one statement (a NOOP statement is sufficient).

### Leaving a loop prematurely

A BREAK statement prematurely ends the innermost loop (FOR, EACH, WHILE or REPEAT) that contains it, including all code blocks nested inside that loop. It consists of the keyword BREAK, which MAY be followed by an expression that evaluates to a BOOLEAN. If that expression evaluates to false, the BREAK statement is ignored. A BREAK statement outside of a loop is invalid.

#### Minecraft block

The BREAK block has the same colors as the BEGIN and END blocks but shows a broken stick in a circle instead of a trapezoid.

##### Crafting

One BREAK block can be crafted shapelessly from a stick and a NOOP block.
