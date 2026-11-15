```json
{
  "title": "WEEK",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 9
}
```

Gibt die Kalenderwoche nach ISO 8601 zurück (1 bis 53).

Woche 1 enthält den ersten Donnerstag des Jahres. Deshalb können Tage um Neujahr zur Woche 52 oder 53 des alten Jahres oder zur Woche 1 des neuen Jahres gehören. Ohne `time` wird die aktuelle Zeit genommen. Die Funktion verwendet die Zeitzone des Servers.

Es gibt keine Funktion für das Jahr, zu dem eine Woche gehört. Am 1. Januar 2027 ergibt WEEK 53, die letzte Woche von 2026, aber YEAR ergibt 2027. Ist WEEK im Januar also 52 oder 53, gehört die Woche zum Jahr davor\; ist sie im Dezember 1, zum Jahr danach.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Woche, 1 bis 53

```c0de
CALL c0de/time/WEEK
  RESULT result week
```
