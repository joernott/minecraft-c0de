```json
{
  "title": "Der Auslöser-Dialog",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 1
}
```

Klicke einen Auslöserblock mit leerer Hand mit rechts an, um seinen Dialog zu öffnen. Er zeigt:

- die PID des laufenden Prozesses
- den Exit-Code des letzten Prozesses und seinen Fehler, falls er abgestürzt ist
- eine Liste zur Auswahl des Programms
- eine Liste zur Auswahl der auszuführenden Funktion
- bei Zeitgeberblöcken den Abstand in Ticks

Der Dialog hat außerdem ein Eingabeinventar, ein Ausgabeinventar und einen Platz für Tarnung. Lege einen beliebigen Block in diesen Platz, und der Auslöserblock nimmt sein Aussehen an.

Die Programm- und die Funktionsliste sind gesperrt, solange ein Prozess läuft.
