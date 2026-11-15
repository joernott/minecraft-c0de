```json
{
  "title": "SGN",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 2
}
```

Returns the sign of a number.

The result is -1 for negative numbers, 0 for 0 and 1 for positive numbers.

- `value` INTEGER|FLOAT: the number
- result `result` INTEGER: -1, 0 or 1

```c0de
CALL c0de/math/SGN
  PARAMETER value balance ;
  RESULT result sign
```
