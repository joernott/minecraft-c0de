```json
{
  "title": "NOW",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 0
}
```

Gibt die aktuelle Zeit zurück.

Der TIME-Wert ist ein Unix-Zeitstempel in Millisekunden. Nimm FORMAT, um ihn in Text umzuwandeln.

- Ergebnis `result` TIME: die aktuelle Zeit

```c0de
CALL c0de/time/NOW
  RESULT result now
```
