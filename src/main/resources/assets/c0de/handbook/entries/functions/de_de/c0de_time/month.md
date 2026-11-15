```json
{
  "title": "MONTH",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 6
}
```

Gibt den Monat einer Zeit zurück (1 bis 12).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: der Monat, 1 bis 12

```c0de
CALL c0de/time/MONTH
  PARAMETER time birthday ;
  RESULT result month
```
