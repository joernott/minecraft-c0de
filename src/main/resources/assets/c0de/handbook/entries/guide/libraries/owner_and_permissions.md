```json
{
  "title": "Owner and permissions",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 19
}
```

Programs act on behalf of the owner of the trigger block. The world-changing functions above only work if the owner is an operator, or if the server allows them in the file `config/c0de.properties`.

Otherwise they raise PERMISSION\_DENIED. Players can only be teleported if the owner is an operator, and only operators can run commands with COMMAND or give command blocks their data.

Three functions work for every owner, because they only do what a player can do:

- MOVE\_ITEMS moves items between two chests the owner could open. The items travel, one stack per tick, and a filter like `$logs` or `minecraft:wheat` picks them.
- MINE\_BLOCK breaks a block with the best tool from a chest (`tool_inventory`). The tool wears out and it takes as long as for a player. The drops go into another chest (`output_inventory`), or fall on the ground. If that chest is full, it raises INVENTORY\_FULL.
- PLACE\_BLOCK with the parameter `inventory` takes the block from a chest. If the chest doesn't contain it, it raises ITEM\_MISSING.

**Recipes**

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>
