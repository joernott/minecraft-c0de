```json
{
  "title": "SET_LEVER",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 8
}
```

Switches a lever on or off.

A block that is not a lever raises INVALID\_ARGUMENT. Levers on the sheets of the c0de dimension can't be changed (PERMISSION\_DENIED). An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the lever is
- `on` BOOLEAN: true to switch it on, false to switch it off

```c0de
CALL c0de/minecraft/SET_LEVER
  PARAMETER position lever ;
  PARAMETER on TRUE ;
```
