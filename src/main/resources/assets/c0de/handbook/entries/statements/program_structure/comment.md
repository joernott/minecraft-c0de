```json
{
  "title": "COMMENT",
  "icon": "c0de:comment_block",
  "category": "program_structure",
  "ordinal": 1
}
```

A COMMENT block holds a note for humans.

Put it at the end of a line. The cursor ignores the COMMENT and every block after it. So a COMMENT in front of blocks switches them off.

A line that starts with a COMMENT is a comment line. It is skipped like a NOOP line. Right-click the block with an empty hand to write its text.

<recipe;c0de:comment_block>

Put a glass block in the middle and eight NOOP blocks around it. You get eight COMMENT blocks.

The text notation writes a COMMENT block as `//` followed by its text.

```c0de
// A comment line is skipped completely
VARIABLE time INTEGER 1 // a comment at the end
```
