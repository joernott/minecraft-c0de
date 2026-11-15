```json
{
  "title": "Die Welt verändern",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 18
}
```

Manche Funktionen verändern die Welt stärker:

- DELETE\_ITEMS: Gegenstände aus Behältern entfernen
- PLACE\_BLOCK, REMOVE\_BLOCK: Blöcke setzen oder abbauen
- SPAWN\_ITEM, SPAWN\_ENTITY: Gegenstände und Kreaturen erschaffen
- TELEPORT: ein Objekt bewegen
- COMMAND: einen Befehl ausführen wie ein Befehlsblock

Positionen werden als Struktur `c0de/minecraft/POSITION` mit x, y, z und einer optionalen Dimension angegeben. Eine leere Dimension bedeutet die Dimension des Auslöserblocks. Programme können die Blöcke auf den Blättern der c0de-Dimension niemals verändern.
