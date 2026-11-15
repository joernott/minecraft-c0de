```json
{
  "title": "FIX",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 11
}
```

Cuts off the decimal places of a number.

There is no rounding: -12.85 becomes -12. A number too large for an INTEGER raises NUMERIC\_OVERFLOW.

- `value` INTEGER|FLOAT: the number
- result `result` INTEGER: the number without decimal places

```c0de
CALL c0de/math/FIX
  PARAMETER value price ;
  RESULT result coins
```
