```json
{
  "title": "RIGHT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 1
}
```

Returns the last characters of a text.

If the text is shorter, you get the whole text. A negative length raises INVALID\_ARGUMENT.

- `text` STRING: the text
- `length` INTEGER: the number of characters
- result `result` STRING: the last `length` characters

```c0de
CALL c0de/strings/RIGHT
  PARAMETER text name ;
  PARAMETER length 3 ;
  RESULT result ending
```
