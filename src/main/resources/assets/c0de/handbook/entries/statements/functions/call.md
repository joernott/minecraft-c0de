```json
{
  "title": "CALL",
  "icon": "c0de:call_block",
  "category": "functions",
  "ordinal": 4
}
```

A CALL line runs a function.

A LABEL with the function name follows. PARAMETER lines give it values, RESULT lines catch its results. A second LABEL on the CALL line may name an ERROR variable. It catches an error, and your function goes on.

Without that ERROR variable, an error stops your function too and goes on to its caller. You can't call a function inside an expression.

The ERROR variable gets the error of every call, also "no error": after a CALL without error, its `code` is 0 again.

<recipe;c0de:call_block>

Craft a NOOP block and a smooth stone block together.

```c0de
VARIABLE problem ERROR
CALL c0de/math/DIV problem
  PARAMETER dividend -7 ;
  PARAMETER divisor 2 ;
  RESULT result quotient
```
