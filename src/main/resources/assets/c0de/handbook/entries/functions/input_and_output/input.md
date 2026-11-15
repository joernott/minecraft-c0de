```json
{
  "title": "INPUT",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 2
}
```

Reads a value into a variable.

`variable` must be the name of one declared variable, without index. A constant or loop variable doesn't work. The function writes into the variable directly. It first looks into the bound I/O blocks for an item with the same name and type, or a literal item of the right type. Otherwise it waits until a player types a line on a bound laptop or tablet. The typed line appears on the screen of that laptop or tablet.

- `variable` variable: the variable that receives the value
- `hide` BOOLEAN: show `*` for what is typed on laptops and tablets (optional, default FALSE)
- no results

```c0de
PROGRAM typing
NOOP
NOOP
FUNCTION ask
BEGIN
  VARIABLE count INTEGER
  CALL INPUT
    PARAMETER variable count ;
  CALL PRINT
    PARAMETER value count * 2 ;
END FUNCTION
```
