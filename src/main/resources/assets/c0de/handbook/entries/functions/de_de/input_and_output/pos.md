```json
{
  "title": "POS",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 6
}
```

Gibt die Spalte des Cursors zurück.

Es gilt der Bildschirm des zuerst gebundenen Geräts. Ohne Bildschirm ist das Ergebnis 0.

- Ergebnis `result` INTEGER: die Spalte des Cursors

```c0de
CALL POS
  RESULT result column
```
