```json
{
  "title": "Calling them",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 7
}
```

All these functions are called with CALL, like your own functions. Built-in functions use their plain name. Library functions use their full name, like `c0de/strings/MID`.

```c0de
CALL c0de/strings/MID
  PARAMETER text name ;
  PARAMETER start 2 ;
  PARAMETER length 3 ;
  RESULT result part
```

A function with one result names it `result`. Positions in texts and lists start at 0.
