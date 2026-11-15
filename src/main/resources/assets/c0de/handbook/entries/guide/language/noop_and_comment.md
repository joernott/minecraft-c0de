```json
{
  "title": "NOOP and COMMENT",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 2
}
```

NOOP means "no operation". A NOOP line does nothing. Use it to separate parts of your program. A NOOP line may not hold any other block. Every [FUNCTION](^language/functions) needs a NOOP line right above it.

A COMMENT block holds a note for humans. Put it at the end of a line. The cursor ignores it and every block after it. So a COMMENT in front of blocks switches them off.

A line that starts with a COMMENT block is a comment line. It is skipped like a NOOP line.

```c0de
// A comment line is skipped completely
VARIABLE time INTEGER 1 // a comment at the end
ASSIGN count time ; COMMENT("disabled") + 100 ;
```

In the last line, `COMMENT("disabled")` is a COMMENT block with the text "disabled". The `+ 100 ;` after it is switched off.

**Recipes**

<item;c0de:noop_block>

<recipe;c0de:noop_block>

<item;c0de:function_block>

<recipe;c0de:function_block>

<item;c0de:comment_block>

<recipe;c0de:comment_block>
