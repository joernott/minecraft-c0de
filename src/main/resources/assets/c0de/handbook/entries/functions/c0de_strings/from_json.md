```json
{
  "title": "FROM_JSON",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 18
}
```

Reads a JSON document into a variable.

`destination` must be a single variable name\; the function writes into it directly. The document must fit the type of the variable. On an error (TYPE\_MISMATCH, INVALID\_ARGUMENT, UNKNOWN\_KEY, STRING\_TOO\_LONG) the variable stays unchanged.

An element of a struct whose key is missing in the document, or `null`, keeps its current value. So the value the variable had before gives the defaults. Arrays and hashes are replaced completely.

UNKNOWN\_KEY only comes when the variable has a TAG for `json` and the document has no value at the path of that tag. A missing key inside the value never raises it.

- `source` STRING: the JSON document
- `destination` variable: the variable that receives the values
- no results

```c0de
CALL c0de/strings/FROM_JSON readError
  PARAMETER source config ;
  PARAMETER destination port ;
```
