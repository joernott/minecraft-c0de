```json
{
  "title": "SQR",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 3
}
```

Returns the square root of a number.

A negative number raises INVALID\_ARGUMENT.

- `value` INTEGER|FLOAT: the number, not negative
- result `result` FLOAT: the square root

```c0de
CALL c0de/math/SQR
  PARAMETER value area ;
  RESULT result side
```
