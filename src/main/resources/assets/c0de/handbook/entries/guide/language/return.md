```json
{
  "title": "RETURN",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 13
}
```

RETURN ends the function right away. It may name a variable of type ERROR to report a problem to the caller.

```c0de
FUNCTION sum
  PARAMETER n INTEGER
  RESULT result INTEGER
BEGIN
  IF n = 0 ;
  BEGIN
    RETURN
  END IF
  CALL sum
    PARAMETER n n - 1 ;
    RESULT result result
  ASSIGN result result + n ;
END FUNCTION
```

This function calls itself. It adds up all numbers from 1 to n.

**Recipes**

<item;c0de:return_block>

<recipe;c0de:return_block>

<item;c0de:type_error_block>

<recipe;c0de:type_error_block>
