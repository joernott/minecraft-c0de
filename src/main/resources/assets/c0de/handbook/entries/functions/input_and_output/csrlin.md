```json
{
  "title": "CSRLIN",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 5
}
```

Returns the row of the cursor.

It answers for the screen of the device bound first. Without a screen, the result is 0.

- result `result` INTEGER: the row of the cursor

```c0de
CALL CSRLIN
  RESULT result row
```
