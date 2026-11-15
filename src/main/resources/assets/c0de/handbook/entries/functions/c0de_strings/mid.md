```json
{
  "title": "MID",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 2
}
```

Returns a part of a text.

Positions start at 0. Near the end you get fewer characters. A start behind the end gives an empty text. A negative start or length raises INVALID\_ARGUMENT.

- `text` STRING: the text
- `start` INTEGER: the position of the first character
- `length` INTEGER: the number of characters (optional, default the rest of the text)
- result `result` STRING: the part of the text

```c0de
CALL c0de/strings/MID
  PARAMETER text name ;
  PARAMETER start 2 ;
  PARAMETER length 3 ;
  RESULT result part
```
