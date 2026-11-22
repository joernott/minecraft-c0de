```json
{
  "title": "Nicht initialisierte Karten",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 4
}
```

Eine **nicht initialisierte** [Variablenkarte](^tools/variable_items) hat keinen Namen, keinen Typ und keinen Wert. Ein Prozess schreibt ein Ergebnis hinein.

Du brauchst eine für jedes Ergebnis, das keine Variablenkarte eines Parameters hat, die seinen Platz einnimmt. Eine Funktion mit einem Ergebnis und ohne Parameter braucht eine nicht initialisierte Karte. [Konstantenkarten](^tools/constant_items) nehmen nicht den Platz eines Ergebnisses ein: Sie bleiben Konstanten.

Übrige nicht initialisierte Karten bleiben im Eingabeinventar. So kannst du dort mehrere davon für viele Durchläufe stapeln.

**Rezepte**

<item;c0de:variable_item>

<recipe;c0de:variable_item_uninitialized>

<item;c0de:constant_item>

<recipe;c0de:constant_item_uninitialized>
