```json
{
  "title": "TELEPORT",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 22
}
```

Teleports an entity or item to a position.

This needs allowTeleporting or an administrator as owner. Only administrators can teleport players. Both raise PERMISSION\_DENIED otherwise. An entity that is not loaded raises INVALID\_ARGUMENT.

The entity may be in any dimension, and `position` may be in another one: it goes there. The chunk of `position` must be loaded, otherwise CHUNK\_NOT\_LOADED is raised and nothing happens. The entity lands in the middle of the block, at its bottom. An empty `dimension` in the position means the dimension of the trigger block.

- `uuid` STRING: the UUID of the entity
- `position` STRUCT c0de/minecraft/POSITION: the destination

```c0de
CALL c0de/minecraft/TELEPORT
  PARAMETER uuid pigId ;
  PARAMETER position barn ;
```
