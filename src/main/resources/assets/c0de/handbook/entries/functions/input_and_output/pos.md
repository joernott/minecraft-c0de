```json
{
  "title": "POS",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 6
}
```

Returns the column of the cursor.

It answers for the screen of the device bound first. Without a screen, the result is 0.

- result `result` INTEGER: the column of the cursor

```c0de
CALL POS
  RESULT result column
```
