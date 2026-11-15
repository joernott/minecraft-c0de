```json
{
  "title": "ASSIGN",
  "icon": "c0de:assign_block",
  "category": "expressions",
  "ordinal": 0
}
```

ASSIGN stores the value of an expression in a variable.

A LABEL with the variable follows, then the expression. It ends with `;`. The value must fit the type: an INTEGER may go into a FLOAT, and a CHAR into a STRING. Lists and other complex values are copied.

<recipe;c0de:assign_block>

Put a cyan wool block in the middle and eight NOOP blocks around it. You get eight ASSIGN blocks.

```c0de
ASSIGN list[0] 10 ;
ASSIGN list[1] 20 ;
ASSIGN list[2] list[0] + list[1] ;
```
