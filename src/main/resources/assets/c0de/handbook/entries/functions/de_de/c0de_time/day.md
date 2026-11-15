```json
{
  "title": "DAY",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 5
}
```

Gibt den Tag des Monats zurück (1 bis 31).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: der Tag, 1 bis 31

```c0de
CALL c0de/time/DAY
  PARAMETER time birthday ;
  RESULT result day
```
