```json
{
  "title": "LITERAL",
  "icon": "c0de:literal_block",
  "category": "values_and_names",
  "ordinal": 1
}
```

A LITERAL block holds a fixed value.

Right-click it with an empty hand. Pick the type and enter the value. A literal can be a BOOLEAN, INTEGER, FLOAT, CHAR, STRING or TIME. The block is an octagon, so you can tell it apart from a LABEL.

<recipe;c0de:literal_block>

Put a white wool block in the middle and eight NOOP blocks around it. You get eight LITERAL blocks.

The text notation writes literals like this: `TRUE`, `42`, `3.14`, `'a'`, `"Hello"` and `@2026-10-19T12:05:09Z`. In the dialog, you type a text without quotes, and a `"` is just a character. The text notation writes it as `\"`, a backslash as `\\` and a new line as `\n`: `"He said \"hi\""`. A line break can't be typed in the dialog\; use CHR of c0de/strings with the code 10.

```c0de
ASSIGN text "Hello, " ⊕ name ;
```
