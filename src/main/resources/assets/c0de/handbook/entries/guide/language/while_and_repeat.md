```json
{
  "title": "WHILE and REPEAT",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 17
}
```

WHILE checks its condition first. If it is false right away, the block never runs. REPEAT checks at the end, so its block runs at least once. Its condition comes after `END REPEAT`.

```c0de
WHILE n > 0 ;
BEGIN
  ASSIGN countdown countdown ⊕ n ⊕ " " ;
  ASSIGN n n - 1 ;
END WHILE
REPEAT
BEGIN
  ASSIGN repeats repeats + 1 ;
END REPEAT n > 0 ;
```

**Recipes**

<item;c0de:while_block>

<recipe;c0de:while_block>

<item;c0de:repeat_block>

<recipe;c0de:repeat_block>
