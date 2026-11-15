```json
{
  "title": "ASC",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 9
}
```

Returns the Unicode code point of a character.

- `char` CHAR: the character
- result `result` INTEGER: its code point, e.g. 65 for A

```c0de
CALL c0de/strings/ASC
  PARAMETER char letter ;
  RESULT result code
```
