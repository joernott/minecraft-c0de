```json
{
  "title": "REGEX_REPLACE",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 17
}
```

Replaces the matches of a regular expression, like the command `s` of sed.

In `replacement`, `&` stands for the whole match and `\1` to `\9` for the groups of the pattern\; `\&` and `\\` are a `&` and a backslash, `\n` is a line break. Without `flags`, only the first match is replaced. The flags are `g` for all matches, a number N for the Nth match (with `g`, the Nth and all after it), `i` to ignore the case, `m` for `^` and `$` in every line, and `p` to PRINT the result if something was replaced. Unknown flags, groups the pattern doesn't have and invalid patterns raise INVALID\_ARGUMENT.

- `text` STRING: the text
- `pattern` STRING: the regular expression
- `replacement` STRING: what replaces a match
- `flags` STRING: like sed, e.g. g or 2g (optional, default empty)
- result `result` STRING: the text with the replacements

```c0de
CALL c0de/strings/REGEX_REPLACE
  PARAMETER text "2026-11-15" ;
  PARAMETER pattern "([0-9]+)-([0-9]+)-([0-9]+)" ;
  PARAMETER replacement "\3.\2.\1" ;
  RESULT result german
```
