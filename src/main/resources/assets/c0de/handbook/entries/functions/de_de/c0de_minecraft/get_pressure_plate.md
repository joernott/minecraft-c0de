```json
{
  "title": "GET_PRESSURE_PLATE",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 11
}
```

Liest die Signalstärke einer Druckplatte.

Druckplatten können nur gelesen werden\; Lebewesen und Objekte drücken sie. Ein Block, der keine Druckplatte ist, löst INVALID\_ARGUMENT aus. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo die Druckplatte ist
- Ergebnis `result` INTEGER: die Signalstärke, 0 bis 15

```c0de
CALL c0de/minecraft/GET_PRESSURE_PLATE
  PARAMETER position plate ;
  RESULT result strength
```
