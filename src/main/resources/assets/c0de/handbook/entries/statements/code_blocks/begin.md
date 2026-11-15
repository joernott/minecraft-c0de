```json
{
  "title": "BEGIN",
  "icon": "c0de:begin_block",
  "category": "code_blocks",
  "ordinal": 0
}
```

BEGIN starts a code block.

It has the same x as the line that needs the code block. The lines inside are one block further east. Variables made inside a code block only live there. A code block may not be empty: a NOOP line is enough.

<recipe;c0de:begin_block>

<recipe;c0de:begin_block_from_end_block>

Put a yellow wool block in the middle and eight NOOP blocks around it for eight BEGIN blocks, or turn one END block into a BEGIN block.

```c0de
WHILE n > 0 ;
BEGIN
  ASSIGN n n - 1 ;
END WHILE
```
