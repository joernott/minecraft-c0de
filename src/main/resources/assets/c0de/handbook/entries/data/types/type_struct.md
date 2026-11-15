```json
{
  "title": "TYPE_STRUCT",
  "icon": "c0de:type_struct_block",
  "category": "types",
  "ordinal": 8
}
```

TYPE\_STRUCT stands for the type STRUCT: your own type with named elements.

At x=5, the block starts a struct declaration. A LABEL with the type name follows, and a code block lists the elements. Elsewhere, it uses a struct: a LABEL with the struct name follows it.

<recipe;c0de:type_struct_block>

Craft a type base and a scaffolding block together.

```c0de
STRUCT point
BEGIN
  x INTEGER
  y INTEGER
END STRUCT
VARIABLE p STRUCT point
```

You reach the elements with an index, like `p.x`.
