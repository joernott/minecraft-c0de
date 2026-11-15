```json
{
  "title": "Prime numbers",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 2
}
```

```c0de
PROGRAM primefinder
NOOP
NOOP
FUNCTION main
  RESULT primes STRING
BEGIN
  FOR candidate INTEGER 2 ; 20 ;
  BEGIN
    VARIABLE prime BOOLEAN TRUE
    FOR divisor INTEGER 2 ; candidate - 1 ;
    BEGIN
      IF candidate \ divisor = 0 ;
      BEGIN
        ASSIGN prime FALSE ;
        BREAK
      END IF
    END FOR
    IF prime ;
    BEGIN
      ASSIGN primes primes ⊕ candidate ⊕ " " ;
    END IF
  END FOR
END FUNCTION
```

The outer loop tries every number from 2 to 20. The inner loop checks if a smaller number divides it with no remainder (`\`). If so, it is no prime, and BREAK stops the inner loop. The result is "2 3 5 7 11 13 17 19 ".
