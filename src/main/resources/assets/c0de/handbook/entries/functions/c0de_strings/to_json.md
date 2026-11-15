```json
{
  "title": "TO_JSON",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 16
}
```

Converts a value to a JSON document.

With `indent` 0 the document is one line. If `value` is a single variable with a json tag, the value is placed at the path of the tag. `indent` outside 0 to 16 raises INVALID\_ARGUMENT.

- `value` any type: the value to convert
- `indent` INTEGER: spaces per level, 0 to 16 (optional, default 0)
- result `result` STRING: the JSON document

```c0de
CALL c0de/strings/TO_JSON
  PARAMETER value config ;
  PARAMETER indent 2 ;
  RESULT result json
```
