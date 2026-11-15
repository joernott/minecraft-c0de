```json
{
  "title": "SPAWN_ENTITY",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 20
}
```

Spawns an entity, such as a pig, at a position.

The result is the UUID of the new entity. This needs allowSpawning or an administrator as owner, otherwise PERMISSION\_DENIED is raised. An unknown type raises INVALID\_ARGUMENT. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the entity appears
- `type` STRING: the entity type, e.g. minecraft:pig
- result `result` STRING: the UUID of the entity

```c0de
CALL c0de/minecraft/SPAWN_ENTITY
  PARAMETER position pen ;
  PARAMETER type "minecraft:pig" ;
  RESULT result pigId
```
