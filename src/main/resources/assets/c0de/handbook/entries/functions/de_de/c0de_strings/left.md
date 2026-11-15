```json
{
  "title": "LEFT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 0
}
```

Gibt die ersten Zeichen eines Texts zurück.

Wenn der Text kürzer ist, bekommst du den ganzen Text. Eine negative Länge löst INVALID\_ARGUMENT aus.

- `text` STRING: der Text
- `length` INTEGER: die Anzahl der Zeichen
- Ergebnis `result` STRING: die ersten `length` Zeichen

```c0de
CALL c0de/strings/LEFT
  PARAMETER text name ;
  PARAMETER length 3 ;
  RESULT result short
```
