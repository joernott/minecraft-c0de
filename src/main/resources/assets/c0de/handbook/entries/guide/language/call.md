```json
{
  "title": "CALL",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 12
}
```

CALL runs a function. PARAMETER lines give it values, RESULT lines say which variable gets which result. Results you don't list are thrown away.

```c0de
CALL greet
  PARAMETER name "c0de" ;
  RESULT result greeting
```

Here `greeting` becomes "Hello, c0de\!". A function can only call functions that are defined above it, or itself.

The same variable may give a parameter and receive a result, like `PARAMETER name greeting ;` and `RESULT result greeting`.
