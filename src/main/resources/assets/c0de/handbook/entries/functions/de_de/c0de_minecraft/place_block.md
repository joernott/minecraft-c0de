```json
{
  "title": "PLACE_BLOCK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 16
}
```

Setzt einen Block an eine Position.

An der Position muss Luft oder ein ersetzbarer Block wie Gras oder Wasser sein, sonst wird INVALID\_ARGUMENT ausgelöst. Ohne `inventory` braucht das allowBlockChanges oder einen Administrator als Besitzer, sonst wird PERMISSION\_DENIED ausgelöst. Eine leere `dimension` in einer Position bedeutet die Dimension des Auslöserblocks.

Mit `inventory` wird der Block aus diesem Inventar genommen, so wie ein Spieler einen Block aus seinem Inventar setzt, und das darf jeder Besitzer. Wenn das Inventar den Block nicht enthält, wird ITEM\_MISSING ausgelöst.

Mit `data` bekommt der Block die Daten, die GET\_BLOCK zurückgibt, z. B. die Gegenstände einer Truhe oder den Text eines Schilds. Das braucht immer allowBlockChanges oder einen Administrator, auch mit `inventory`\; die Daten von Befehlsblöcken, Konstruktionsblöcken und Verbundblöcken dürfen nur Administratoren setzen. Daten, die kein SNBT sind, oder ein Block ohne Daten wie Stein lösen INVALID\_ARGUMENT aus, bevor etwas gesetzt wird.

- `position` STRUCT c0de/minecraft/POSITION: wohin der Block kommt
- `id` STRING: die Block-ID, z. B. minecraft:stone
- `properties` HASH STRING: Blockzustände wie facing (optional, Standard leer)
- `inventory` STRUCT c0de/minecraft/POSITION: eine Truhe oder ein anderer Block mit Inventar, aus dem der Block genommen wird (optional)
- `data` STRING: die Daten des Blocks als SNBT (optional, Standard leer)

```c0de
CALL c0de/minecraft/PLACE_BLOCK
  PARAMETER position spot ;
  PARAMETER id "minecraft:torch" ;
  PARAMETER inventory chest ;
```
