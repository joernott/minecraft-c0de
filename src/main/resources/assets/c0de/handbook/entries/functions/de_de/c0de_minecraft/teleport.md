```json
{
  "title": "TELEPORT",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 22
}
```

Teleportiert ein Objekt oder einen Gegenstand an eine Position.

Das braucht allowTeleporting oder einen Administrator als Besitzer. Nur Administratoren können Spieler teleportieren. Beides löst sonst PERMISSION\_DENIED aus. Ein Objekt, das nicht geladen ist, löst INVALID\_ARGUMENT aus.

Das Objekt darf in jeder Dimension sein, und `position` darf in einer anderen liegen: Es kommt dorthin. Der Chunk von `position` muss geladen sein, sonst wird CHUNK\_NOT\_LOADED ausgelöst und nichts passiert. Das Objekt landet in der Mitte des Blocks, auf seinem Boden. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `uuid` STRING: die UUID des Objekts
- `position` STRUCT c0de/minecraft/POSITION: das Ziel

```c0de
CALL c0de/minecraft/TELEPORT
  PARAMETER uuid pigId ;
  PARAMETER position barn ;
```
