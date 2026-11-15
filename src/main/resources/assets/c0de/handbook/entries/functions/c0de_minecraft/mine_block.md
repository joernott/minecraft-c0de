```json
{
  "title": "MINE_BLOCK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 18
}
```

Breaks a block like a player, with a tool from an inventory.

It takes the most suitable tool of `tool_inventory`: first a tool that gets the block and is faster (a pickaxe for cobblestone, shears for leaves), then an item that gets the block, then a faster tool, and otherwise bare hands. Of several equally suitable tools it takes the first one, e.g. the iron pickaxe in slot 0 before the diamond pickaxe in slot 2 for cobblestone\; for obsidian it takes the diamond pickaxe.

The tool is damaged as if a player used it, and the program waits as long as a player would need to break the block. The drops go into `output_inventory`\; without it, they fall on the ground. If they don't fit into the output inventory, INVENTORY\_FULL is raised and the block stays. Bedrock and fluids can't be mined (INVALID\_ARGUMENT). Every owner may use MINE\_BLOCK, because it only does what players can do.

On air, MINE\_BLOCK does nothing: no tool is used and the program doesn't wait. The positions are still checked first, so an unloaded chunk (CHUNK\_NOT\_LOADED), a `tool_inventory` without inventory (INVALID\_ARGUMENT) or a block on a sheet (PERMISSION\_DENIED) raise their errors. An empty `dimension` in a position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the block is
- `tool_inventory` STRUCT c0de/minecraft/POSITION: the block whose inventory holds the tools, e.g. a chest
- `output_inventory` STRUCT c0de/minecraft/POSITION: the block whose inventory gets the drops (optional)

```c0de
CALL c0de/minecraft/MINE_BLOCK
  PARAMETER position spot ;
  PARAMETER tool_inventory tools ;
  PARAMETER output_inventory chest ;
```
