```json
{
  "title": "DIV",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 14
}
```

Divides two integers and rounds towards 0.

-7 divided by 2 is -3. A divisor of 0 raises DIVISION\_BY\_ZERO.

- `dividend` INTEGER: the number to divide
- `divisor` INTEGER: the number to divide by, not 0
- result `result` INTEGER: the quotient without remainder

```c0de
CALL c0de/math/DIV
  PARAMETER dividend items ;
  PARAMETER divisor 64 ;
  RESULT result stacks
```
