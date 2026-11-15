```json
{
  "title": "SGN",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 2
}
```

Gibt das Vorzeichen einer Zahl zurück.

Das Ergebnis ist -1 für negative Zahlen, 0 für 0 und 1 für positive Zahlen.

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` INTEGER: -1, 0 oder 1

```c0de
CALL c0de/math/SGN
  PARAMETER value balance ;
  RESULT result sign
```
