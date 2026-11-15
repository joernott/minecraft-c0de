```json
{
  "title": "LOCATE",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 4
}
```

Bewegt den Cursor der Bildschirme.

Das nächste PRINT schreibt an diese Stelle. Eine Zeile oder Spalte außerhalb des Bildschirms setzt den Cursor an den nächsten Rand.

- `row` INTEGER: die Zeile
- `column` INTEGER: die Spalte (optional, Standard 0)

```c0de
CALL LOCATE
  PARAMETER row 3 ;
  PARAMETER column 5 ;
```
