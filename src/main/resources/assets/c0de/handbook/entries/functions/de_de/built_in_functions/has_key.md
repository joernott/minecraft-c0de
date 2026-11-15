```json
{
  "title": "HAS_KEY",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 1
}
```

Prüft, ob ein Hash einen Schlüssel enthält.

- `hash` HASH: der Hash, in dem gesucht wird
- `key` STRING: der gesuchte Schlüssel
- Ergebnis `result` BOOLEAN: wahr, wenn es den Schlüssel gibt

```c0de
CALL HAS_KEY
  PARAMETER hash stock ;
  PARAMETER key "gold" ;
  RESULT result hasGold
```
