```json
{
  "title": "RETURN",
  "icon": "c0de:return_block",
  "category": "functions",
  "ordinal": 3
}
```

RETURN ends the function right away.

It may name a variable of type ERROR to report a problem to the caller. Without a name, the function returns "no error". The results keep the values they have at that moment.

The code of an error you made yourself must be 1000 or higher, otherwise RETURN raises INVALID\_ARGUMENT. An error your function got from a CALL can be passed on with its code.

<recipe;c0de:return_block>

Put a spruce door in the middle and eight NOOP blocks around it. You get eight RETURN blocks.

```c0de
FUNCTION fail
BEGIN
  VARIABLE e ERROR
  ASSIGN e.code 1000 ;
  RETURN e
END FUNCTION
```
