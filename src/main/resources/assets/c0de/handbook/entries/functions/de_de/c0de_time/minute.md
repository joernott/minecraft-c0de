```json
{
  "title": "MINUTE",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 3
}
```

Gibt die Minute einer Zeit zurück (0 bis 59).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Minute, 0 bis 59

```c0de
CALL c0de/time/MINUTE
  RESULT result minute
```
