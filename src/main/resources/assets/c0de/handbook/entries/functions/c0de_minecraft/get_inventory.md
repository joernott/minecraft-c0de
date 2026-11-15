```json
{
  "title": "GET_INVENTORY",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 6
}
```

Reads the inventory of a block, such as a chest.

You get one ITEM\_STACK (id, count) per slot. An empty slot has the id "" and the count 0. A block without inventory raises INVALID\_ARGUMENT. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the block is
- result `result` ARRAY STRUCT c0de/minecraft/ITEM\_STACK: the slots

```c0de
CALL c0de/minecraft/GET_INVENTORY
  PARAMETER position chest ;
  RESULT result slots
```
