```json
{
  "title": "DELETE_SECRET",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 1
}
```

Entfernt ein Geheimnis aus deinem Tresor.

Ein dauerhaftes Geheimnis wird auch aus der Datei deines dauerhaften Tresors entfernt, der offen sein muss. Ein Geheimnis, das es nicht gibt, löst SECRET\_NOT\_FOUND aus.

- `name` STRING: der Name des Geheimnisses
- keine Ergebnisse

```c0de
CALL c0de/security/DELETE_SECRET
  PARAMETER name "apiKey" ;
```
