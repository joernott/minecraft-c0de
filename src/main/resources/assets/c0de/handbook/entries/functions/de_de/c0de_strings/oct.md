```json
{
  "title": "OCT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 15
}
```

Gibt den Oktaltext einer ganzen Zahl zurück.

Negative Zahlen verwenden das Zweierkomplement.

- `value` INTEGER: die Zahl
- Ergebnis `result` STRING: der Oktaltext

- `text` STRING: der Text
- `pattern` STRING: der reguläre Ausdruck
- `replacement` STRING: was einen Treffer ersetzt
- `flags` STRING: wie bei sed, z. B. g oder 2g (optional, Standard leer)
- Ergebnis `result` STRING: der Text mit den Ersetzungen

in jeder Zeile und `p`, um das Ergebnis mit PRINT auszugeben, wenn etwas ersetzt wurde. Unbekannte Flags, Gruppen, die das Muster nicht hat, und ungültige Muster lösen INVALID\_ARGUMENT aus.

```c0de
CALL c0de/strings/REGEX_REPLACE
  PARAMETER text "2026-11-15" ;
  PARAMETER pattern "([0-9]+)-([0-9]+)-([0-9]+)" ;
  PARAMETER replacement "\3.\2.\1" ;
  RESULT result german
```
