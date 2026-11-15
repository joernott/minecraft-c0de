```json
{
  "title": "GET_PRESSURE_PLATE",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 11
}
```

Reads the signal strength of a pressure plate.

Pressure plates can only be read\; entities press them. A block that is not a pressure plate raises INVALID\_ARGUMENT. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the pressure plate is
- result `result` INTEGER: the signal strength, 0 to 15

```c0de
CALL c0de/minecraft/GET_PRESSURE_PLATE
  PARAMETER position plate ;
  RESULT result strength
```
