```json
{
  "title": "REPLACE",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 3
}
```

Überschreibt Zeichen eines Texts mit einem anderen Text.

Die Länge des Texts bleibt gleich. Zeichen von `insert`, die ihn länger machen würden, fallen weg. Ein Start hinter dem Ende des Texts löst INVALID\_ARGUMENT aus.

- `text` STRING: der Text
- `start` INTEGER: die Position, an der das Überschreiben beginnt
- `insert` STRING: die neuen Zeichen
- Ergebnis `result` STRING: der geänderte Text

```c0de
CALL c0de/strings/REPLACE
  PARAMETER text "Hello Alex" ;
  PARAMETER start 6 ;
  PARAMETER insert "Stev" ;
  RESULT result greeting
```
