```json
{
  "title": "VARIABLE",
  "icon": "c0de:variable_block",
  "category": "values_and_names",
  "ordinal": 2
}
```

A VARIABLE line makes a variable: a named place for a value that can change.

A name and a type follow the block. A literal may give the start value. Without one, the variable holds the default of its type. Arrays, hashes and structs get their start values in a code block.

A variable made inside a code block lives only inside that block. A variable at x=5 is global.

<recipe;c0de:variable_block>

<recipe;c0de:variable_block_from_constant_block>

Craft a type base and a bone block, or turn a CONSTANT block into a VARIABLE block.

```c0de
VARIABLE i INTEGER
VARIABLE name STRING "Steve"
VARIABLE list ARRAY INTEGER
BEGIN
  1 2 3
END VARIABLE
```
