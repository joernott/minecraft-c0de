# c0de

## Abstract

c0de is a simple interpreted programming language. It is implemented as a Minecraft mod. The mod uses its own dimension where you can place blocks to assemble a program. All those blocks can be crafted from vanilla materials. Programs can be run by triggering special blocks placed in other dimensions.

## Introduction

Inspired by the programming language BASIC and the concept of early Lego Mindstorms, where the user could program the "Brick" in an IDE by connecting colorful blocks that looked like Lego bricks, the c0de language is a simple programming language that can be written by placing c0de blocks in a special Minecraft dimension.

It is possible to import and export programn as a textual representation in a yaml file that can be worked on with an IDE, but where is the fun of that.

### Terminology
The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described below.

  - MUST: This word, or the terms "REQUIRED" or "SHALL", mean that the definition is an absolute requirement of the specification.
  - MUST NOT: This phrase, or the phrase "SHALL NOT", mean that the definition is an absolute prohibition of the specification.
  - SHOULD: This word, or the adjective "RECOMMENDED", mean that there may exist valid reasons in particular circumstances to ignore a particular item, but the full implications must be understood and carefully weighed before choosing a different course.
  - SHOULD NOT: This phrase, or the phrase "NOT RECOMMENDED" mean that there may exist valid reasons in particular circumstances when the particular behavior is acceptable or even useful, but the full implications should be understood and the case carefully weighed before implementing any behavior described with this label.
  - MAY: This word, or the adjective "OPTIONAL", mean that an item is truly optional. One vendor may choose to include the item because a particular marketplace requires it or because the vendor feels that it enhances the product while another vendor may omit the same item.  An implementation which does not include a particular option MUST be prepared to interoperate with another implementation which does include the option, though perhaps with reduced functionality.  In the same vein an implementation which does include a particular option MUST be prepared to interoperate with another implementation which does not include the option (except, of course, for the feature the option provides.)

After that nod to the famous terminology definition used in many RFC, we return to our regular program:
  - sheet: A group of chunks in the c0de dimension. A sheet is 2 chunks wide (in x direction) and almost endless (up to max z) in z direction. Sheets always start at z=0.
  - program: A program consists of a set of valid instructions on a sheet. A program MUST start at z=0. It MUST have a valid and unique name and MUST be followed by a single empty line. A program always runs south (from lower to higher z coordinates).
  - process: A process runs a program. It MUST have a unique process id. Processes are created by special blocks placed in other dimensions than the c0de dimension. The block that creates a process MUST NOT start another process while it has a running process.
  - process id (PID): A process ID consists of a program name and the y coordinate on which a cursor runs. Process ids are integer values between -0 and 1023. A PID MUST be unique.
  - cursor: A cursor is a moving block on the x=4 postiton (chunk relative), that marks the line of the program that is currently being processed. See [see "The cursor".](#the-cursor)
  - before: A line with a lower z coordinate in the current program
  - directly before: The line z-1
  - after: A line with a higher z coordinate
  - directly after: The line z+1
  - followed by: Blocks that are in the same line with a higher relative x coordinate
  - directly followed by: Blocks in the same line with the  x+1 coordinate.
  - unique: A name defined by a LABEL statement is unique if it has not been defined in a parent or the current scope
  - indentation: Statements inside a block are on a higher x coordinate than the surrounding BEGIN and END lines.
  - statement: A statement consist of one or more valid lines that are described by the languages rule set.
  - line: A line consists of a KEYWORD and depending on the keyword, optional or required language elements. A valid line complies with the definition of the statement.
  - code block: A code block starts with a BEGIN keyword and ends with an END keyword. Variables and constants can be declared inside a code block. Those can only be used inside that block and are forgotten, once a block ends. A block has access to all variables of it's parent scope(s) that have been declared before it.

## The c0de language

### The c0de dimension
The c0de dimension has the following properties:
  - no mob spawning
  - no sun or daylight cycle
  - no weather
  - a cloudless blue sky
  - a constant ambient background light level of 12
  - a floor at y=-4 made of the following types of unbreakable and uncraftable blocks:
    - white c0de bedrock (texture similar to white concrete with an off white color: #F5F7F3)
    - light green c0de bedrock (texture similar to concrete with a very bright green #D2EBD7)
    - black c0de bedrock (texture similar black concrete, color #000000)
    - cursor c0de bedrock (texture similar to gray concrete with a single rail in its middle, always running in z direction)  
  - a configurable build height between 5 and 2052 (default is 260, meaning the up to y=255), 

#### Terrain generation
The floor of the dimension should reminesce 32 blocks (2 chunks) wide greenbar paper (Tabellierpapier) running in north south direction and starting at x=0, z=0.

Chunks with negative x and z coordinates are rendered with a floor consisting only of black c0de bedrock.

Chunks with positive x and z coordinates form a 2 chunk wide pattern:

![floor_c0de](./images/floor_c0de.svg)

This creates endless sheets of greenbar paper next to each other. These sheets are 2 chunks wide in x direction and endless (up to the maximum z coordinate) in the z direction.

### Doing nothing
The noop (no operation) statement is the ideal of many people and organizations, especially governments. It is fast, efficient and does nothing. A valid statements is a single line containing the NOOP keyword.

#### Minecraft block
The NOOP block is a fully transparent block with a thin metallic corners and wooden borders between those corners. The base recipe for it is:

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
Labels are used for types, variables, constants and functions. A valid name follows the following rules:
  - It consists of one or more of the letters a-z, A-Z and 0-9 and underscore (_).
  - It MUST NOT be a keyword
  - It MUST start with a letter

A LABEL has the following properties:
  - parent: If the kind of the label is FUNCTION, this property MUST be set, defining the parent of its name:
    - for functions defined in the program, this is the program name
    - for builtin functions, this is an empty string
    - for functions coming from an include, it is the name of the mod followed by a slash and the name of the library in the mod.
  - name: Each label MUST have a name. The combination of parent and name MUST be unique.
  - index: The index MAY be used for complex types (String, Array, Hash, Struct) to reference one element of it.

#### Minecraft block
The block is white (color #ffffff) and shows the last word of the name written in black on all sides when it is positioned correctly and contains a valid unique name. If that is not the case, the block color is gray (color #AAAAAA).

A LABEL block MUST directly follow one of the following blocks:
  - a function block
  - a variable block
  - a constant block
  - a type block
  - a call block

A LABEL may be part of an expression.

When placing a label, the game initializes its kind and parent properties based on its position as much as possible. The user can right click a label to open a dialog.
The layout depends on the placement of a block:
- If the label is placed next to a PROGRAM, FUNCTION, VARIABLE, CONSTANT, FOR or EACH block, an input field is shown.
- If it is next to INCLUDE keyword, a dropdown list of available libraries is shown
- If it is next to a CALL keyword, a list of available functions from the core, from libraries and the program is shown
- If it is part of an expression, a dropdown showing variables that are in scope is shown.

##### Crafting
One LABEL block can be crafted shapeless from a noop block and a piece of paper. To assign a name, shift right click with an empty hand. This will open a dialog box where the user can enter a name. The input field in that box shows the current name which can be edited. If the name is valid, the OK button (a green checkmark) can be clicked, if it is invalid, the button is disabled. Pressing escape or clicking outside of the dialog closes it without making changes.

### Programs
A program is a list of statements placed on sheet of greenbar paper. Programs can be created by placing c0de language blocks on that "paper" between relative chunk coordinates x=5 and x=28. Programs always start at absolute z=0, and x=5 (relative to the sheet border) with a valid program statement followed by a NOOP statement. Programs always run in ascending z direction (north to south).

A program ends on an empty line. It MAY be ended prematurely by an EXIT statement. The exit statement consists of the keyword EXIT followed by an expression that evaluates to a value beween 0 and 15. That value is returned to the object that started a process.

A program statement consists of the PROGRAM keyword directly followed by a label. The line after the program statement MUST contain a noop statement.

After the noop statement, one or more include statements MAY be defined. An include statement starts with the keyword INCLUDE directly followed by a LABEL specifying the library to include. A library name starts with the name of the mod it is in, e.g. c0de, followed by a slash and the libraries name, e.g. c0de/minecraft or c0de/net. Library statements are always indented by one block in relation to the PROGRAM statement. 

#### Minecraft blocks
##### Program block
A PROGRAM block in a valid program statement is dark gray (color #555555) with the picture of a Minecraft book on all six sides. If the statement is not valid, it is black (color #000000).

###### Crafting
A PROGRAM block can be crafted shapelessly from a NOOP block and a Book.

##### Exit block
An EXIT block in a valid exit statement is dark gray (color #555555) with the picture of a Minecraft iron door on all six sides. If the statement is not valid, it is black (color #000000).

###### Crafting
Sixteen EXIT blocks can be crafted from an iron door surrounded by eight NOOP blocks.
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
    "id": "c0de:begin_block",
    "count": 8
  }
}
```
##### INCLUDE block
An INCLUDE block in a valid program statement is dark gray (color #555555) with the picture of a paper on all six sides. If the statement is not valid, it is black (color #000000).

###### Crafting
An INCLUDE block can be crafted shapelessly from a NOOP block and a paper.

### Scopes
The language uses lexical scoping. Objects can only be referenced after they have been defined and only if they are in the same or higher scope.

The top scope contains noop statements, struct types, global constants and global variables. It also contains functions. Other scopes are defined by creating a code block.

A valid code block consists of a begin statement, one or more statements and is terminated by an end statement. These code blocks MUST not contain types and functions. A code block MUST be declared directly after a data structure type or a structural statement.

#### Beginning a code block
The begin statement consists of a single BEGIN keyword. It must be on the same indentation level as the keyword that required a code block.

##### Minecraft block
It is gold (color #FFAA00) if there is no matching valid END statement and yellow (color #FFFF55) when there is a matching end block. The top side of a BEGIN block show a trapez (in yellow when the block base color is gold and gold, when the base color is yellow) where the long side faces in positive z direction.

###### Crafting
Eight BEGIN block can be crafted from a yellow wool block surrounded by eight NOOP blocks.
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
One BEGIN block can also be craftes shapelessly from one END block.

###### Placement
The BEGIN block can only be placed as part of a structural statement like a TYPE_STRUCT, FUNCTION, IF, ELSE, WHILE, REPEAT. It MUST be placed at the same x coordinate as that block. Blocks following the BEGIN statement MUST have a x coordinate that is higher that of the BEGIN block. The corresponding END block must be placed on the same x coordinate as the BEGIN block. 

#### Ending a code block
The end statement consists of a END keyword on the same indentation level as its corresponding BEGIN statement. it must be directly followed by the keyword block that required the code block. It ends the scope started by a begin statement.

##### Minecraft block
The END block has the same color as the BEGIN block but with the long side of the trapez facing negative z.

###### Crafting
One END block can be crafted shapelessly from one BEGIN block.

###### Placement
An END block can only be placed on a the same x position as the corrsponding BEGIN block.that is exactly one block left to the block that came before it. It MUST NOT
be placed directly after the BEGIN block.

#### Prematurely leaving a code block
A break statement prematurely ends a code block. It starts with the keyword BREAK and MAY be followed by an expression.If that expression evaluates to false, the break statement will be ignored.

##### Minecraft block
The BREAK block has the same color as the begin and end block but shows a broken stick in a circle instead of a trapez.

###### Crafting
One BREAK block can be crafted shapelessly from a stick and a NOOP block.

### Expressions
An expression is a combination of literals, variables, constants and operators that can be evaluated to return a result.
A primitive expression can be:
  - a literal
  - a variable
  - a constant
A complex espression can be:
  - an unary operator that MUST be followed by an expression
  - a combination that starts with an expression, followed by an operator and another expression. In this case, the expression is read from left to right (in ascending X order) and operations are executed in order of their precedence
  - an opening bracket, an expression and a closing bracket

An expression can span multiple lines. The end of an expression MUST be marked by a semicolon (;).

### Assigning values
An assign statement assigns a value to a variable. The variable MUST be defined before it can be assigned a value.

An assign statement consists of the ASSIGN keyword, directly followed by the variable label, directly followed by an expression.

#### Minecraft block
ASSIGN blocks are aqua (color #55FFFF). If the assigmnent statement is not valid, it is colored dark aqua (color #00AAAA). All six sides show a black equilateral triangle. with one corner points towards positive x.

##### Crafting
Eight ASSIGN block can be crafted from a a cyan wool block surrounded by eight NOOP blocks.
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

### Operators
#### Minecraft block design
Operator blocks in a valid expression are red (color #FF5555) and have a white circle on all sides that contains the text form of it in dark red (color #AA0000). If the expression is incomplete, the blocks are dark red (color #AA0000)

#### Grouping
Parts of an expression can be grouped with parentheses ( and ). These can be nested up to a depth of 8 lebels.

#### Arithmetic operators
The following arithmetic operators exist:
-  Addition: +
- Subtraction: - (can be unary)
- Multiplication: *
- Division: /
- Exponentiation: ^
- Modulus: \

#### Assignment
- See assignments for more details

#### Comparison
- Equal: =
- Not Equal: ≠
- Greater than: >
- Less than: <
- Greater or equal: ≥
- Less or equal: ≤

#### Logical
- The logical operators NOT, AND, OR and XOR. NOT can be unary

#### String
- Concatenate: ⊕

#### Bitwise 

- Bitwise and: &
- Bitwise or: |
- Negate: ¬ is a unary operator.
- Shift left: ←
- Shift right: →

#### Precedence

1. Parentheses
2. Bitwise: ¬ Negation
3. Logical NOT Logical Negation / Inversion, this a unary operator.
4. Arithmetic   ^   Exponentiation (Power of)
5. Arithmetic   - (Unary)   Negation / Unary Minus (e.g., -X)  
6. Arithmetic   *, /    Multiplication and Floating-point Division  
8. Arithmetic   \ Modulo Arithmetic (Remainder of division)  
9. Arithmetic   +, -    Addition and Subtraction  
10. Bitwise: ← and → Shift
11. Text ⊕ String Concatenation  
12. Comparison   =, ≠, <, >, ≤, ≥ All standard comparison operations  
13. Bitwise: &
14. Bitwise: |
15. Logical: AND 
16. Logical: OR

### Data types
A data type is a language element that defines what values a LITERAL, CONSTANT, VARABLE or PARAMETER can have. They are always following the label of a constant, variable, parameter or result. 

#### Minecraft item
A type base is an item, not a block. It looks like a red cube with a black question mark on every side. It can be used to craft the various blocks


##### Crafting
Eight type base block can be crafted from a Minecraft red carpet surrounded by eight NOOP blocks
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
A boolean is declared by the BOOLEAN keyword. It is the equivalent of a java boolean.

###### Minecraft block.
A TYPE_BOOLEAN block is a red block with a white octagon on every side that contains a redstone torch. It can be crafted shapelessly from a type base and a redstone torch.

##### Integer
An integer is declared by the INTEGER keyword. It is the equivalent of a java signed long

###### Minecraft block.
A TYPE_INTEGER block is a red block with a white octagon on every side that contains a heap of yellow sand. The sand looks the same as sugar but in yellow. It can be crafted shapelessly from a type base and a yellow sand block. As an alternative, it can be crafted shapelessly from a Float and yellow dye.

##### Float
A float is declared by the FLOAT keyword. It is the equivalent of a java double.

###### Minecraft block.
A TYPE_FLOAT block is a red block with a white octagon on every side that contains an heap of orange sand. The sand looks the same as sugar but in orange. It can be crafted shapelessly from a type base and an orange sand block. As an alternative, it can be crafted shapelessly from an Integer and orange dye.

##### Character
A character is declared by the CHAR keywort . It can contain a single utf-8 character.

###### Minecraft block.
A TYPE_CHAR block is a red block with a white octagon on every side that contains an oak plank. It can be crafted shapelessly from a type base and an oak plank.

#### Complex data types 

##### String
A string is declared by the STRING keyword. It is the equivalent of a java String. The STRING keyword MAY be followed directly by a literal that limits it's length.

###### Minecraft block.
A TYPE_STRING block is a red block with a white octagon on every side that contains an minecraft string. It can be crafted shapelessly from a type base and a string.

##### Array
An array is declared by the ARRAY keyword. It is the equivalent of a java array with a numerical index. The index always starts with 0 and there is no need to declare the size of an array. An array grows and shrinks automatically.
The ARRAY keyword MUST be directly followed by another type keyword specifying it's contents.  If that TYPE is a struct, that must be followed directly by a LABEL containing the name of a previously declared struct.
Multidimensional arrays can be created by declaring arrays of arrays. There MUST NOT be more than 8 dimensions.

###### Minecraft block.
A TYPE_ARRAY block is a red block with a white octagon on every side that contains an minecraft barrel. It can be crafted shapelessly from a type base and a barrel.

##### Hash
A hash is declared with the HASH keyword. It is the equivalent of an associative array with a STRING index. There is no need to declare the size of a hash. A hash grows and shrinks automatically.
The HASH keyword MUST be directly followed by another type keyword specifying it's contents. If that TYPE is a struct, that must be followed directly by a LABEL containing the name of a previously declared struct.
Multidimensional hashes can be created by declaring hashes of hashes. There MUST NOT be more than 8 dimensions.

###### Minecraft block.
A TYPE_HASH block is a red block with a white octagon on every side that contains an minecraft chest. It can be crafted shapelessly from a type base and a chest. It can alternatively be crafted shapelessly from a type base and a trapped chest.

##### Struct
A struct is declared by the STRUCT keyword. It is a data structure containing one or more elements. There MUST NOT be another keyword after the struct keyword but there must be a code block immediately after the line with the STRUCT keyword. That block may only contain lines consisting  of a LABEL followed by a simple or complex TYPE. If the type for a line is a STRUCT, it must be followed by another LABEL containing the name of a struct that has been declared before it. This is the only way of nesting structures.

###### Minecraft block.
A TYPE_STRUCT block is a red block with a white octagon on every side that contains an scaffolding block. It can be crafted shapelessly from a type base and a scaffolding block.

#### Special data types
Currently, there is only one special data type: ERROR. Every function implicitly returns an error. It is essentially astruct that contains the following fields:
- An INTEGER error.code, which is 0 if there was no error
- A STRING error.message, which is empty if there was no error
- A STRING error.context that always contain the name of the function it was raised in
- An INTEGER error.line specifying the line tht produced the error 

###### Minecraft block.
A TYPE_ERROR block is a red block with a white octagon on every side that contains a burning camp fire. It can be crafted shapelessly from a type base and a camp fire block.

### Variables, constants and literals

#### Variable
A variable definition is a statement that contains a VARAIBLE keyword, directly followed by a valid unique LABEL and a TYPE. If that type is a struct, the type must be followed directly with a LABEL containing the name of a previously declared struct.
A variable MAY be initialized with a value by following the type with a LITERAL if it is a simple type or String. To initialize an Array, Hash or Struct, the line directly after the VARIABLE must start a code block.
For an array, the lines between the BEGIN and END blocks can just contain one or more lines filled with LITERALs, each containing a value of the type specified for the array content.  Nested declarations are allowed up to a depth of 8.
For a hash, each line MUST only contain one LABEL with the name for an element, directly followed by a LITERAL containing a value of the type specified for the hash content.  Nested declarations are allowed up to a depth of 8.
For a struct type, each line MUST only contain one LABEL with one of the names declared in the struct type. The rules for assigning values are the same as described for the variables above. Nested declarations are allowed up to a depth of 8. 

###### Minecraft block.
A VARIABLE block is a blue block (color #5555FF) with a white square on every side when it is a valid variable statement. Otherwise its color is dark blue (color #0000AA).

It can be crafted shapelessly from a type base and a bone block. It can also be crafted shapelessly from a CONSTANT block.

#### Constant
A constant definition is a line that contains a CONSTANT keyword, followed by a valid unique LABEL and a TYPE. Its declaration follws the same rules as for a variable but it MUST be initialized with a value. A constant MUST NOT directly follow an ASSIGN keyword, its content is immutable after declaration.

###### Minecraft block.
A VARIABLE block is a blue block (color #5555FF) with a white raute on every side when it is a valid variable statement. Otherwise its color is dark blue (color #0000AA).

It can be crafted shapelessly from a type base and three bones. It can also be crafted shapelessly from a VARIABLE block.

#### Literal
A literal is similar to a constant without a name. It can only have a simple type and it can only be used in an expression or statement. A literal consists of the LITERAL keyword, directly followed by a value and TYPE.

###### Minecraft block.
A LITERAL block is a light gray block (color #CCCCCC) when it has a value that matches its type. Otherwise its color is gray (color #AAAAAA). It's value is written on all six sides.

Eight literal blocks can be crafted from a white wool surrounded by eight NOOP blocks
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
    "id": "c0de:literal",
    "count": 8
  }
}
```
### Functions

#### Declaring functions
A function is a multiline statement. It is a structural element that MUST habe an empty line above it. It starts with a line that contains the FUNCTION keyword, directly followed by a valid unique LABEL.

The following lines zero or more parameters MAY be defined. Each parameter starts with the keyword PARAMETER, indented by one block from the FUNCTION keyword. The PARAMETER keyword must be followed by a valid LABEL and TYPE. If this is a simple type, it MAY be initilized with a default value by following the TYPE with a LITERAL.
A parameters value MUST be set by the calling entity, if it does not have a default value. Function parameters are always passed by value. They copy the value they were called with and behave like variables declared in the scope of the function.

After the parameters, zero or more result lines MAY be defined. Each result starts with the keyword RESULT, indented by one block from the FUNCTION keyword. It MUST be followed directly by a LABEL and TYPE. A result behaves like variables declared in the scope of the function. It will be returned to the calling entity, containing the last value that has been assigned to it. 

After that there must be a valid code block containing the statements that make up the function. The END keyword of that block MUST be followed directly by a FUNCTION keyword. It MAY be followed by a LABEL repeating the function name. The code inside a function code block can contain nested code blocks but neither types nor other functions.

The code block MAY contain return statements. A return statement immediately breaks all code blocks nested in the function code block and ends the function immediately. A return statement MUST start with the RETURN keyword. It may have a LABEL next to it that refers to an error variable or constant that has been defined before. 

#### Calling functions
A call statement is used to call functions. It starts with a line that contains the CALL keyword, directly followed by the LABEL containing the name of the function to be called. It MAY have a second LABEL that specifies a variable of the ERROR type which will receive the error object from the called function.
A called function must have been declared before the call statement. It may also be the name of the function, the statement is in, enabling recursion.

If the called function has mandatory parameters the lines directly after the CALL statement MUST provide them. Each of the optional parameters MAY be provided.  Those lines are indented. Parameter lines start with the PARAMETER keyword, followed by a LABEL of a variable or constant that has been declared or a LITERAL, then followed by its TYPE.

If the called function has results, the lines after the last parameter line or call line, if no parameters are provided, MAY declare them. Those lines are indented. Result lines start with the RESULT keyword, followed by a LABEL of a variable that will receive the result data.

#### Minecraft blocks

##### Function block
The FUNCTION block is a structural block. It is dark gray (color #555555) when the function is invalid and light gray (color #aaaaaa) when there is a LABEL block with a a valid unique name directly following it. All sides show a white circle with the letter ƒ (symbol for function) in it. It can only be placed if there is a NOOP staement in the line before it (z-1) and it can only be placed on the first valid block in a chunk (x=3). block.

###### Crafting
A FUNCTION block can be crafted shapeless from a NOOP block and a stone 

##### Parameter block
- The PARAMETER block is a dependent block of the FUNCTION block. As a consequence, this block can only be placed directly after a FUNCTION block or after another PARAMETER block. It looks like a hopper. It's color is a dark purple (color #AA00AA) when the parameter line is incomplete and light purple (color #FF55FF) when it is complete. The parameter must be indented by one block from its FUNCTION keyword.

###### Crafting
Sixteen PARAMETER blocks can be crafted from a hopper surrounded by eight NOOP blocks
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
    "id": "c0de:parameter",
    "count": 8
  }
}
```
One PARAMETER block can also be crafted shapelessly from one RESULT block.

##### Result block
- The RESULT block is a dependent block of the FUNCTION block. As a consequence, this block can only be placed directly after a FUNCTION, PARAMETER block or after another RESULT block. It looks like a hopper pointing upwards. It's color is a dark purple (color #AA00AA) when the result line is incomplete and light purple (color #FF55FF) when it is complete. The result must be indented by one block from its FUNCTION keyword.

###### Crafting
One RESULT block can also be crafted shapelessly from one PARAMETER block.

##### Return block
- The RETURN block is a a dark purple (color #AA00AA) when the result line is incomplete and light purple (color #FF55FF) when it is complete. All six sides show a spruce door.

###### Crafting
Eight RETURN blocks can be crafted from a spruce door surrounded by eight NOOP blocks
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
    "id": "c0de:parameter",
    "count": 8
  }
}
```
##### Call block
The CALL block is a structural block. It is dark gray (color #555555) when the function is invalid and light gray (color #aaaaaa) when it forms a valid call statement. All sides show a white circle with the symbol 📞 (phone) in it. 

###### Crafting
A CALL block can be crafted shapeless from a NOOP block and a smooth stone block. 

### Structural statements
Structural statements are conditions and loops.

##### Minecraft item
The structural base item caan be used to craft the various structural blocks.
###### Crafting
Eight structural base blocks can be crafted from a green wool surrounded by eight NOOP blocks
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
A condition is a structural statement that starts with an IF keyword directly followed by an expression that can be evaluated into a boolean value. It MUST have a code block directly following the IF statement. That block will be only executed if the expression evaluated to true.

That code block MAY be followed by the ELSE keyword and another code block, that will only be executed if the expression in the IF statement evaluated to false.

The last END keyword of a condition MUST be directly followed by an IF keyword.

##### Minecraft blocks
The IF block is a a dark green (color #00AA00) when the conditional statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white raute with a green questionmark (?) in it.

The ELSE block is a a dark green (color #00AA00) when the conditional statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white raute with an inverted green questionmark (¿) in it.

###### Crafting
An IF block can be crafted shapelessly from a structural item block and a seed. It can also be crafted shapelessly from an ELSE block.

An ELSE block can be crafted shapelessly from a structural base item and a wheat item. It can also be crafted shapelessly from an IF block.

#### Counting loop
A FOR loop is a multiline structural statement that starts with the FOR keyword, directly followed by a LABEL and TYPE. It MUST then followed by an expression that defines the start value, an expression that defines the end value, and optionally an expression that defines the increment. If that expression is omitted, its value is assumed to be 1. Directly after the FOR statement, a code block MUST follow. The END line always MUST HAVE a FOR keyword following the END keyword.

##### Minecraft block
The FOR block is a a dark green (color #00AA00) when the for statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white circle bordered by a circular arrow around a # sign in it.

###### Crafting
A FOR block can be crafted shapelessly from a structural base item and a dirt block. It can also be crafted shapelessly from an EACH block.

#### Iterating loop
A EACH loop is a multiline structural statement that starts with the EACH keyword, directly followed by the LABEL of a previously defined iterable variable. That can be an array or hash. Directly after the EACH statement, a code block MUST follow. The END line always MUST HAVE a EACH keyword following the END keyword.

##### Minecraft block
The EACH block is a a dark green (color #00AA00) when the for statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white circle bordered by a circular arrow around a stack of cobblestone in it.

###### Crafting
A EACH block can be crafted shapelessly from a structural base item and a cobblestone block. It can also be crafted shapelessly from a FOR block.

#### Conditional loops
There are two conditional loop statements. The while statement evaluates its epression before entering the loop. If it is already false, the loop code block will never be entered.

The repeat statement evaluates its expression at the end of the loop, thus entering the loop code block at least once.

A while loop starts with a line containing the WHILE keyword, directly followed by an expression. Directly after, a code block MUST follow. The END keyword of that loop is always followed by a WHILE keyword.

A repeat loop starts with a line containing only the REPEAT keyword. Directly after that line, a block  MUST follow. The END keyword of that block is followed by the REPEAT keyword and an expression.

##### Minecraft block
The WHILE block is a a dark green (color #00AA00) when the for statement is incomplete and green (color #55FF55) when it is complete. All six sides show a white circle bordered by two circular arrows forming a circle around a questionmark sign in it.

The REPEAT block is a a dark green (color #00AA00) when the for statement is incomplete and green (color #55FF55) when it is complete. All six sides show a square  containing two angled arrows bordering it and containing a questionmark sign in it.

###### Crafting
A WHILE block can be crafted shapelessly from a structural base item and a diorite block. It can also be crafted shapelessly from an REPEAT block.

A REPEAT block can be crafted shapelessly from a structural base item and a granite block. It can also be crafted shapelessly from an WHILE block.

## Running programs

### Trigger blocks
There are blocks, that can be placed in all dimensions, that can start a process that executes a program. A trigger block needs a redstone pulse to start a process and a timer block starts a process at defined intervals. A debug block is a special variant of a trigger block, that only starts if a player attacks it with a debugger tool.

A block can't start a process if
- it is not configured yet
- a program or function it has been configured with have since been deleted
- a process is already running for it
- no process id is available for it. The number of Process IDs is limited by the height of the c0de dimension. PID blocks can be placed between y=0 and max build height. 
- inputs for required parameters are not in its input inventory
- there are no consumable empty variable items in its input inventory and the number of results defined by a function is larger than the number of parameters of that function.
- There are items in the output inventory
- A timer can't start a process if it receives redstone signal.

Each of those block has a property contains the numerical process id (PID) when a process is running. It also has two properties that specify the program and function to run. A timer block also has a property specifying the time interval between activations. Those can be set by the user. Right clicking the block opens a dialog for it. The dialog has the following elements:
  - It displays the process ID of a currently running process
  - A dropdown to select the name of the program to run. This is locked, while a process is running. It is populated by the names of the programs defined in the c0de dimension.
  - A dropdown to choose the name of the function in that program to run. This is locked, while a process is running. The second dropdown is also locked if no program has been chosen. It is populated by the functions in the selected program. Changing the program will empty its current content and repopulate the dropdown.
  - One chest sized inventory for inputs. Only variable and constant items (not to be confused with variable and constant blocks) can be inserted here. 
  - One chest sized inventory for outputs
  - One one block inventory that accepts a single block item. Placing a block in there changes the blocks texture to that of the block, thus camouflaging it.

Starting a process will place a PID block on the cursor block line next to the program statement (x=4, z=0 relative to the sheet). Its y position will be determined by looking for the next free air block starting from y=0 up to the maximum build height. The y coordinate equals the process ID. A cursor will be spawned  in the line after it and initialized with the properties it needs from the trigger block (dimension and coordinates, the name of the program and the name of the function it should run).

When a process ends, initialized variable items will be moved from the cursors stack to the output inventory for each result defined by the function that was run. If the function has less results than parameters, a number of empty variable items will be created that make up the difference. If there is only one block with an inventory adjacent to the trigger block, and the inventory has a sufficient capacity, all items in the output inventory will be transferred to it automatically.

Ending a process will remove the related cursor entity and PID block from the c0de dimension.

#### Minecraft blocks

##### Trigger block
A non camouflaged trigger block looks like a transparent block with copper corners and a golden ball in its center. When camouflaged, it looks lite the block it is camouflaged as.

###### Crafting
A trigger block is crafted with the following recipe:
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

##### Timer block
A non camouflaged timer block looks like a transparent block with copper corners and a minecraft clock in its center. When camouflaged, it looks lite the block it is camouflaged as.

###### Crafting
A timer block is crafted with the following recipe:
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

##### Debug trigger block
A non camouflaged debug trigger block looks like a transparent block with copper corners and a red ball in its center. When camouflaged, it looks lite the block it is camouflaged as.

###### Crafting
A debug trigger block is crafted with the following recipe:
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
    "C": "minecraft:redstrone_block"
  },
  "result": {
    "id": "c0de:debug_trigger_block",
    "count": 1
  }
}
```

##### PID block
A PID block is an almost black block (color #222222) that shows its own y coordinate (the PID) value in green (color #55FF55) on its northern and southern faces and the dimension and coordinates of the block that started the process on the eastern and western faces. This block can't be crafted. It also can't be destroyed by any entity.

#### Minecraft items

##### Debugging tool
The debugging tool looks like a fly swatter with a wooden handle and a leather square at the top.

###### Crafting
a debug tool is crafted from a red wool, a stick and a piece of leather:
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

##### Marker
A text marker can be used to mark code. it looks like a yellow feather. By left clicking on a line of code, the line is marked, left clicking it again removes the mark. A marked line gets a yellow overlay.

###### Crafting
A text marker is crafted shapelessly with a feather and yellow dye.


##### Wrench
Every decent tech mod needs a wrench. This is the c0de wrench. It can be used to move marked code. A user can left click a marked area and then right click a line outside that area. That will move the selected code above that line.

###### Crafting
A wrench is crafted from red wool and 3 iron ingots:
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
    "id": "c0de:debug_tool",
    "count": 1
  }
}
```

##### Hammer
Every decent tech mod needs a hammer. This is the c0de hammer. It can be used to delete a single line or marked code. A user can left click a marked area or a single line.That will remove the slected area or line. Lines after will be moved up automatically.

###### Crafting
A hammer is crafted from red wool and 4 iron ingots:
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
    "id": "c0de:debug_tool",
    "count": 1
  }
}
```

##### Data item
The data item is the base for crafting items that contain data. It looks like a blank punch card , a bright yellow (color #FFFEE9) rectangle with the upper right corner cut in a 45 degree angle that has light gray markers on it (color #CCCCCC) 

###### Crafting
4 data items can be crafted shapelessly from a paper, a white and a yellow dye.

##### Type items
The Type items look like the data item it was crafted from but with a red border 

###### Crafting
Each type item can be crafted shapelessly from a data item, a red dye and the item used to craft a data block:
- Boolean: redstone torch
- Integer: yellow sand
- Float: orange sand
- Character: oak plank
- String: string
- Array: barrel
- Hash: chest
- Struct: scaffolding
- ERROR: camp fire

##### Variable item
The variable item looks like the data item it was crafted from but with a blue (color #5555FF) border. It depicts the item of its type on the front and back side.
A variable item has the following properties:
- name
- type
- subtype
- value

###### Crafting
2 variable items can be crafted shapelessly from a data item, a type item and light blue dye.

##### Constant item
The constant item looks like the data item it was crafted from but with a purple (color #FF55FF) border. It depicts the item of its type on the front and back side.
A variable item has the following properties:
- name
- type
- subtype
- value

###### Crafting
2 constant items can be crafted shapelessly from a data item, a type item and pink dye.

##### Literal item
The literal item looks like the data item it was crafted from but with a light gray (color (color #CCCCCC) border. It depicts the item of its type on the front and back side.

A literal item has the following properties:
- type
- value

###### Crafting
2 literal items can be crafted shapelessly from a data item, a type item and light gray dye.

##### Function item
The function item looks like the data item it was crafted from but with a dark gray (color (color #555555) border. It has the letter ƒ on the front and back side.

A function item has the following properties:
- name
- line
- list of result names

###### Crafting
2 literal items can be crafted shapelessly from a data item, a type item and light gray dye.


## The cursor
The cursor is an entity that is created, when a process is started. It is placed directly after the PID block it belongs to (x=4,y=PID,z=1 relative to the sheet).
There are two cursors, a regular and a debug cursor. The regular cursor will move automatically, the debug cursor is controlled by a user, using the debug item. The regular cursor will move every tick if it is allowed to. The debug cursor will move every tick, if it was shift left clicked by a user with a debugging tool. It will move one line only, if it was left clicked by the user with a debugging tool. Right clicking a cursor with a debugging tool will open a dialog that shows its content.

When a cursor arrives on a new line, it will process the line in a separate thread. It is not allowed to move further, while that thread exists but it will not block Minecraft from ticking.

### Properties
Some of the standard Minecraft properties can be used to infer some relevant data:
- the PID can be dreived from a cursors y coordinate
- the line it is processing by its z coordinate

The cursor has the following cursor specific properties:
- The dimension and coordinates of the block that started the process
- The name of the program
- The name of the function it should run
- The name of the function it is currently in
- An list of available functions
- An associative array of available types
- An associative list of available constants
- A stack that has 2048 slots, each can contain one stack item. A larger stack can be configured in the mods settings.
- Its state, this is either RUNNING, CRASHED, or PAUSED if the cursor is a debug cursor
- the last keyword it processed

### Processing statements

#### When a cursor is created
When a cursor is created, the parameters that were provided by the trigger are moved from the input inventory of the trigger block to the stack of the cursor. The list of available functions is initialized with the functions built in to code. The list of types is initialized with the builtin types.

#### When a cursor reaches a new line
The cursor checks the indentation of that line:
- Lines beginning with PROGRAM, INCLUDE, STRUCT, FUNCTION MUST have their keyword block at x=5. Global variables and constants (declared outside a function) also MUST have their keyword block at x=5
- PARAMETER and RESULT blocks must have a x coordinate that is one greater than the x coordinate of the FUNCTION or CALL block demanding them
- BEGIN blocks must always be on the same x coordinate as the keyword that required them
- All blocks after that BEGIN block must have a x coordinate that is one greater than the x coordinate of the BEGIN block, except for its corresponding END block
- END blocks must always be on the same indentation as the corresponding BEGIN block
If the cursor encounters a wrong indentation, it crashes with the error code SYNTAX_ERROR_INDENTATION.

The cursor checks whether the statement is valid at that position:
- An INCLUDE line must always come before any STRUCT, VARIABLE, CONSTANT or FUNCTION is read by the cursor
- PARAMETER blocks are only allowed in a line after a FUNCTION or another PARAMETER line
- RESULT blocks are only allowed  a line after a FUNCTION, PARAMETER or another RESULT line
- An END block MUST NOT be placed without a corresponding BEGIN block above it
- A BEGIN block MUST only be placed as part of a structural statement or a struct, complex variable or constant initialization
- Lines that start with FUNCTION or STRUCT blocks may never be placed inside a code block
- Lines that start with CALL, ASSIGN, IF, ELSE, FOR, EACH, WHILE or REPEAT MUST be placed inside a code block
If a line violates one of the rules, the cursor crashes with the error code SYNTAX_ERROR_PLACEMENT.

If the cursor encounters a BEGIN statement it scans ahead to find a matching END statement, at the same indentation with. If that fails or the END statement has the wrong keyword block following it, the cursor crashes with the error code SYNTAX_ERROR_BEGIN_WITHOUT_END.

#### Skipping over NOOP blocks
When a cursor reaches a line that starts with a NOOP block, the line is skipped as if it were not there.

#### When a cursor reaches an INCLUDE statement
When a cursor reaches an INCLUDE statement, the LABEL next to it is read and the cursor tries to get a list of elements specified by that mod/library.

It first reads the array of types provided by the library. The list MAY be empty but MUST be defined. The types therein are added to the array of types known by the cursor with their name in the format mod/library/type. If the cursor can't read that list, it crashes with the error code BROKEN_LIBRARY.

It then reads the array of constants provided by the library. The list MAY be empty but MUST be defined. The constants therein are added to the array of types known by the cursor with their name in the format mod/library/constant. If the cursor can't read that list, it crashes with the error code BROKEN_LIBRARY.

As a last step, the cursor reads the list of functions provided by the library. The list MAY be empty but MUST be defined. The functions therein are added to the array of functions known by the cursor with their name in the format mod/library/function. If the cursor can't read that list, it crashes with the error code BROKEN_LIBRARY.

#### When a cursor reaches a STRUCT statement
The cursor starts forming a type definition in its type array. The name of the type is read from the LABEL directly following the STRUCT keyword. If the name is not valid or the STRUCT keyword is not at x=5, the cursor crashes with the error code SYNTAX_ERROR_STRUCT. 

The cursor then moves to the next line. If that line does not contain a BEGIN block at the same x coordinate of the STRUCT keyword, the cursor crashes with the error code SYNTAX_ERROR_STRUCT.

It then proceeds reading the lines following the BEGIN line to add elements to its type definition based on the line content. It expects a LABEL specifying the properties name at x=7, directly followed by a type keyword at x=8 specifying the properties type. If these requirements are not met, the cursor crashes with the error code SYNTAX_ERROR_STRUCT. If the type keyword is STRUCT, a LABEL containing the name of an already known STRUCT is expected at x=9. If that label is missing or uninitialized, the cursor crashes with the error code SYNTAX_ERROR_STRUCT. If the struct referenced in the label is not known, the cursor crashes with the error code UNKNOWN_STRUCT_ENTITY.

When the cursor reaches a line containing the END keyword, it expects the END keyword at the same x coordinate as the BEGIN block. It must be followed diretly by a STRUCT block. If the line is formatted incorrectly, the cursor crashes with the error code SYNTAX_ERROR_STRUCT.

#### When a cursor reaches a VARIABLE statement
When a cursor reaches a line starting with a VARIABLE block, it creates a new variable item. It reads the LABEL directly following the VARIABLE keyword to set its name property. If there is not a LABEL directly following the VARIABLE block or the name in the variable is invalid or not initialized, the cursor crashes with the error code SYNTAX_ERROR_VARIABLE.

The LABEL MUST be followed directly by a TYPE block. That is used to set the type property of the variable item. If the type is missing, the cursor crashes with the error code SYNTAX_ERROR_VARIABLE.

If the type is ARRAY or HASH, there MUST be another type directly following the first TYPE block. That is used to specify the subtype for the array or hash content. If the type is missing, the cursor crashes with the error code SYNTAX_ERROR_VARIABLE.

If the type of the variable is a simple type or String, a LITERAL block MAY directly follow the TYPE block. If it exists, the content of the LITERAL is used to initialize the value of the variable. If the literal has a different type than what the TYPE indicated, the  cursor crashes with the error code SYNTAX_ERROR_TYPE_MISMATCH.

If the type of the variable is a complex type other than STRING, the next line MAY contain a BEGIN keyword. If that is the case, the variables value is initialized with the data defined in the lines between the BEGIN and corresponding END statement:
- For an Array, every line MAY contain one or more LITERAL blocks, those are read from left to right and added to the arrays content. If a literal is uninitialized or has a type that does not match the arrays subtype, the cursor crashes with the error code SYNTAX_ERROR_TYPE_MISMATCH.
- For a hash, every line MUST only contain a LABEL specifying the key, directly followed by a LITERAL specifying a value. A corresponding entry is added to the Hash. If a literal is uninitialized or has a type that does not match the hashes subtype, the cursor crashes with the error code SYNTAX_ERROR_TYPE_MISMATCH.

If a variable item was created successfully, it is placed into the first free slot on the cursors stack. If the stack is full, the  cursor crashes with the error code STACK_OVERFLOW.

#### When a cursor reaches a CONSTANT statement
When a cursor reaches a line starting with a CONSTANT block, it creates a new constant item. It reads the LABEL directly following the CONSTANT keyword to set its name property. If there is not a LABEL directly following the CONSTANT block or the name in the constant is invalid or not initialized, the cursor crashes with the error code SYNTAX_ERROR_CONSTANT.

The LABEL MUST be followed directly by a TYPE block. That is used to set the type property of the constant item. If the type is missing, the cursor crashes with the error code SYNTAX_ERROR_CONSTANT.

If the type is ARRAY or HASH, there MUST be another type directly following the first TYPE block. That is used to specify the subtype for the array or hash content. If the type is missing, the cursor crashes with the error code SYNTAX_ERROR_CONSTANT.

If the type of the constant is a simple type or String, a LITERAL block MUST directly follow the TYPE block. The content of the LITERAL is used to initialize the value of the constant. If the literal does not exist or has a different type than what the TYPE indicated, the  cursor crashes with the error code SYNTAX_ERROR_TYPE_MISMATCH.

If the type of the constant is a complex type other than STRING, the next line MUST contain a BEGIN keyword. If the code block is missing,  the cursor crashes with the error code SYNTAX_ERROR_CONSTANT. The constant value is initialized with the data defined in the lines between the BEGIN and corresponding END statement:
- For an Array, every line MAY contain one or more LITERAL blocks, those are read from left to right and added to the arrays content. If a literal is uninitialized or has a type that does not match the arrays subtype, the cursor crashes with the error code SYNTAX_ERROR_TYPE_MISMATCH.
- For a hash, every line MUST only contain a LABEL specifying the key, directly followed by a LITERAL specifying a value. A corresponding entry is added to the Hash. If a literal is uninitialized or has a type that does not match the hashes subtype, the cursor crashes with the error code SYNTAX_ERROR_TYPE_MISMATCH.

If a constant item was created successfully, it is placed into the first free slot on the cursors stack. If the stack is full, the  cursor crashes with the error code STACK_OVERFLOW.

#### When a cursor reaches a FUNCTION statement
When the cursor reaches a line starting with the FUNCTION statement, it reads the function name from the LABEL block directly following it. If the LABEL is missing, uninitialized or contains an invalid name, the  cursor crashes with the error code SYNTAX_ERROR_FUNCTION.

The cursor creates a new entry in the list of functions, initializing it with the name of the function and line it is found in.

The cursor then reads the next lines until it encounters the BEGIN keyword. The parameters defined by PARAMETER keywords and results specified by RESULT keywords are documented in that entry, too. If a parameter or result has no LABEL, the LABEL is not initialized or has no value, the cursor crashes with the error code SYNTAX_ERROR_FUNCTION. The LABEL MUST be followed by a TYPE. If that keyword is missing, the cursor crashes with the error code SYNTAX_ERROR_FUNCTION.

If the functions BEGIN block is reached, the cursor checks, if the function name matches the name of the function, it is supposed to run. If that is not the case, the cursor skips the next lines until it reache the corresponding END block.

If the function name matches, the cursor adds a function item initialized with the functions name and line onto its stack. It then fetches the variables and constants from the input inventory of the block that triggered the process and pushes them onto its stack (hello Rust 😊). Constants are converted to variables. If there is no variable for a required PARAMETER, the cursor crashes with the error code FUNCTION_MISSING_INPUT. If the stack is full when trying to push the function item or a variable onto it, the cursor crashes with a STACK_OVERFLOW error.

If the function has more RESULTS declared than variables have been fetched, uninitialized variable in the input inventory of the calling trigger will be consumed to make up the difference. If that fails, the cursor crashes with the error code FUNCTION_MISSING_INPUT.

If the function has less RESULTS than provided PARAMETERS, additional uninitialized variables are crrated in the output inventory of the trigger block. for every RESULT, a matching variable is created on the stack. If the stack is full when trying to push a variable onto it, the cursor crashes with a STACK_OVERFLOW error.
Afterwards, all items that are not uninitialized variables will be moved from the input inventory of the trigger block to its output directory.

#### When a cursor reaches an ASSIGN statement

#### When a cursor reaches an IF statement

#### When a cursor reaches a FOR statement

#### When a cursor reaches an EACH statement

#### When a cursor reaches a WHILE statement

#### When a cursor reaches a REPEAT statement

### Error codes
SYNTAX_ERROR_INDENTATION: A line has a wrong indentation
SYNTAX_ERROR_PLACEMENT: A line is not allowed in that context/code block
SYNTAX_ERROR_BEGIN_WITHOUT_END: A BEGIN line has no matching END line
BROKEN_LIBRARY: A library could not be read or does not contain the required elements
SYNTAX_ERROR_STRUCT: A part of a struct declaration is wrong
SYNTAX_ERROR_VARIABLE: A part of a variable declaration is wrong
SYNTAX_ERROR_CONSTANT: A part of a constant declaration is wrong
SYNTAX_ERROR_TYPE_MISMATCH: A constant or variable is initialized with data that does not match its type or a variable is assigned date that does not match its type.
UNKNOWN_STRUCT_ENTITY: A declaration references the name of a STRUCT that has not been defined (yet).
STACK_OVERFLOW: The cursors stack is full and a new item was pushed to the stack.
FUNCTION_MISSING_INPUT: A function has been called and not all mandatory parameters have been set by the calling 
