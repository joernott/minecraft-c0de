```json
{
  "title": "POSITION, ENTITY and ITEM_STACK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 0
}
```

The three struct types of the library. Declare variables of them like of any struct, e.g. `VARIABLE spot STRUCT c0de/minecraft/POSITION`.

**POSITION** is a block: `x`, `y` and `z` are INTEGERs, `dimension` is a STRING. In every function, an empty `dimension` means the dimension of the trigger block. Otherwise it holds an ID like `minecraft:the_nether`\; an unknown ID raises INVALID\_ARGUMENT.

**ENTITY** is an entity found by FIND\_ENTITIES: `uuid` (STRING, for TELEPORT), `type` (STRING, like `minecraft:sheep`), `name` (STRING) and `position` (POSITION). The name is the name of a player, the name from a name tag, or else the name of the type.

The `position` of an ENTITY is the block the entity is in: its coordinates are rounded down, so x = -0.5 is block -1. Its `dimension` is empty in the dimension of the trigger block. Compare `type`, not `name`: a name can be in the language of the game.

**ITEM\_STACK** is one slot of an inventory, from GET\_INVENTORY: `id` (STRING, like `minecraft:wheat`) and `count` (INTEGER). An empty slot has the id "" and the count 0.

```c0de
VARIABLE spot STRUCT c0de/minecraft/POSITION
BEGIN
  x 10
  y 64
  z -20
END VARIABLE
```

`dimension` is left out, so it stays empty: the dimension of the trigger block.
