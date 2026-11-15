```json
{
  "title": "END",
  "icon": "c0de:end_block",
  "category": "code_blocks",
  "ordinal": 1
}
```

END closes a code block.

It has the same x as its BEGIN. The keyword that opened the block follows it, like `END FUNCTION` or `END WHILE`. In an IF with an ELSE, the first END stands alone. After `END REPEAT` comes the loop condition.

<recipe;c0de:end_block>

Turn one BEGIN block into an END block.

```c0de
FUNCTION answer
  RESULT result INTEGER
BEGIN
  ASSIGN result 42 ;
END FUNCTION
```
