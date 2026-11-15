```json
{
  "title": "INT",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 10
}
```

Rounds a number down to an integer.

-12.85 becomes -13. A number too large for an INTEGER raises NUMERIC\_OVERFLOW.

- `value` INTEGER|FLOAT: the number
- result `result` INTEGER: the largest integer not greater than `value`

```c0de
CALL c0de/math/INT
  PARAMETER value height ;
  RESULT result blocks
```
