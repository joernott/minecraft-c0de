```json
{
  "title": "Lines and indentation",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 1
}
```

Global lines start at x=5: PROGRAM, INCLUDE, STRUCT, FUNCTION and global VARIABLE and CONSTANT lines. Lines inside a code block are one block further east than its BEGIN.

A code block starts with BEGIN and ends with END. END is followed by the keyword that opened the block, like `END FUNCTION` or `END WHILE`. A code block may not be empty.

Variables and constants made inside a code block only live inside it. They are forgotten when the block ends.
