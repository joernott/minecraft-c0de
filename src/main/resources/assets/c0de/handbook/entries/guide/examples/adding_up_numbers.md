```json
{
  "title": "Adding up numbers",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 1
}
```

```c0de
PROGRAM adder
NOOP
NOOP
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

This function adds all numbers from 1 to `n`. It calls itself with `n - 1` until `n` is 0, then adds `n` on the way back. For `n` = 10, the result is 55.

To try it, put in an INTEGER variable item named `n`. Each call uses room on the cursor's stack, so very large numbers can crash it.
