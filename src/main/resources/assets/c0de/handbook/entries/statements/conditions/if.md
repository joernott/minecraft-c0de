```json
{
  "title": "IF",
  "icon": "c0de:if_block",
  "category": "conditions",
  "ordinal": 1
}
```

IF runs its code block only when its expression is true.

The expression must give a BOOLEAN and ends with `;`. A code block follows. The last END of the condition is followed by an IF block.

There is no ELSE IF. Put another IF inside the ELSE code block instead.

<recipe;c0de:if_block>

<recipe;c0de:if_block_from_else_block>

Craft a structural base and wheat seeds, or turn an ELSE block into an IF block.

```c0de
IF candidate \ divisor = 0 ;
BEGIN
  ASSIGN prime FALSE ;
END IF
```
