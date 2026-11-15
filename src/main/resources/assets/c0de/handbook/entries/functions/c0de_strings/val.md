```json
{
  "title": "VAL",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 13
}
```

Converts a text to a number.

`isInteger` is true if the text is a whole number. Use FIX to turn the value into an INTEGER then. A text that is not a number raises INVALID\_ARGUMENT.

- `text` STRING: the text, e.g. "42" or "3.5"
- result `value` FLOAT: the number
- result `isInteger` BOOLEAN: true if the text is a whole number

```c0de
CALL c0de/strings/VAL
  PARAMETER text input ;
  RESULT value number
  RESULT isInteger whole
```
