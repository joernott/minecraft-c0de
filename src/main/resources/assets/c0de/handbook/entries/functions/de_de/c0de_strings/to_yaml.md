```json
{
  "title": "TO_YAML",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 19
}
```

Wandelt einen Wert in ein YAML-Dokument um.

Das Dokument verwendet den Blockstil mit zwei Leerzeichen Einrückung. Wenn `value` eine einzelne Variable mit einem yaml-Tag ist, wird der Wert am Pfad des Tags abgelegt.

- `value` beliebiger Typ: der Wert, der umgewandelt wird
- Ergebnis `result` STRING: das YAML-Dokument

```c0de
CALL c0de/strings/TO_YAML
  PARAMETER value config ;
  RESULT result yaml
```
