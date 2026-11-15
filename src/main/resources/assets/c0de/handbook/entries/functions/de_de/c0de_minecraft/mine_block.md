```json
{
  "title": "MINE_BLOCK",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 18
}
```

Baut einen Block ab wie ein Spieler, mit einem Werkzeug aus einem Inventar.

Die Funktion nimmt das passendste Werkzeug aus `tool_inventory`: zuerst ein Werkzeug, das den Block droppen lässt und schneller ist (eine Spitzhacke für Bruchstein, eine Schere für Laub), dann einen Gegenstand, der den Block droppen lässt, dann ein schnelleres Werkzeug und sonst die bloßen Hände. Von mehreren gleich passenden Werkzeugen nimmt sie das erste, z. B. bei Bruchstein die Eisenspitzhacke in Slot 0 vor der Diamantspitzhacke in Slot 2\; bei Obsidian nimmt sie die Diamantspitzhacke.

Das Werkzeug nutzt sich ab, als hätte ein Spieler es benutzt, und das Programm wartet so lange, wie ein Spieler zum Abbauen des Blocks bräuchte. Die Drops kommen in `output_inventory`\; ohne dieses fallen sie auf den Boden. Wenn sie nicht in das Ausgabeinventar passen, wird INVENTORY\_FULL ausgelöst und der Block bleibt stehen. Grundgestein und Flüssigkeiten können nicht abgebaut werden (INVALID\_ARGUMENT). Jeder Besitzer darf MINE\_BLOCK verwenden, weil es nur tut, was Spieler auch können.

Bei Luft tut MINE\_BLOCK nichts: Kein Werkzeug wird benutzt, und das Programm wartet nicht. Die Positionen werden trotzdem zuerst geprüft, also lösen ein nicht geladener Chunk (CHUNK\_NOT\_LOADED), ein `tool_inventory` ohne Inventar (INVALID\_ARGUMENT) oder ein Block auf einem Blatt (PERMISSION\_DENIED) ihre Fehler aus. Eine leere `dimension` in einer Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo der Block ist
- `tool_inventory` STRUCT c0de/minecraft/POSITION: der Block, dessen Inventar die Werkzeuge enthält, z. B. eine Truhe
- `output_inventory` STRUCT c0de/minecraft/POSITION: der Block, dessen Inventar die Drops bekommt (optional)

```c0de
CALL c0de/minecraft/MINE_BLOCK
  PARAMETER position spot ;
  PARAMETER tool_inventory tools ;
  PARAMETER output_inventory chest ;
```
