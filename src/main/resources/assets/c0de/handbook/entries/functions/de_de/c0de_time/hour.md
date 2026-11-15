```json
{
  "title": "HOUR",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 4
}
```

Gibt die Stunde einer Zeit zurück (0 bis 23).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Stunde, 0 bis 23

```c0de
CALL c0de/time/HOUR
  RESULT result hour
```
