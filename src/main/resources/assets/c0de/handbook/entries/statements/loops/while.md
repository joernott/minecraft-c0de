```json
{
  "title": "WHILE",
  "icon": "c0de:while_block",
  "category": "loops",
  "ordinal": 2
}
```

WHILE runs its code block as long as its condition is true.

It checks the condition before each round. If it is false right away, the code block never runs. The condition must give a BOOLEAN and ends with `;`.

<recipe;c0de:while_block>

<recipe;c0de:while_block_from_repeat_block>

Craft a structural base and a diorite block, or turn a REPEAT block into a WHILE block.

```c0de
VARIABLE n INTEGER 3
WHILE n > 0 ;
BEGIN
  ASSIGN countdown countdown ⊕ n ⊕ " " ;
  ASSIGN n n - 1 ;
END WHILE
```

Now `countdown` is "3 2 1 ".
