```json
{
  "title": "SQR",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 3
}
```

Gibt die Quadratwurzel einer Zahl zurück.

Eine negative Zahl löst INVALID\_ARGUMENT aus.

- `value` INTEGER|FLOAT: die Zahl, nicht negativ
- Ergebnis `result` FLOAT: die Quadratwurzel

```c0de
CALL c0de/math/SQR
  PARAMETER value area ;
  RESULT result side
```
