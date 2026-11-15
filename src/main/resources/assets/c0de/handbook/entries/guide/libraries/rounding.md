```json
{
  "title": "Rounding",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 12
}
```

INT rounds down: -12.85 gives -13. FIX just cuts off the decimals: -12.85 gives -12. ROUND goes to the nearest whole number. A .5 goes to the even neighbor, so 12.5 gives 12 and 13.5 gives 14.

```c0de
CALL c0de/math/ROUND
  PARAMETER value 12.5 ;
  RESULT result rounded
```
