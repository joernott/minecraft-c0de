```json
{
  "title": "Names",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 3
}
```

LABEL blocks hold names. A name uses the letters a-z and A-Z, the digits 0-9 and the underscore. It must start with a letter.

Keywords in capitals are reserved: `RESULT` is not a valid name, but `result` and `Result` are. Library names use slashes, like `c0de/math/PI`.

A label can point into a value with an **index**, like `list[0]`, `stock["gold"]` or `problem.code`. You enter the index as text in the LABEL dialog.

Each part of an index is a number, a text in quotes, an element name or the name of a variable or constant, like `list[i]`. It can't be a calculation: `list[i - 1]` doesn't work. Put `i - 1` into a variable first and use that.
