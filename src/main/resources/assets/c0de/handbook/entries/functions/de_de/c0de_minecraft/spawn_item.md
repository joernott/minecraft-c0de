```json
{
  "title": "SPAWN_ITEM",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 19
}
```

Erzeugt Gegenstände auf dem Boden an einer Position.

`count` muss 1 bis 64 sein. Das Ergebnis ist die UUID des neuen Gegenstandsobjekts. Das braucht allowSpawning oder einen Administrator als Besitzer, sonst wird PERMISSION\_DENIED ausgelöst. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo die Gegenstände erscheinen
- `id` STRING: die Gegenstands-ID, z. B. minecraft:bread
- `count` INTEGER: die Anzahl der Gegenstände, 1 bis 64
- Ergebnis `result` STRING: die UUID des Gegenstandsobjekts

```c0de
CALL c0de/minecraft/SPAWN_ITEM
  PARAMETER position spot ;
  PARAMETER id "minecraft:bread" ;
  PARAMETER count 3 ;
  RESULT result itemId
```
