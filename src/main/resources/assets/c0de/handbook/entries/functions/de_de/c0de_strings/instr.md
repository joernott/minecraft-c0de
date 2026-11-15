```json
{
  "title": "INSTR",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 4
}
```

Findet einen Text in einem anderen Text.

Das Ergebnis ist die Position des ersten Treffers oder -1, wenn es keinen gibt.

- `text` STRING: der Text, in dem gesucht wird
- `search` STRING: der gesuchte Text
- `start` INTEGER: wo die Suche beginnt (optional, Standard 0)
- Ergebnis `result` INTEGER: die Position oder -1

```c0de
CALL c0de/strings/INSTR
  PARAMETER text message ;
  PARAMETER search "diamond" ;
  RESULT result position
```
