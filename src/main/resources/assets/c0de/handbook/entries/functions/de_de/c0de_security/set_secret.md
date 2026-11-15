```json
{
  "title": "SET_SECRET",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 0
}
```

Legt ein Geheimnis in deinen Tresor.

Ein Geheimnis mit demselben Namen wird ersetzt. Der Typ des Geheimnisses ist der Typ von `value`. Ein Tresor fasst bis zu 128 Geheimnisse mit bis zu 2000 Zeichen. Mit `persistent` TRUE wird das Geheimnis auch in deinem dauerhaften Tresor gespeichert, der offen sein muss (sonst VAULT\_NOT\_OPEN). Der Cursor wartet, während dein Spiel die Datei verschlüsselt.

- `name` STRING: der Name des Geheimnisses
- `value` BOOLEAN, INTEGER, FLOAT, CHAR, STRING oder TIME: der Wert
- `persistent` BOOLEAN: auch im dauerhaften Tresor speichern (optional, Standard FALSE)
- keine Ergebnisse

```c0de
CALL c0de/security/SET_SECRET
  PARAMETER name "apiKey" ;
  PARAMETER value key ;
  PARAMETER persistent TRUE ;
```
