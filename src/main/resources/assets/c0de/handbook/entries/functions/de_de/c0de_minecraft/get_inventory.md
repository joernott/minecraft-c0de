```json
{
  "title": "GET_INVENTORY",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 6
}
```

Liest das Inventar eines Blocks, z. B. einer Truhe.

Du bekommst einen ITEM\_STACK (id, count) pro Slot. Ein leerer Slot hat die id "" und die Anzahl 0. Ein Block ohne Inventar löst INVALID\_ARGUMENT aus. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo der Block ist
- Ergebnis `result` ARRAY STRUCT c0de/minecraft/ITEM\_STACK: die Slots

```c0de
CALL c0de/minecraft/GET_INVENTORY
  PARAMETER position chest ;
  RESULT result slots
```
