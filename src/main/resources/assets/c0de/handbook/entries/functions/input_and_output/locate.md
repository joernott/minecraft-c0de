```json
{
  "title": "LOCATE",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 4
}
```

Moves the cursor of the screens.

The next PRINT writes at this place. A row or column outside the screen moves the cursor to the nearest edge.

- `row` INTEGER: the row
- `column` INTEGER: the column (optional, default 0)

```c0de
CALL LOCATE
  PARAMETER row 3 ;
  PARAMETER column 5 ;
```
