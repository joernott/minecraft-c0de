```json
{
  "title": "Types",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 4
}
```

A type block follows the name of a variable, constant, parameter or result. Simple types:

- BOOLEAN: true or false
- INTEGER: whole numbers
- FLOAT: numbers with decimals
- CHAR: one character
- TIME: a point in time

Complex types:

- STRING: text, optionally with a length limit
- ARRAY: a list, numbered from 0
- HASH: values stored under text keys
- STRUCT: your own type with named elements
- ERROR: an error with code, message, context and line

ARRAY and HASH are followed by the type of their content, like `ARRAY INTEGER`. A STRUCT type is followed by a LABEL with the struct name. Arrays and hashes grow and shrink by themselves.
