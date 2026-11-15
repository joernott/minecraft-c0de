```json
{
  "title": "Changing values",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 8
}
```

Functions get copies of their parameters. A function that changes a text, list or hash gives back the changed copy as `result`.

To change a variable, use it both as parameter and as result target:

```c0de
CALL INSERT
  PARAMETER array list ;
  PARAMETER index 0 ;
  PARAMETER value 0 ;
  RESULT result list
```
