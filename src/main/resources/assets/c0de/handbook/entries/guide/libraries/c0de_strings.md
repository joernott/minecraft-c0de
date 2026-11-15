```json
{
  "title": "c0de/strings",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 6
}
```

Text and characters:

- LEFT, RIGHT, MID: parts of a text
- REPLACE: overwrite part of a text
- INSTR: find a text in a text, -1 if not found
- UCASE, LCASE: upper and lower case
- SPACE, FILL: a text of spaces or of one character
- ASC, CHR: characters and their code numbers

Conversions:

- STR: any simple value as text
- [FORMAT](^libraries/format): a value as text in a format you choose
- VAL: a text as a number
- HEX, OCT: a number in hexadecimal or octal

Regular expressions:

- REGEX\_MATCH: does a text match a pattern like `^[0-9]+$`?
- REGEX\_REPLACE: replace what a pattern matches, like sed
