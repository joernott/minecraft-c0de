```json
{
  "title": "PRESS_BUTTON",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 10
}
```

Presses a button like a player would.

A block that is not a button raises INVALID\_ARGUMENT. Buttons on the sheets of the c0de dimension can't be pressed (PERMISSION\_DENIED). An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the button is

```c0de
CALL c0de/minecraft/PRESS_BUTTON
  PARAMETER position button ;
```
