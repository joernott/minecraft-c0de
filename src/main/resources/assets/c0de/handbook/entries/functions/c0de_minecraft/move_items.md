```json
{
  "title": "MOVE_ITEMS",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 14
}
```

Moves items from one inventory to another.

It moves up to `count` items with the ID `id`, or that match `filter`, and returns how many were moved. `count` -1 moves all of them. Give `id` or `filter`, not both (INVALID\_ARGUMENT)\; without both, all items are moved. Every owner may use it, like carrying the items by hand, but the owner must be able to open both inventories: they may not be locked, in the spawn protection or on the sheets of the c0de dimension. Otherwise PERMISSION\_DENIED is raised. Both blocks must be in the same dimension, otherwise INVALID\_ARGUMENT is raised, as for blocks without inventory. An empty `dimension` in a position means the dimension of the trigger block.

The items travel: one stack per tick for each 128 blocks of distance, so the cursor waits a tick per stack for nearby chests. The server can change the speed and switch the function off in `config/c0de.properties`.

The filter is an item ID like `minecraft:wheat`, or a search like in JEI: words that the name must contain, `@` and a mod, `$` and a tag, `&` and a part of the ID, `-` before a word that must not match, and `|` between alternatives. An array of filters matches items that one of them matches. Without a filter, all items match.

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
