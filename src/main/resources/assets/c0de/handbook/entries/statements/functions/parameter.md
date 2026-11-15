```json
{
  "title": "PARAMETER",
  "icon": "c0de:parameter_block",
  "category": "functions",
  "ordinal": 1
}
```

A PARAMETER line is an input of a function.

Below a FUNCTION, it has a name, a type and an optional default value. Below a CALL, it has the name of a parameter and an expression with its value. PARAMETER lines are one block further east than their FUNCTION or CALL.

A caller may leave out parameters with a default value. Parameters are copies: changing them doesn't change the caller's variables. A parameter and a result of the same function can't have the same name (SYNTAX\_ERROR\_FUNCTION).

<recipe;c0de:parameter_block>

<recipe;c0de:parameter_block_from_result_block>

Put a hopper in the middle and eight NOOP blocks around it for eight PARAMETER blocks, or turn one RESULT block into a PARAMETER block.

```c0de
CALL greet
  PARAMETER name "c0de" ;
  RESULT result greeting
```
