```json
{
  "title": "TYPE_FLOAT",
  "icon": "c0de:type_float_block",
  "category": "types",
  "ordinal": 3
}
```

TYPE\_FLOAT stands for the type FLOAT: a number with decimals.

A new FLOAT holds 0.0. You may store an INTEGER in a FLOAT, but not the other way round. A division with `/` always gives a FLOAT.

<recipe;c0de:type_float_block>

<recipe;c0de:type_float_block_from_type_integer_block>

Craft a type base and a red sand block, or turn a TYPE\_INTEGER block into a TYPE\_FLOAT block with orange dye.

```c0de
VARIABLE half FLOAT
ASSIGN half 1 / 2 ;
```

Now `half` holds 0.5.
