```json
{
  "title": "DELETE_ITEMS",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 15
}
```

Entfernt Gegenstände aus einem Inventar.

Die Funktion entfernt bis zu `count` Gegenstände und gibt zurück, wie viele entfernt wurden. Das braucht allowInventoryChanges oder einen Administrator als Besitzer, sonst wird PERMISSION\_DENIED ausgelöst. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: der Block mit dem Inventar
- `id` STRING: die Gegenstands-ID, z. B. minecraft:rotten\_flesh
- `count` INTEGER: die höchste Anzahl an Gegenständen, die entfernt wird
- Ergebnis `result` INTEGER: die Anzahl der entfernten Gegenstände

```c0de
CALL c0de/minecraft/DELETE_ITEMS
  PARAMETER position trashChest ;
  PARAMETER id "minecraft:rotten_flesh" ;
  PARAMETER count 64 ;
  RESULT result removed
```
