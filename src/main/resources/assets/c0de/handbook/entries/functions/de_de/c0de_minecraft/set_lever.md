```json
{
  "title": "SET_LEVER",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 8
}
```

Schaltet einen Hebel an oder aus.

Ein Block, der kein Hebel ist, löst INVALID\_ARGUMENT aus. Hebel auf den Blättern der c0de-Dimension können nicht verändert werden (PERMISSION\_DENIED). Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo der Hebel ist
- `on` BOOLEAN: wahr zum Anschalten, falsch zum Ausschalten

```c0de
CALL c0de/minecraft/SET_LEVER
  PARAMETER position lever ;
  PARAMETER on TRUE ;
```
