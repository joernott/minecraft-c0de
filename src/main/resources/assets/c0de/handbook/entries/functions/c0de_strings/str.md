```json
{
  "title": "STR",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 11
}
```

Converts a value to text.

The text is the same as the ⊕ operator creates.

- `value` BOOLEAN|INTEGER|FLOAT|CHAR|TIME: the value
- result `result` STRING: the value as text

```c0de
CALL c0de/strings/STR
  PARAMETER value score ;
  RESULT result scoreText
```
