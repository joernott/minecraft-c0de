```json
{
  "title": "FILL",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 8
}
```

Returns a text that repeats one character.

`count` must be between 0 and 1,000,000, otherwise INVALID\_ARGUMENT is raised.

- `count` INTEGER: how often the character is repeated
- `char` CHAR: the character
- result `result` STRING: the repeated character

```c0de
CALL c0de/strings/FILL
  PARAMETER count 20 ;
  PARAMETER char '-' ;
  RESULT result line
```
