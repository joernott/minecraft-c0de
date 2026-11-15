```json
{
  "title": "SPACE",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 7
}
```

Returns a text of spaces.

`count` must be between 0 and 1,000,000, otherwise INVALID\_ARGUMENT is raised.

- `count` INTEGER: the number of spaces
- result `result` STRING: the spaces

```c0de
CALL c0de/strings/SPACE
  PARAMETER count 8 ;
  RESULT result indent
```
