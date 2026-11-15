```json
{
  "title": "ATN",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 9
}
```

Gibt den Arkustangens einer Zahl zurück.

Das Ergebnis ist ein Winkel im Bogenmaß.

- `value` INTEGER|FLOAT: der Tangens
- Ergebnis `result` FLOAT: der Winkel im Bogenmaß

```c0de
CALL c0de/math/ATN
  PARAMETER value slope ;
  RESULT result angle
```
