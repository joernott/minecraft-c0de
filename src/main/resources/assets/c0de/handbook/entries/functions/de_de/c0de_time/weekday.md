```json
{
  "title": "WEEKDAY",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 8
}
```

Gibt den Wochentag zurück, von 1 (Montag) bis 7 (Sonntag).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: der Wochentag, 1 bis 7

```c0de
CALL c0de/time/WEEKDAY
  PARAMETER time t ;
  RESULT result weekday
```
