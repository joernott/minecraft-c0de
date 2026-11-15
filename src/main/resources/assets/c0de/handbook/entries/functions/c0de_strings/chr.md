```json
{
  "title": "CHR",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 10
}
```

Returns the character with a Unicode code point.

A number that is not a code point raises INVALID\_ARGUMENT.

- `code` INTEGER: the code point
- result `result` CHAR: the character

```c0de
CALL c0de/strings/CHR
  PARAMETER code 65 ;
  RESULT result letter
```
