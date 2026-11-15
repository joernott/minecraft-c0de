```json
{
  "title": "Your own errors",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 21
}
```

You can raise your own errors. Their codes must be 1000 or higher. Set the code of an ERROR variable and return it, or use the built-in function RAISE.

```c0de
FUNCTION fail
BEGIN
  VARIABLE e ERROR
  ASSIGN e.code 1000 ;
  RETURN e
END FUNCTION
```
