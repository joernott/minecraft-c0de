```json
{
  "title": "ABS",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 1
}
```

Returns the absolute value of a number.

A result that doesn't fit into an INTEGER raises NUMERIC\_OVERFLOW.

- `value` INTEGER|FLOAT: the number
- result `result` like `value`: the number without its sign

```c0de
CALL c0de/math/ABS
  PARAMETER value distance ;
  RESULT result steps
```
