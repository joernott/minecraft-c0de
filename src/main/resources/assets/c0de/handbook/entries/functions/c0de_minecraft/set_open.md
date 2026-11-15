```json
{
  "title": "SET_OPEN",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 13
}
```

Opens or closes a door, trapdoor or fence gate.

Any other block raises INVALID\_ARGUMENT. Iron doors and iron trapdoors need the permission allowBlockChanges or an administrator as owner, otherwise PERMISSION\_DENIED is raised. An empty `dimension` in the position means the dimension of the trigger block.

- `position` STRUCT c0de/minecraft/POSITION: where the door is
- `open` BOOLEAN: true to open, false to close

```c0de
CALL c0de/minecraft/SET_OPEN
  PARAMETER position door ;
  PARAMETER open TRUE ;
```
