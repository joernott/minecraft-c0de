```json
{
  "title": "Variablenkarte",
  "icon": "c0de:variable_item",
  "category": "items",
  "ordinal": 2
}
```

Eine Variablenkarte trägt einen Namen, einen Typ und einen Wert in einen Prozess hinein oder aus ihm heraus.

Sie hat einen blauen Rand. Halte sie in deiner Haupthand und klicke mit rechts, um ihren Namen, Typ und Wert zu bearbeiten. Der Mülleimer-Knopf leert alle Felder. Wenn du mit allen Feldern leer speicherst, wird sie zu einer nicht initialisierten Karte.

Eine nicht initialisierte Variablenkarte hat keinen Namen, keinen Typ und keinen Wert. Ein Prozess schreibt ein Ergebnis in sie hinein. Du brauchst eine für jedes Ergebnis, an dessen Stelle keine Parameterkarte tritt.

<recipe;c0de:variable_item_boolean>

<recipe;c0de:variable_item_uninitialized>

Eine Datenkarte, eine Typkarte und hellblauer Farbstoff ergeben zwei Variablenkarten\; eine Datenkarte und hellblauer Farbstoff ergeben eine nicht initialisierte Karte.

Diese Funktion verdoppelt eine Zahl. Lege eine INTEGER-Variablenkarte namens `n` mit dem Wert 21 in den Auslöserblock. Nach dem Lauf enthält die Ausgabe eine Karte namens `result` mit dem Wert 42.

```c0de
PROGRAM doubler
NOOP
NOOP
FUNCTION double
  PARAMETER n INTEGER
  RESULT result INTEGER
BEGIN
  ASSIGN result n * 2 ;
END FUNCTION
```
