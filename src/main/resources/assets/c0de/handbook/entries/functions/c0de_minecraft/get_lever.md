```json
{
  "title": "GET_LEVER",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 7
}
```

Checks if a lever is on.

A block that is not a lever raises INVALID\_ARGUMENT. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the lever is
- result `result` BOOLEAN: true if the lever is on

```c0de
CALL c0de/minecraft/GET_LEVER
  PARAMETER position lever ;
  RESULT result isOn
```
