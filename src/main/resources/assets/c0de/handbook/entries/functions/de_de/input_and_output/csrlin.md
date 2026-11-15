```json
{
  "title": "CSRLIN",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 5
}
```

Gibt die Zeile des Cursors zurück.

Es gilt der Bildschirm des zuerst gebundenen Geräts. Ohne Bildschirm ist das Ergebnis 0.

- Ergebnis `result` INTEGER: die Zeile des Cursors

```c0de
CALL CSRLIN
  RESULT result row
```
