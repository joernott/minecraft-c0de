```json
{
  "title": "YEAR",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 7
}
```

Gibt das Jahr einer Zeit zurück, z. B. 2026.

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: das Jahr

```c0de
CALL c0de/time/YEAR
  RESULT result year
```
