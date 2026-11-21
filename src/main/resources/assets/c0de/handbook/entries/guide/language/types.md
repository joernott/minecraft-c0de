```json
{
  "title": "Types",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 4
}
```

A [type block](^tools/type_blocks) follows the name of a variable, constant, parameter or result. Simple types:

- BOOLEAN: true or false
- INTEGER: whole numbers
- FLOAT: numbers with decimals
- CHAR: one character
- TIME: a point in time

Complex types:

- STRING: text, optionally with a length limit
- ARRAY: a list, numbered from 0
- HASH: values stored under text keys
- [STRUCT](^language/structs): your own type with named elements
- ERROR: an error with code, message, context and line
- TYPE: a type of a library of another mod, like the canvas of c0de paint

ARRAY and HASH are followed by the type of their content, like `ARRAY INTEGER`. A STRUCT type is followed by a LABEL with the struct name, a TYPE by a LABEL with the full name of the library type, like `TYPE c0de_paint/cpaint/CANVAS`. Arrays and hashes grow and shrink by themselves.

A value of a library type can't be looked into. You keep it in variables and hand it to the functions of its library. It has no literal, and there are no constants of it.

**Recipes**

<item;c0de:type_boolean_block>

<recipe;c0de:type_boolean_block>

<item;c0de:type_integer_block>

<recipe;c0de:type_integer_block>

<item;c0de:type_float_block>

<recipe;c0de:type_float_block>

<item;c0de:type_char_block>

<recipe;c0de:type_char_block>

<item;c0de:type_time_block>

<recipe;c0de:type_time_block>

<item;c0de:type_string_block>

<recipe;c0de:type_string_block>

<item;c0de:type_array_block>

<recipe;c0de:type_array_block>

<item;c0de:type_hash_block>

<recipe;c0de:type_hash_block>

<item;c0de:type_struct_block>

<recipe;c0de:type_struct_block>

<item;c0de:type_error_block>

<recipe;c0de:type_error_block>

<item;c0de:label_block>

<recipe;c0de:label_block>
