```json
{
  "title": "SPAWN_ITEM",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 19
}
```

Spawns items on the ground at a position.

`count` must be 1 to 64. The result is the UUID of the new item entity. This needs allowSpawning or an administrator as owner, otherwise PERMISSION\_DENIED is raised. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the items appear
- `id` STRING: the item ID, e.g. minecraft:bread
- `count` INTEGER: the number of items, 1 to 64
- result `result` STRING: the UUID of the item entity

```c0de
CALL c0de/minecraft/SPAWN_ITEM
  PARAMETER position spot ;
  PARAMETER id "minecraft:bread" ;
  PARAMETER count 3 ;
  RESULT result itemId
```
