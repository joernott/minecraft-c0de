```json
{
  "title": "SECOND",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 2
}
```

Gibt die Sekunde einer Zeit zurück (0 bis 59).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Sekunde, 0 bis 59

```c0de
CALL c0de/time/SECOND
  PARAMETER time start ;
  RESULT result second
```
