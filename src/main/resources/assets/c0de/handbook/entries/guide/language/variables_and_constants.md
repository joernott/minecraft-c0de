```json
{
  "title": "Variables and constants",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 5
}
```

A VARIABLE line has a name, a type and an optional start value. Without one, it holds the default: false, 0, 0.0, a space, an empty text or list.

A CONSTANT line looks the same, but it must have a value. You can't change it later. The value is a LITERAL, not a calculation and not another constant.

```c0de
VARIABLE b BOOLEAN TRUE
VARIABLE i INTEGER
CONSTANT BASE INTEGER 42
```

Lists, hashes and structs get their start values in a code block. Each line holds literals or a key and a value:

```c0de
VARIABLE list ARRAY INTEGER
BEGIN
  1 2 3
END VARIABLE
VARIABLE stock HASH INTEGER
BEGIN
  gold 1
END VARIABLE
```

**Recipes**

<item;c0de:variable_block>

<recipe;c0de:variable_block>

<item;c0de:constant_block>

<recipe;c0de:constant_block>

<item;c0de:literal_block>

<recipe;c0de:literal_block>
