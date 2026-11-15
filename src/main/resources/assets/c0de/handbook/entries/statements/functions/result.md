```json
{
  "title": "RESULT",
  "icon": "c0de:result_block",
  "category": "functions",
  "ordinal": 2
}
```

A RESULT line is an output of a function.

Below a FUNCTION, it has a name and a type. It starts with the default value of its type. When the function ends, its last value goes back to the caller. Below a CALL, it names a result and the variable that receives it.

RESULT lines come after the PARAMETER lines. Results the caller doesn't list are thrown away.

<recipe;c0de:result_block>

Turn one PARAMETER block into a RESULT block.

```c0de
CALL c0de/math/ROUND
  PARAMETER value 12.5 ;
  RESULT result rounded
```

The result `result` of ROUND goes into the variable `rounded`.
