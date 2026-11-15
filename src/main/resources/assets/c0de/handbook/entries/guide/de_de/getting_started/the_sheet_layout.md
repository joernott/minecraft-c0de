```json
{
  "title": "Der Aufbau eines Blatts",
  "icon": "c0de:wrench",
  "category": "getting_started",
  "ordinal": 3
}
```

Ein Programm beginnt am Nordende eines Blatts und verläuft nach Süden. Jede Reihe von Blöcken quer über das Blatt ist eine **Zeile**. Die erste Zeile ist Zeile 0.

Innerhalb eines Blatts zählt x vom Westrand aus. Sprachblöcke kommen zwischen x=5 und x=28, also hat eine Zeile höchstens 24 Blöcke. So bleiben Zeilen kurz und gut lesbar.

Bei x=4 liegt eine graue Schiene: die **Cursorschiene**. Wenn ein Programm läuft, bewegt sich ein leuchtender Cursor darauf entlang und zeigt, welche Zeile gerade arbeitet.
