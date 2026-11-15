```json
{
  "title": "ROUND",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 12
}
```

Rundet eine Zahl auf die nächste ganze Zahl.

Ein .5 geht zur nächsten geraden Zahl: Aus 12.5 wird 12, aus 13.5 wird 14. Eine Zahl, die für einen INTEGER zu groß ist, löst NUMERIC\_OVERFLOW aus.

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` INTEGER: die gerundete Zahl

```c0de
CALL c0de/math/ROUND
  PARAMETER value 12.5 ;
  RESULT result rounded
```
