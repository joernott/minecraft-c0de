```json
{
  "title": "FIX",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 11
}
```

Schneidet die Nachkommastellen einer Zahl ab.

Es wird nicht gerundet: Aus -12.85 wird -12. Eine Zahl, die für einen INTEGER zu groß ist, löst NUMERIC\_OVERFLOW aus.

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` INTEGER: die Zahl ohne Nachkommastellen

```c0de
CALL c0de/math/FIX
  PARAMETER value price ;
  RESULT result coins
```
