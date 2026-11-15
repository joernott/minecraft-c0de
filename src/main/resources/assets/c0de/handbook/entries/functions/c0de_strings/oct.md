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

- `text` STRING: the text
- `pattern` STRING: the regular expression
- `replacement` STRING: what replaces a match
- `flags` STRING: like sed, e.g. g or 2g (optional, default empty)
- result `result` STRING: the text with the replacements

in every line, and `p` to PRINT the result if something was replaced. Unknown flags, groups the pattern doesn't have and invalid patterns raise INVALID\_ARGUMENT.

```c0de
CALL c0de/strings/REGEX_REPLACE
  PARAMETER text "2026-11-15" ;
  PARAMETER pattern "([0-9]+)-([0-9]+)-([0-9]+)" ;
  PARAMETER replacement "\3.\2.\1" ;
  RESULT result german
```
