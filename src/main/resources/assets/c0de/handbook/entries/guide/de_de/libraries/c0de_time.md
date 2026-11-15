```json
{
  "title": "c0de/time",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 9
}
```

Echte Uhrzeit und echtes Datum, in der Zeitzone des Servers:

- NOW: die aktuelle Zeit als TIME-Wert
- YEAR, MONTH, DAY: Teile des Datums
- HOUR, MINUTE, SECOND, MILLISECOND: Teile der Uhrzeit
- WEEKDAY: 1 für Montag bis 7 für Sonntag
- WEEK: die Kalenderwoche
- TIMER: Sekunden seit Mitternacht
- SLEEP: eine Anzahl von Spielticks warten

Die Datums- und Zeitfunktionen benutzen die aktuelle Zeit oder einen TIME-Wert, den du als `time` übergibst. SLEEP wartet zwischen 1 und 72000 Ticks.

TIMER beginnt um Mitternacht bei 0. WEEK liefert die ISO-Woche: Am 1. Januar kann noch Woche 52 oder 53 des Vorjahres sein, und keine Funktion liefert dieses Jahr.
