```json
{
  "title": "OCT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 15
}
```

Returns the octal text of an integer.

Negative numbers use two's complement.

- `value` INTEGER: the number
- result `result` STRING: the octal text

```c0de
CALL c0de/strings/OCT
  PARAMETER value 8 ;
  RESULT result octText
```
