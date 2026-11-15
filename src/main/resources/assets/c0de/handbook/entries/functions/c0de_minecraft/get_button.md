```json
{
  "title": "GET_BUTTON",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 9
}
```

Checks if a button is pressed.

A block that is not a button raises INVALID\_ARGUMENT. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the button is
- result `result` BOOLEAN: true if the button is pressed

```c0de
CALL c0de/minecraft/GET_BUTTON
  PARAMETER position button ;
  RESULT result pressed
```
