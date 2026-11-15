```json
{
  "title": "FUNCTION",
  "icon": "c0de:function_block",
  "category": "functions",
  "ordinal": 0
}
```

A FUNCTION line starts a function.

It sits at x=5, right below a NOOP line. A LABEL with the function name follows. PARAMETER and RESULT lines may come next, then the code block. It ends with `END FUNCTION`.

A function can only call functions above it, or itself. Struct and function declarations are not allowed inside a function.

<recipe;c0de:function_block>

Craft a NOOP block and a stone block together.

```c0de
NOOP
FUNCTION greet
  PARAMETER name STRING "World"
  PARAMETER mark CHAR '!'
  RESULT result STRING
BEGIN
  ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```
