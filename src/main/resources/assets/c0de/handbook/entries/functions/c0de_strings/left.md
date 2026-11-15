```json
{
  "title": "LEFT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 0
}
```

Returns the first characters of a text.

If the text is shorter, you get the whole text. A negative length raises INVALID\_ARGUMENT.

- `text` STRING: the text
- `length` INTEGER: the number of characters
- result `result` STRING: the first `length` characters

```c0de
CALL c0de/strings/LEFT
  PARAMETER text name ;
  PARAMETER length 3 ;
  RESULT result short
```
