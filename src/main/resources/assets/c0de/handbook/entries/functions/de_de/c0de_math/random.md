```json
{
  "title": "RANDOM",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 15
}
```

Gibt eine Zufallszahl zwischen 0 und 1 zurück.

0 ist möglich, 1 nicht. Jeder Prozess hat seinen eigenen Zufallszahlengenerator.

- Ergebnis `result` FLOAT: eine Zufallszahl von 0 bis unter 1

```c0de
CALL c0de/math/RANDOM
  RESULT result chance
```
