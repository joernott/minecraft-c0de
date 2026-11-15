```json
{
  "title": "PRINT",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 0
}
```

Writes a value on the screens and starts a new line.

The text appears on every display, laptop and tablet bound to the program and function. Strings are printed without quotes, BOOLEAN values as `TRUE` and `FALSE`. Arrays, hashes and structs are printed as JSON. To print several values on one line, join them with ⊕. Without a bound screen, PRINT does nothing.

- `value` any type: the value to print

```c0de
CALL PRINT
  PARAMETER value "Hello" ;
```
