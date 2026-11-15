```json
{
  "title": "ROUND",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 12
}
```

Rounds a number to the nearest integer.

A .5 goes to the nearest even integer: 12.5 becomes 12, 13.5 becomes 14. A number too large for an INTEGER raises NUMERIC\_OVERFLOW.

- `value` INTEGER|FLOAT: the number
- result `result` INTEGER: the rounded number

```c0de
CALL c0de/math/ROUND
  PARAMETER value 12.5 ;
  RESULT result rounded
```
