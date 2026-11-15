```json
{
  "title": "A crash on purpose",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 5
}
```

```c0de
PROGRAM errors
NOOP
NOOP
FUNCTION divide
  PARAMETER a INTEGER
  PARAMETER b INTEGER
  RESULT result INTEGER
BEGIN
  VARIABLE quotient FLOAT
  ASSIGN quotient a / b ;
END FUNCTION
NOOP
FUNCTION main
BEGIN
  CALL divide
    PARAMETER a 1 ;
    PARAMETER b 0 ;
END FUNCTION
```

Dividing by zero raises DIVISION\_BY\_ZERO in `divide`. The CALL in `main` has no ERROR variable to catch it, so the error moves up. In the entry function, it crashes the process. The trigger dialog shows the error and line 9, and a comparator reads 15.

**Recipes**

<item;c0de:call_block>

<recipe;c0de:call_block>

<item;c0de:type_error_block>

<recipe;c0de:type_error_block>
