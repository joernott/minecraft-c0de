```json
{
  "title": "HAS_SECRET",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 2
}
```

Prüft, ob dein Tresor ein Geheimnis hat.

- `name` STRING: der Name des Geheimnisses
- Ergebnis `result` BOOLEAN: wahr, wenn es das Geheimnis gibt

```c0de
CALL c0de/security/HAS_SECRET
  PARAMETER name "apiKey" ;
  RESULT result found
```
