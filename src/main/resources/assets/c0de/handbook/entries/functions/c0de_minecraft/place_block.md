```json
{
  "title": "PLACE_BLOCK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 16
}
```

Places a block at a position.

The position must hold air or a replaceable block like grass or water, otherwise INVALID\_ARGUMENT is raised. Without `inventory`, this needs allowBlockChanges or an administrator as owner, otherwise PERMISSION\_DENIED is raised. An empty `dimension` in a position means the dimension of the trigger block.

With `inventory`, the block is taken from that inventory, like a player places a block from theirs, and every owner may do that. If the inventory doesn't contain the block, ITEM\_MISSING is raised.

With `data`, the block gets the data that GET\_BLOCK returns, e.g. the items of a chest or the text of a sign. This always needs allowBlockChanges or an administrator, also with `inventory`\; the data of command blocks, structure blocks and jigsaw blocks only administrators may set. Data that is not SNBT, or a block without data like stone, raise INVALID\_ARGUMENT before anything is placed.

- `position` STRUCT c0de/minecraft/POSITION: where the block goes
- `id` STRING: the block ID, e.g. minecraft:stone
- `properties` HASH STRING: block states such as facing (optional, default empty)
- `inventory` STRUCT c0de/minecraft/POSITION: a chest or another block with an inventory that the block is taken from (optional)
- `data` STRING: the data of the block as SNBT (optional, default empty)

```c0de
CALL c0de/minecraft/PLACE_BLOCK
  PARAMETER position spot ;
  PARAMETER id "minecraft:torch" ;
  PARAMETER inventory chest ;
```
