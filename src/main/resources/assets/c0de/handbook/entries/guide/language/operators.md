```json
{
  "title": "Operators",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 9
}
```

- math: `+` `-` `*` `/` `^` (power) `\` (remainder)
- compare: `=` `≠` `<` `>` `≤` `≥`
- logic: `NOT` `AND` `OR` `XOR`
- text: `⊕` joins two values into a text
- bits: `&` `|` `⊻` `¬` `←` `→`

`=` only compares. To store a value, use [ASSIGN](^language/assign). `⊕` turns numbers and lists into text, so `'a' ⊕ 1 ⊕ "b"` gives `"a1b"`.

`\` only works with INTEGERs. The result keeps the sign of the left number: `-7 \ 2` is `-1`. Dividing by zero raises DIVISION\_BY\_ZERO, with `/` and `\`, for FLOATs too: `1.5 / 0.0` doesn't give infinity.

TIME values can be compared with `<`, `>` and the others: the earlier time is the smaller one. `⊕` writes a TIME as ISO text in UTC with milliseconds, like `2026-10-19T12:05:09.250Z`. For the local time, use [FORMAT](^libraries/format) of [c0de/strings](^libraries/c0de_strings).

**Recipes**

<item;c0de:assign_block>

<recipe;c0de:assign_block>

<item;c0de:type_time_block>

<recipe;c0de:type_time_block>
