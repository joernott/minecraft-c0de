```json
{
  "title": "BREAK",
  "icon": "c0de:break_block",
  "category": "loops",
  "ordinal": 4
}
```

BREAK leaves the innermost loop at once.

It works in FOR, EACH, WHILE and REPEAT loops. It may have a condition: then it only leaves the loop if the condition is true. A BREAK outside of a loop is a mistake.

<recipe;c0de:break_block>

Craft a NOOP block and a stick together.

```c0de
FOR divisor INTEGER 2 ; candidate - 1 ;
BEGIN
  BREAK candidate \ divisor = 0 ;
END FOR
```

The loop stops as soon as a divisor is found.
