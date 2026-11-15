```json
{
  "title": "Exit-Codes",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 8
}
```

Jeder Prozess endet mit einem **Exit-Code** von 0 bis 15. Er ist 0, wenn das Programm normal endet, die Zahl aus einer EXIT-Anweisung oder 15, wenn der Prozess abgestürzt ist.

Ein Komparator, der den Auslöserblock ausliest, gibt ein Signal aus, das so stark ist wie der Exit-Code. Das Signal bleibt, bis der nächste Prozess startet, und ist 0, solange ein Prozess läuft. So kann deine Redstone-Schaltung auf das Ergebnis eines Programms reagieren. Ein Komparator liest Zeitgeberblöcke und Debug-Auslöserblöcke genauso aus.
