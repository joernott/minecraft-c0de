```json
{
  "title": "LABEL",
  "icon": "c0de:label_block",
  "category": "values_and_names",
  "ordinal": 0
}
```

A LABEL block holds a name.

Labels name programs, functions, variables and more. A name uses a-z, A-Z, 0-9 and the underscore, and it starts with a letter. Keywords in capitals are reserved: `RESULT` is not a valid name, but `result` is.

A label can point into a value with an index, like `list[0]`, `stock["gold"]` or `problem.code`. Right-click the block with an empty hand to enter the name and the index.

Each part of an index is a number, a text in quotes, an element name or the name of a variable or constant. A calculation like `list[i - 1]` doesn't work\; put `i - 1` into a variable first.

<recipe;c0de:label_block>

Craft a NOOP block and a paper together.

```c0de
VARIABLE score INTEGER
ASSIGN score score + 1 ;
```

All three `score` blocks are LABEL blocks. The first names the variable, the others refer to it.
