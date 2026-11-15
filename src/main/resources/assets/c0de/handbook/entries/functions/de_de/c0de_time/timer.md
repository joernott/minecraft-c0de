```json
{
  "title": "TIMER",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 10
}
```

Gibt die Sekunden seit Mitternacht zurück.

Das ist die echte Uhrzeit des Servers, nicht die Spielzeit. Nimm TICKS aus c0de/minecraft für die Spielzeit.

TIMER beginnt um Mitternacht in der Zeitzone des Servers bei 0.0 und zählt bis unter 86400, mit den Millisekunden als Nachkommastellen. An den Tagen, an denen die Uhren auf Sommerzeit oder zurück gestellt werden, folgt er der Uhr.

- Ergebnis `result` FLOAT: die Sekunden seit Mitternacht

```c0de
CALL c0de/time/TIMER
  RESULT result seconds
```
