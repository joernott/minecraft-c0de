```json
{
  "title": "REMOVE_BLOCK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 17
}
```

Entfernt den Block an einer Position.

Mit `drop` fällt der Block als Gegenstand heraus, wie beim Abbauen. Das braucht allowBlockChanges oder einen Administrator als Besitzer, sonst wird PERMISSION\_DENIED ausgelöst. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo der Block ist
- `drop` BOOLEAN: wahr, damit der Block als Gegenstand herausfällt (optional, Standard TRUE)

```c0de
CALL c0de/minecraft/REMOVE_BLOCK
  PARAMETER position spot ;
  PARAMETER drop FALSE ;
```
