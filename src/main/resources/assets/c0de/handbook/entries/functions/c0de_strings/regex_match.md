```json
{
  "title": "REGEX_MATCH",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 16
}
```

Checks whether a text matches a regular expression.

The pattern may match any part of the text\; `^` and `$` tie it to the start and the end. c0de uses the regular expressions of Java, which have the syntax of PCRE: classes like `[a-z]`, `\d` and `\w`, the quantifiers `*`, `+`, `?` and `{2,4}`, groups, lookahead and lookbehind, and inline flags like `(?i)` for ignoring the case. A pattern that is not valid raises INVALID\_ARGUMENT, and so does a pattern that takes too long.

- `text` STRING: the text
- `pattern` STRING: the regular expression
- result `result` BOOLEAN: TRUE if the pattern matches

```c0de
CALL c0de/strings/REGEX_MATCH
  PARAMETER text input ;
  PARAMETER pattern "^[0-9]{4}-[0-9]{2}-[0-9]{2}$" ;
  RESULT result isDate
```
