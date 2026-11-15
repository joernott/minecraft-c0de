```json
{
  "title": "PID-Block",
  "icon": "minecraft:book",
  "category": "running_programs",
  "ordinal": 0
}
```

Der PID-Block markiert einen laufenden Prozess auf der Cursorschiene seines Blatts.

Wenn ein Prozess startet, erscheint ein dunkler PID-Block in Zeile 0 der Cursorschiene. Seine Höhe ist die PID, die Nummer des Prozesses. Seine Nord- und Südseite zeigen die PID in Grün. Seine Ost- und Westseite zeigen, wo der Auslöserblock steht.

Solange ein PID-Block da ist, ist das Blatt gesperrt: Niemand kann darauf Blöcke platzieren oder abbauen. Klicke den PID-Block mit gedrückter Umschalttaste und dem Debugging-Werkzeug mit rechts an, um den Prozess abzubrechen. Das können nur der Besitzer des Auslöserblocks und Operatoren.

Nicht herstellbar.

Ein Programm läuft in einer Endlosschleife, und der Auslöserblock kann es nicht neu starten. Suche seinen PID-Block, klicke ihn mit gedrückter Umschalttaste und dem Debugging-Werkzeug mit rechts an, und der Prozess stoppt. Seine Gegenstände kommen ins Ausgabe-Inventar.

Mehrere Prozesse eines Programms können gleichzeitig laufen, einer pro Auslöserblock. Jeder hat seinen eigenen PID-Block und Cursor.
