```json
{
  "title": "FIND_ENTITIES",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 21
}
```

Finds all entities near a position.

Items count as entities too. Each ENTITY has uuid, type, name and position. `radius` must be 1 to 64, otherwise INVALID\_ARGUMENT is raised.

It searches a sphere, not a cube: it finds the entities whose feet are at most `radius` blocks away from the middle of the block at `position`, in the dimension of `position`. `type` must be the full ID, like `minecraft:sheep`. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: the center of the search
- `radius` INTEGER: the search radius in blocks, 1 to 64
- `type` STRING: only this entity type (optional, default empty for all types)
- result `result` ARRAY STRUCT c0de/minecraft/ENTITY: the entities found

```c0de
CALL c0de/minecraft/FIND_ENTITIES
  PARAMETER position pen ;
  PARAMETER radius 8 ;
  PARAMETER type "minecraft:sheep" ;
  RESULT result sheep
```
