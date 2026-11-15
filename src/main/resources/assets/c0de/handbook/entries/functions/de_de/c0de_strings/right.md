```json
{
  "title": "RIGHT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 1
}
```

Gibt die letzten Zeichen eines Texts zurück.

Wenn der Text kürzer ist, bekommst du den ganzen Text. Eine negative Länge löst INVALID\_ARGUMENT aus.

- `text` STRING: der Text
- `length` INTEGER: die Anzahl der Zeichen
- Ergebnis `result` STRING: die letzten `length` Zeichen

```c0de
CALL c0de/strings/RIGHT
  PARAMETER text name ;
  PARAMETER length 3 ;
  RESULT result ending
```
