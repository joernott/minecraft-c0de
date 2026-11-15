```json
{
  "title": "MILLISECOND",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 1
}
```

Gibt die Millisekunden einer Zeit zurück (0 bis 999).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Millisekunde, 0 bis 999

```c0de
CALL c0de/time/MILLISECOND
  PARAMETER time start ;
  RESULT result millis
```
