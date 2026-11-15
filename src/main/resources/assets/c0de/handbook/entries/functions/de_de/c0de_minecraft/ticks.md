```json
{
  "title": "TICKS",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 1
}
```

Gibt die Spielzeit der Welt in Ticks zurück.

Sie zählt alle Ticks, seit die Welt erschaffen wurde, 20 pro Sekunde, solange die Welt läuft, und sie ist in jeder Dimension gleich. Schlafen, `/time set` und die Spielregel doDaylightCycle ändern sie nicht. Sie ist also nicht die Tageszeit.

Es gibt keine Funktion für die Tageszeit. `TICKS \ 24000` ergibt sie nur in einer Welt, in der nie jemand geschlafen oder `/time set` verwendet hat (0 ist Sonnenaufgang, 6000 Mittag, 18000 Mitternacht). Ein Tageslichtsensor unterscheidet Tag und Nacht: GET\_BLOCK liefert sein Signal als Text in `properties["power"]`, nachts 0.

- Ergebnis `result` INTEGER: die Spielzeit in Ticks

```c0de
CALL c0de/minecraft/TICKS
  RESULT result ticks
```
