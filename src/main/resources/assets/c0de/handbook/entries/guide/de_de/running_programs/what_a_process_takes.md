```json
{
  "title": "Was ein Prozess nimmt",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 5
}
```

Wenn ein Prozess startet, nimmt er aus dem Eingabeinventar:

- eine Karte für jeden Parameter, für den es eine Karte gibt,
- eine nicht initialisierte Karte für jedes zusätzliche Ergebnis,
- alle anderen initialisierten Variablen- und Konstantenkarten.

Diese anderen Karten werden nicht verwendet. Sie werden unverändert in das Ausgabeinventar weitergereicht, wenn der Prozess endet.

**Rezepte**

<item;c0de:constant_item>

<recipe;c0de:constant_item_uninitialized>
