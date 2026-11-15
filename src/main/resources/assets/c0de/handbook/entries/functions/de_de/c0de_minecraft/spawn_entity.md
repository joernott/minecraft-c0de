```json
{
  "title": "SPAWN_ENTITY",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 20
}
```

Erzeugt ein Objekt, z. B. ein Schwein, an einer Position.

Das Ergebnis ist die UUID des neuen Objekts. Das braucht allowSpawning oder einen Administrator als Besitzer, sonst wird PERMISSION\_DENIED ausgelöst. Ein unbekannter Typ löst INVALID\_ARGUMENT aus. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo das Objekt erscheint
- `type` STRING: der Objekttyp, z. B. minecraft:pig
- Ergebnis `result` STRING: die UUID des Objekts

```c0de
CALL c0de/minecraft/SPAWN_ENTITY
  PARAMETER position pen ;
  PARAMETER type "minecraft:pig" ;
  RESULT result pigId
```
