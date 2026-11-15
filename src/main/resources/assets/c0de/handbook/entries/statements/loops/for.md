```json
{
  "title": "FOR",
  "icon": "c0de:for_block",
  "category": "loops",
  "ordinal": 0
}
```

FOR counts with a loop variable.

A name, a type (INTEGER or FLOAT), a start value and an end value follow. An optional third value is the step. Without it, the step is 1. The step may not be 0, and a negative step counts down.

The end value is included. The values are computed once, before the loop starts. You can't change the loop variable yourself.

<recipe;c0de:for_block>

<recipe;c0de:for_block_from_each_block>

Craft a structural base and a dirt block, or turn an EACH block into a FOR block.

```c0de
FOR i INTEGER 1 ; 10 ; 2 ;
BEGIN
  ASSIGN sum sum + i ;
END FOR
```

The loop runs with i = 1, 3, 5, 7 and 9.
