```json
{
  "title": "FIND_ENTITIES",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 21
}
```

Findet alle Objekte in der Nähe einer Position.

Gegenstände zählen auch als Objekte. Jedes ENTITY hat uuid, type, name und position. `radius` muss 1 bis 64 sein, sonst wird INVALID\_ARGUMENT ausgelöst.

Sie sucht in einer Kugel, nicht in einem Würfel: Sie findet die Objekte, deren Füße höchstens `radius` Blöcke von der Mitte des Blocks bei `position` entfernt sind, in der Dimension von `position`. `type` muss die volle ID sein, wie `minecraft:sheep`. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: die Mitte der Suche
- `radius` INTEGER: der Suchradius in Blöcken, 1 bis 64
- `type` STRING: nur dieser Objekttyp (optional, Standard leer für alle Typen)
- Ergebnis `result` ARRAY STRUCT c0de/minecraft/ENTITY: die gefundenen Objekte

```c0de
CALL c0de/minecraft/FIND_ENTITIES
  PARAMETER position pen ;
  PARAMETER radius 8 ;
  PARAMETER type "minecraft:sheep" ;
  RESULT result sheep
```
