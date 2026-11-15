```json
{
  "title": "Drei Auslöserblöcke",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 0
}
```

- **Auslöserblock**: startet einen Prozess, wenn er einen Redstone-Impuls bekommt.
- **Zeitgeberblock**: startet einen Prozess immer wieder, in einem Abstand, den du einstellst.
- **Debug-Auslöserblock**: startet einen Prozess, wenn du ihn mit dem Debugging-Werkzeug schlägst.

Nur ein Signal, das angeht, startet einen Auslöserblock. Einen Hebel auszuschalten startet ihn nicht, und ein Signal, das an bleibt, startet ihn nicht noch einmal.

Auslöserblöcke funktionieren in jeder Dimension. In der c0de-Dimension kannst du sie nur auf den schwarzen Boden außerhalb der Blätter setzen.

Ein Redstone-Signal pausiert einen Zeitgeberblock. Verwende einen Hebel, um ihn abzuschalten. Da ein Blatt geschützt ist, solange sein Programm läuft, pausiere einen schnellen Zeitgeber, bevor du das Programm änderst\; sein Dialog zeigt dann „Durch Redstone pausiert“.
