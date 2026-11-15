```json
{
  "title": "SPACE",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 7
}
```

Gibt einen Text aus Leerzeichen zurück.

`count` muss zwischen 0 und 1.000.000 liegen, sonst wird INVALID\_ARGUMENT ausgelöst.

- `count` INTEGER: die Anzahl der Leerzeichen
- Ergebnis `result` STRING: die Leerzeichen

```c0de
CALL c0de/strings/SPACE
  PARAMETER count 8 ;
  RESULT result indent
```
