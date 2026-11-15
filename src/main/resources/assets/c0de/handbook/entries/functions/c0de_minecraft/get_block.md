```json
{
  "title": "GET_BLOCK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 5
}
```

Reads the block at a position.

You get its ID, its block state properties as text, the redstone power it receives, whether it has an inventory, and its data. An unloaded chunk raises CHUNK\_NOT\_LOADED. An empty `dimension` in the position means the dimension of the trigger block.

The data is what a chest, sign or furnace keeps besides its block states: the items, the text, the custom name. It is SNBT, the text of the commands `/data` and `/setblock`, e.g. `{Items:[{Slot:0b,count:12,id:"minecraft:cobblestone"}]}`, without the position. Blocks without such data have `""`. PLACE\_BLOCK can place a block with it.

- `position` STRUCT c0de/minecraft/POSITION: where the block is
- result `id` STRING: the block ID, e.g. minecraft:oak\_door
- result `properties` HASH STRING: the block states, e.g. facing is north
- result `power` INTEGER: the redstone power it receives, 0 to 15
- result `has_inventory` BOOLEAN: true for blocks with an inventory, such as chests\; GET\_INVENTORY, MOVE\_ITEMS and DELETE\_ITEMS work with them
- result `data` STRING: the data of the block as SNBT, or `""`

```c0de
CALL c0de/minecraft/GET_BLOCK
  PARAMETER position spot ;
  RESULT id blockId
  RESULT properties states
  RESULT power power
  RESULT has_inventory isChest
```
