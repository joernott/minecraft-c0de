```json
{
  "title": "TO_JSON",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 16
}
```

Wandelt einen Wert in ein JSON-Dokument um.

Mit `indent` 0 ist das Dokument eine Zeile. Wenn `value` eine einzelne Variable mit einem json-Tag ist, wird der Wert am Pfad des Tags abgelegt. `indent` außerhalb von 0 bis 16 löst INVALID\_ARGUMENT aus.

- `value` beliebiger Typ: der Wert, der umgewandelt wird
- `indent` INTEGER: Leerzeichen pro Ebene, 0 bis 16 (optional, Standard 0)
- Ergebnis `result` STRING: das JSON-Dokument

```c0de
CALL c0de/strings/TO_JSON
  PARAMETER value config ;
  PARAMETER indent 2 ;
  RESULT result json
```
