```json
{
  "title": "TO_FLOAT",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 13
}
```

Converts a number to a FLOAT.

You rarely need it. An INTEGER goes into a FLOAT variable, parameter or result by itself, `/` always gives a FLOAT, and the math functions and `%f` of FORMAT take INTEGERs too.

Use it when a calculation with INTEGERs gets too big: `+`, `*` and `^` of two INTEGERs raise NUMERIC\_OVERFLOW above 9223372036854775807, with a FLOAT they don't. Or when a text should show a FLOAT: `⊕` writes the INTEGER 8 as `8` and the FLOAT as `8.0`.

- `value` INTEGER|FLOAT: the number
- result `result` FLOAT: the same number as FLOAT

```c0de
CALL c0de/math/TO_FLOAT
  PARAMETER value count ;
  RESULT result amount
```
