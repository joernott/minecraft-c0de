```json
{
  "title": "MOVE_ITEMS",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 14
}
```

Verschiebt Gegenstände von einem Inventar in ein anderes.

und ein Tag, `&` und ein Teil der ID, `-` vor einem Wort, das nicht passen darf, und `|` zwischen Alternativen. Ein Array von Filtern trifft Gegenstände, zu denen einer von ihnen passt. Ohne Filter passen alle Gegenstände.

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
