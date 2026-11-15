```json
{
  "title": "POSITION, ENTITY und ITEM_STACK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 0
}
```

Die drei Strukturtypen der Bibliothek. Du deklarierst Variablen von ihnen wie bei jeder Struktur, z. B. `VARIABLE spot STRUCT c0de/minecraft/POSITION`.

**POSITION** ist ein Block: `x`, `y` und `z` sind INTEGERs, `dimension` ist ein STRING. In jeder Funktion bedeutet eine leere `dimension` die Dimension des Auslöserblocks. Sonst enthält sie eine ID wie `minecraft:the_nether`\; eine unbekannte ID löst INVALID\_ARGUMENT aus.

**ENTITY** ist ein Objekt, das FIND\_ENTITIES gefunden hat: `uuid` (STRING, für TELEPORT), `type` (STRING, wie `minecraft:sheep`), `name` (STRING) und `position` (POSITION). Der Name ist der Name eines Spielers, der Name von einem Namensschild oder sonst der Name des Typs.

Die `position` eines ENTITY ist der Block, in dem das Objekt ist: Seine Koordinaten werden abgerundet, x = -0.5 ist also Block -1. Ihre `dimension` ist in der Dimension des Auslöserblocks leer. Vergleiche `type`, nicht `name`: Ein Name kann in der Sprache des Spiels sein.

**ITEM\_STACK** ist ein Slot eines Inventars, von GET\_INVENTORY: `id` (STRING, wie `minecraft:wheat`) und `count` (INTEGER). Ein leerer Slot hat die id "" und die Anzahl 0.

```c0de
VARIABLE spot STRUCT c0de/minecraft/POSITION
BEGIN
  x 10
  y 64
  z -20
END VARIABLE
```

`dimension` fehlt, sie bleibt also leer: die Dimension des Auslöserblocks.
