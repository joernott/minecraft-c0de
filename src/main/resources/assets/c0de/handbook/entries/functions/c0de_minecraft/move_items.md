```json
{
  "title": "MOVE_ITEMS",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 14
}
```

Moves items from one inventory to another.

and a tag, `&` and a part of the ID, `-` before a word that must not match, and `|` between alternatives. An array of filters matches items that one of them matches. Without a filter, all items match.

- `from` STRUCT c0de/minecraft/POSITION: the block to take items from
- `to` STRUCT c0de/minecraft/POSITION: the block to put items into
- `count` INTEGER: the most items to move, -1 for all (optional, default 1)
- `id` STRING: the item ID, e.g. minecraft:wheat (optional)
- `filter` STRING or ARRAY STRING: a filter of the items to move instead of `id` (optional)
- result `result` INTEGER: the number of moved items

```c0de
CALL c0de/minecraft/MOVE_ITEMS
  PARAMETER from farmChest ;
  PARAMETER to storage ;
  PARAMETER count -1 ;
  PARAMETER id "minecraft:wheat" ;
  RESULT result moved
```
