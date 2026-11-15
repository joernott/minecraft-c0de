```json
{
  "title": "INSTR",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 4
}
```

Finds a text inside another text.

The result is the position of the first match, or -1 if there is none.

- `text` STRING: the text to search in
- `search` STRING: the text to find
- `start` INTEGER: where the search begins (optional, default 0)
- result `result` INTEGER: the position, or -1

```c0de
CALL c0de/strings/INSTR
  PARAMETER text message ;
  PARAMETER search "diamond" ;
  RESULT result position
```
