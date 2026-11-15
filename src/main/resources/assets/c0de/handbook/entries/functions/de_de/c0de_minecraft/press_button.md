```json
{
  "title": "PRESS_BUTTON",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 10
}
```

Drückt einen Knopf, wie es ein Spieler tun würde.

Ein Block, der kein Knopf ist, löst INVALID\_ARGUMENT aus. Knöpfe auf den Blättern der c0de-Dimension können nicht gedrückt werden (PERMISSION\_DENIED). Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo der Knopf ist

```c0de
CALL c0de/minecraft/PRESS_BUTTON
  PARAMETER position button ;
```
