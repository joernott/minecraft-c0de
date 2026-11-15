```json
{
  "title": "MOVE_ITEMS",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 14
}
```

Verschiebt Gegenstände von einem Inventar in ein anderes.

Die Funktion verschiebt bis zu `count` Gegenstände mit der ID `id` oder solche, die zu `filter` passen, und gibt zurück, wie viele verschoben wurden. `count` -1 verschiebt alle. Gib `id` oder `filter` an, nicht beide (INVALID\_ARGUMENT)\; ohne beide werden alle Gegenstände verschoben. Jeder Besitzer darf sie verwenden, wie wenn er die Gegenstände von Hand trägt, aber der Besitzer muss beide Inventare öffnen können: Sie dürfen nicht verschlossen sein, nicht im Spawnschutz liegen und nicht auf den Blättern der c0de-Dimension stehen. Sonst wird PERMISSION\_DENIED ausgelöst. Beide Blöcke müssen in derselben Dimension sein, sonst wird INVALID\_ARGUMENT ausgelöst, wie bei Blöcken ohne Inventar. Eine leere `dimension` in einer Position bedeutet die Dimension des Auslöserblocks.

Die Gegenstände sind unterwegs: ein Stapel pro Tick für je 128 Blöcke Entfernung, der Cursor wartet bei nahen Truhen also einen Tick pro Stapel. Der Server kann die Geschwindigkeit ändern und die Funktion in `config/c0de.properties` abschalten.

Der Filter ist eine Gegenstands-ID wie `minecraft:wheat` oder eine Suche wie in JEI: Wörter, die der Name enthalten muss, `@` und eine Mod, `$` und ein Tag, `&` und ein Teil der ID, `-` vor einem Wort, das nicht passen darf, und `|` zwischen Alternativen. Ein Array von Filtern trifft Gegenstände, zu denen einer von ihnen passt. Ohne Filter passen alle Gegenstände.

- `from` STRUCT c0de/minecraft/POSITION: der Block, aus dem Gegenstände genommen werden
- `to` STRUCT c0de/minecraft/POSITION: der Block, in den Gegenstände gelegt werden
- `count` INTEGER: die höchste Anzahl an Gegenständen, die verschoben wird, -1 für alle (optional, Standard 1)
- `id` STRING: die Gegenstands-ID, z. B. minecraft:wheat (optional)
- `filter` STRING oder ARRAY STRING: ein Filter der Gegenstände, die verschoben werden, statt `id` (optional)
- Ergebnis `result` INTEGER: die Anzahl der verschobenen Gegenstände

```c0de
CALL c0de/minecraft/MOVE_ITEMS
  PARAMETER from farmChest ;
  PARAMETER to storage ;
  PARAMETER count -1 ;
  PARAMETER id "minecraft:wheat" ;
  RESULT result moved
```
