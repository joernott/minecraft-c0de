```json
{
  "title": "Structs",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 6
}
```

A STRUCT declares your own type. Each line in its code block is an element: a name and a type.

```c0de
STRUCT point
BEGIN
  x INTEGER
  y INTEGER
END STRUCT
```

A variable of this type is declared as `VARIABLE p STRUCT point`. Its elements are reached with an index, like `p.x`.

Every program also has a few constants it doesn't declare. `SHEET` describes its sheet, e.g. `SHEET.number` and `SHEET.owner`. `PID` describes the running process, e.g. `PID.pid`, `PID.activator` (the player who started it, empty for other redstone and timer blocks) and `PID.x`, `PID.y`, `PID.z` (where its trigger block is). `CURSOR` describes the cursor right now: `CURSOR.line`, `CURSOR.function`, `CURSOR.depth` and how much of its stack is used, `CURSOR.stack_used` of `CURSOR.stack_size`. `C0DE_VERSION` holds the version of c0de, e.g. "0.4.0". The names of the errors and of the colors are constants too.
