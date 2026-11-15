```json
{
  "title": "REGEX_REPLACE",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 17
}
```

Ersetzt, was ein regulärer Ausdruck findet, wie der Befehl `s` von sed.

In `replacement` steht `&` für den ganzen Treffer und `\1` bis `\9` für die Gruppen des Musters\; `\&` und `\\` sind ein `&` und ein Backslash, `\n` ist ein Zeilenumbruch. Ohne `flags` wird nur der erste Treffer ersetzt. Die Flags sind `g` für alle Treffer, eine Zahl N für den N-ten Treffer (mit `g` den N-ten und alle danach), `i`, um Groß- und Kleinschreibung zu ignorieren, `m` für `^` und `$` in jeder Zeile und `p`, um das Ergebnis mit PRINT auszugeben, wenn etwas ersetzt wurde. Unbekannte Flags, Gruppen, die das Muster nicht hat, und ungültige Muster lösen INVALID\_ARGUMENT aus.

In der Textschreibweise der Beispiele wird ein Backslash in einem Text doppelt geschrieben, aus `\1` wird also `\\1` und aus `\d` wird `\\d`\; im Dialog eines LITERAL-Blocks tippst du ihn einmal.

- `text` STRING: der Text
- `pattern` STRING: der reguläre Ausdruck
- `replacement` STRING: was einen Treffer ersetzt
- `flags` STRING: wie bei sed, z. B. g oder 2g (optional, Standard leer)
- Ergebnis `result` STRING: der Text mit den Ersetzungen

```c0de
CALL c0de/strings/REGEX_REPLACE
  PARAMETER text "2026-11-15" ;
  PARAMETER pattern "([0-9]+)-([0-9]+)-([0-9]+)" ;
  PARAMETER replacement "\\3.\\2.\\1" ;
  RESULT result german
```
