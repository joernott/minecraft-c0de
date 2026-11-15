```json
{
  "title": "FROM_YAML",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 19
}
```

Reads a YAML document into a variable.

`destination` must be a single variable name\; the function writes into it directly. Only the first document is read. On an error (TYPE\_MISMATCH, INVALID\_ARGUMENT, UNKNOWN\_KEY, STRING\_TOO\_LONG) the variable stays unchanged.

Like FROM\_JSON, missing or `null` keys keep the current value of a struct element. UNKNOWN\_KEY only comes when the variable has a TAG for `yaml` and the document has no value at its path.

- `source` STRING: the YAML document
- `destination` variable: the variable that receives the values
- no results

```c0de
CALL c0de/strings/FROM_YAML readError
  PARAMETER source config ;
  PARAMETER destination settings ;
```
