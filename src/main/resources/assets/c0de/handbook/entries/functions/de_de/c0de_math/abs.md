```json
{
  "title": "ABS",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 1
}
```

Gibt den Betrag einer Zahl zurück.

Ein Ergebnis, das nicht in einen INTEGER passt, löst NUMERIC\_OVERFLOW aus.

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` wie `value`: die Zahl ohne ihr Vorzeichen

```c0de
CALL c0de/math/ABS
  PARAMETER value distance ;
  RESULT result steps
```
