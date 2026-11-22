```json
{
  "title": "Ergebnisse",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 6
}
```

Wenn ein Prozess normal endet, bekommt das Ausgabeinventar eine [Variablenkarte](^tools/variable_items) pro Ergebnis der Einstiegsfunktion. Jede Karte trägt den Namen, den Typ und den Wert ihres Ergebnisses. Ein [EXIT](^language/exit) zählt als normales Ende: Die Ergebnisse bekommen die Werte, die sie haben, wenn EXIT läuft.

Konstantenkarten von Parametern kommen unverändert zurück: Eine Konstante bleibt eine Konstante. Wenn die Funktion weniger Ergebnisse als Parameter mit Variablenkarten hat, bekommst du auch leere Variablenkarten zurück. So ist die Zahl der Karten, die hinein- und herausgehen, gleich.

**Rezepte**

<item;c0de:variable_item>

<recipe;c0de:variable_item_uninitialized>

<item;c0de:exit_block>

<recipe;c0de:exit_block>

<item;c0de:constant_item>

<recipe;c0de:constant_item_uninitialized>
