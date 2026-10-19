# Functions

[c0de concept](../concept.md) › Functions

## Declaring functions

A function is a multi-line statement in the global scope. The line directly before it MUST be a NOOP line. It starts with a line that contains the FUNCTION keyword at x=5, directly followed by a valid, unique LABEL with the function name.

The following lines MAY define zero or more parameters. Each parameter line starts with the PARAMETER keyword, indented by one block relative to the FUNCTION keyword. The PARAMETER keyword MUST be directly followed by a valid LABEL and a type. If the type is a simple type or STRING, the parameter MAY have a default value, defined by following the type with a LITERAL.

The calling entity MUST set the value of every parameter that has no default value. Parameters are always passed by value: they contain a copy of the value they were called with and behave like variables declared in the scope of the function.

After the parameters, zero or more result lines MAY be defined. Each result line starts with the RESULT keyword, indented by one block relative to the FUNCTION keyword. It MUST be directly followed by a LABEL and a type. A result behaves like a variable declared in the scope of the function and is initialized with the default value of its type. When the function ends, the last value assigned to a result is returned to the calling entity.

After that, a valid code block containing the statements of the function MUST follow. Its BEGIN block is at x=5. The END keyword of that block MUST be directly followed by a FUNCTION keyword, which MAY be followed by a LABEL repeating the function name. The code block of a function can contain nested code blocks, but neither STRUCT nor FUNCTION statements.

## Returning from functions

The code block of a function MAY contain RETURN statements. A RETURN statement immediately ends all code blocks nested in the code block of the function and ends the function. It consists of the RETURN keyword, which MAY be directly followed by a LABEL referring to a previously defined variable or constant of type ERROR. That error is returned to the calling entity. Without such a label, the function returns an error with code 0.

## Calling functions

A CALL statement calls a function. Its first line contains the CALL keyword, directly followed by a LABEL containing the name of the function. A second LABEL MAY follow, referring to a variable of type ERROR that receives the error returned by the called function.

A called function MUST be a built-in function, a function of an included library or a program function that has been declared before the CALL statement. A function MAY also call itself, which enables recursion.

The lines directly after the CALL line provide the parameters. They are indented by one block relative to the CALL keyword. Each parameter line consists of the PARAMETER keyword, directly followed by a LABEL with the name of a parameter of the called function, directly followed by an expression that provides its value. Every parameter without a default value MUST be provided, parameters with a default value MAY be provided. Each parameter MUST NOT be provided more than once.

After the parameter lines (or directly after the CALL line, if no parameters are provided), result lines MAY follow, indented the same way. Each result line consists of the RESULT keyword, directly followed by a LABEL with the name of a result of the called function, directly followed by a LABEL referring to a variable that receives the value of that result. Results that are not listed are discarded.

If the called function returns an error with a code other than 0 and the CALL statement has no error label, the error is raised in the calling function (see [Error handling](15-cursor.md#error-handling)).

## Minecraft blocks

### Function block

The FUNCTION block is a structural block. It is light gray (color #AAAAAA) when it is directly followed by a LABEL block with a valid, unique name and dark gray (color #555555) otherwise. All sides show a white circle with the letter ƒ (symbol for function) in it. It can only be placed at x=5 and only if the line directly before it (z-1) contains a NOOP statement.

#### Crafting

A FUNCTION block can be crafted shapelessly from a NOOP block and a stone block.

### Parameter block

The PARAMETER block is a dependent block of the FUNCTION and CALL blocks. It can only be placed in the line directly after a FUNCTION or CALL line or after another PARAMETER line, indented by one block relative to the FUNCTION or CALL block. It looks like a hopper. Its color is dark purple (color #AA00AA) when the parameter line is incomplete and light purple (color #FF55FF) when it is complete.

#### Crafting

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

### Result block

The RESULT block is a dependent block of the FUNCTION and CALL blocks. It can only be placed in the line directly after a FUNCTION or CALL line, a PARAMETER line or another RESULT line, indented by one block relative to the FUNCTION or CALL block. It looks like a hopper pointing upwards. Its color is dark purple (color #AA00AA) when the result line is incomplete and light purple (color #FF55FF) when it is complete.

#### Crafting

One RESULT block can be crafted shapelessly from one PARAMETER block.

### Return block

The RETURN block is dark purple (color #AA00AA) when the RETURN statement is invalid and light purple (color #FF55FF) when it is valid. All six sides show a spruce door.

#### Crafting

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

### Call block

The CALL block is a structural block. It is light gray (color #AAAAAA) when it forms a valid CALL statement and dark gray (color #555555) otherwise. All sides show a white circle with the symbol 📞 (telephone) in it.

#### Crafting

A CALL block can be crafted shapelessly from a NOOP block and a smooth stone block.
