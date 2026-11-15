```json
{
  "title": "LOG",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 5
}
```

Gibt den natürlichen Logarithmus einer Zahl zurück.

Die Zahl muss größer als 0 sein, sonst wird INVALID\_ARGUMENT ausgelöst.

- `value` INTEGER|FLOAT: die Zahl, größer als 0
- Ergebnis `result` FLOAT: der natürliche Logarithmus

```c0de
CALL c0de/math/LOG
  PARAMETER value amount ;
  RESULT result logarithm
```
