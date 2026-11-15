```json
{
  "title": "REPEAT",
  "icon": "c0de:repeat_block",
  "category": "loops",
  "ordinal": 3
}
```

REPEAT runs its code block, then checks its condition.

The REPEAT line holds only the REPEAT block. The condition comes after `END REPEAT`. The code block runs again as long as the condition is true, and it always runs at least once.

<recipe;c0de:repeat_block>

<recipe;c0de:repeat_block_from_while_block>

Craft a structural base and a granite block, or turn a WHILE block into a REPEAT block.

```c0de
REPEAT
BEGIN
  ASSIGN repeats repeats + 1 ;
END REPEAT n > 0 ;
```
