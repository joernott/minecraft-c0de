```json
{
  "title": "TYPE_STRING",
  "icon": "c0de:type_string_block",
  "category": "types",
  "ordinal": 5
}
```

TYPE\_STRING stands for the type STRING: a text.

A new STRING is empty. You may put an INTEGER literal after the type to limit the length. You can read one character with an index that starts at 0, like `name[0]`.

<recipe;c0de:type_string_block>

Craft a type base and a string together.

```c0de
VARIABLE greeting STRING "Hello"
VARIABLE short STRING 3
```

The second line makes a text with at most 3 characters.
