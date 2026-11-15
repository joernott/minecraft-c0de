```json
{
  "title": "TO_YAML",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 17
}
```

Converts a value to a YAML document.

The document uses block style with two spaces indentation. If `value` is a single variable with a yaml tag, the value is placed at the path of the tag.

- `value` any type: the value to convert
- result `result` STRING: the YAML document

```c0de
CALL c0de/strings/TO_YAML
  PARAMETER value config ;
  RESULT result yaml
```
