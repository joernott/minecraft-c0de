```json
{
  "title": "DELETE_ITEMS",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 15
}
```

Removes items from an inventory.

It removes up to `count` items and returns how many were removed. This needs allowInventoryChanges or an administrator as owner, otherwise PERMISSION\_DENIED is raised. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: the block with the inventory
- `id` STRING: the item ID, e.g. minecraft:rotten\_flesh
- `count` INTEGER: the most items to remove
- result `result` INTEGER: the number of removed items

```c0de
CALL c0de/minecraft/DELETE_ITEMS
  PARAMETER position trashChest ;
  PARAMETER id "minecraft:rotten_flesh" ;
  PARAMETER count 64 ;
  RESULT result removed
```
