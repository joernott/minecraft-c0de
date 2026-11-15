```json
{
  "title": "Math library",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 3
}
```

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
NOOP
FUNCTION main
  RESULT rounded INTEGER
  RESULT quotient INTEGER
  RESULT root FLOAT
  RESULT circle STRING
BEGIN
  CALL c0de/math/ROUND
    PARAMETER value 12.5 ;
    RESULT result rounded
  CALL c0de/math/DIV
    PARAMETER dividend -7 ;
    PARAMETER divisor 2 ;
    RESULT result quotient
  CALL c0de/math/SQR
    PARAMETER value 9 ;
    RESULT result root
  CALL c0de/strings/FORMAT
    PARAMETER value c0de/math/PI ;
    PARAMETER format "%.2f" ;
    RESULT result circle
END FUNCTION
```

This program loads two libraries. ROUND turns 12.5 into 12, DIV divides -7 by 2 and gives -3, and SQR gives 3.0. [FORMAT](^libraries/format) shows PI with two decimals: "3.14". The function has four results, so put in four [uninitialized variable items](^running_programs/uninitialized_items).

**Recipes**

<item;c0de:variable_item>

<recipe;c0de:variable_item_uninitialized>
