```json
{
  "title": "REGEX_MATCH",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 16
}
```

Prüft, ob ein Text zu einem regulären Ausdruck passt.

Das Muster darf zu einem beliebigen Teil des Texts passen\; `^` und `$` binden es an den Anfang und das Ende. c0de verwendet die regulären Ausdrücke von Java, die die Syntax von PCRE haben: Klassen wie `[a-z]`, `\d` und `\w`, die Quantoren `*`, `+`, `?` und `{2,4}`, Gruppen, Lookahead und Lookbehind und eingebettete Optionen wie `(?i)`, um Groß- und Kleinschreibung zu ignorieren. Ein ungültiges Muster löst INVALID\_ARGUMENT aus, ebenso ein Muster, das zu lange braucht.

- `text` STRING: der Text
- `pattern` STRING: der reguläre Ausdruck
- Ergebnis `result` BOOLEAN: TRUE, wenn das Muster passt

```c0de
CALL c0de/strings/REGEX_MATCH
  PARAMETER text input ;
  PARAMETER pattern "^[0-9]{4}-[0-9]{2}-[0-9]{2}$" ;
  RESULT result isDate
```
