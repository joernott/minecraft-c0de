```json
{
  "title": "REMOVE_BLOCK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 17
}
```

Removes the block at a position.

With `drop`, the block drops as an item like when it is mined. This needs allowBlockChanges or an administrator as owner, otherwise PERMISSION\_DENIED is raised. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the block is
- `drop` BOOLEAN: true to drop the block as an item (optional, default TRUE)

```c0de
CALL c0de/minecraft/REMOVE_BLOCK
  PARAMETER position spot ;
  PARAMETER drop FALSE ;
```
