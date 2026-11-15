```json
{
  "title": "Changing the world",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 18
}
```

Some functions change the world more:

- DELETE\_ITEMS: remove items from containers
- PLACE\_BLOCK, REMOVE\_BLOCK: place or break blocks
- SPAWN\_ITEM, SPAWN\_ENTITY: create items and mobs
- TELEPORT: move an entity
- COMMAND: run a command like a command block

Positions are given as a `c0de/minecraft/POSITION` struct with x, y, z and an optional dimension. An empty dimension means the dimension of the trigger block. Programs can never change the blocks on the sheets of the c0de dimension.
