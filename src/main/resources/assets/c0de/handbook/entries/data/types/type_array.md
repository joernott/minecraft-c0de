```json
{
  "title": "TYPE_ARRAY",
  "icon": "c0de:type_array_block",
  "category": "types",
  "ordinal": 6
}
```

TYPE\_ARRAY stands for the type ARRAY: a list of values.

An array is numbered from 0. It grows and shrinks by itself. After ARRAY comes the type of its content, like `ARRAY INTEGER`. Arrays of arrays are allowed, up to 8 levels deep.

<recipe;c0de:type_array_block>

Craft a type base and a barrel together.

```c0de
VARIABLE list ARRAY INTEGER
BEGIN
  1 2 3
END VARIABLE
ASSIGN list[3] 4 ;
```

Assigning to the index right after the end adds a new element.
