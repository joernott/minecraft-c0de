```json
{
  "title": "FILL",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 8
}
```

Gibt einen Text zurück, der ein Zeichen wiederholt.

`count` muss zwischen 0 und 1.000.000 liegen, sonst wird INVALID\_ARGUMENT ausgelöst.

- `count` INTEGER: wie oft das Zeichen wiederholt wird
- `char` CHAR: das Zeichen
- Ergebnis `result` STRING: das wiederholte Zeichen

```c0de
CALL c0de/strings/FILL
  PARAMETER count 20 ;
  PARAMETER char '-' ;
  RESULT result line
```
