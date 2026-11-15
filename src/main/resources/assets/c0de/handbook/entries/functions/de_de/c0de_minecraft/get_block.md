```json
{
  "title": "GET_BLOCK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 5
}
```

Liest den Block an einer Position.

Du bekommst seine ID, seine Blockzustände als Text, die Redstone-Stärke, die er empfängt, ob er ein Inventar hat, und seine Daten. Ein nicht geladener Chunk löst CHUNK\_NOT\_LOADED aus. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

Die Daten sind, was eine Truhe, ein Schild oder ein Ofen neben seinen Blockzuständen speichert: die Gegenstände, den Text, den eigenen Namen. Sie sind SNBT, der Text der Befehle `/data` und `/setblock`, z. B. `{Items:[{Slot:0b,count:12,id:"minecraft:cobblestone"}]}`, ohne die Position. Blöcke ohne solche Daten haben `""`. PLACE\_BLOCK kann einen Block mit ihnen setzen.

- `position` STRUCT c0de/minecraft/POSITION: wo der Block ist
- Ergebnis `id` STRING: die Block-ID, z. B. minecraft:oak\_door
- Ergebnis `properties` HASH STRING: die Blockzustände, z. B. facing ist north
- Ergebnis `power` INTEGER: die Redstone-Stärke, die er empfängt, 0 bis 15
- Ergebnis `has_inventory` BOOLEAN: wahr für Blöcke mit Inventar, z. B. Truhen\; GET\_INVENTORY, MOVE\_ITEMS und DELETE\_ITEMS funktionieren mit ihnen
- Ergebnis `data` STRING: die Daten des Blocks als SNBT oder `""`

```c0de
CALL c0de/minecraft/GET_BLOCK
  PARAMETER position spot ;
  RESULT id blockId
  RESULT properties states
  RESULT power power
  RESULT has_inventory isChest
```
