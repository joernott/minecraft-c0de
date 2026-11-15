```json
{
  "title": "REPLACE",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 3
}
```

Overwrites characters of a text with another text.

The length of the text stays the same. Characters of `insert` that would make it longer are dropped. A start behind the end of the text raises INVALID\_ARGUMENT.

- `text` STRING: the text
- `start` INTEGER: the position where overwriting begins
- `insert` STRING: the new characters
- result `result` STRING: the changed text

```c0de
CALL c0de/strings/REPLACE
  PARAMETER text "Hello Alex" ;
  PARAMETER start 6 ;
  PARAMETER insert "Stev" ;
  RESULT result greeting
```
