```json
{
  "title": "INKEY",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 8
}
```

Gibt die älteste Taste zurück, die ein Spieler auf einem Laptop oder Tablet gedrückt hat.

Es zählen nur Tasten, die gedrückt wurden, während kein INPUT wartet. Enter kommt als CHR 13 an. Wenn keine Taste wartet, ist das Ergebnis der leere String. INKEY wartet nicht.

- Ergebnis `result` STRING: die Taste oder der leere String

```c0de
CALL INKEY
  RESULT result key
```
