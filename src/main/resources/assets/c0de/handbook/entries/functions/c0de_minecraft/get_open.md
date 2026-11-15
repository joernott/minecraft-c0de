```json
{
  "title": "GET_OPEN",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 12
}
```

Checks if a door, trapdoor or fence gate is open.

Any other block raises INVALID\_ARGUMENT. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the door is
- result `result` BOOLEAN: true if it is open

```c0de
CALL c0de/minecraft/GET_OPEN
  PARAMETER position door ;
  RESULT result isOpen
```
