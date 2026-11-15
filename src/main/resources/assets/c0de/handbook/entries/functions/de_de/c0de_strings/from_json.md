```json
{
  "title": "FROM_JSON",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 20
}
```

Liest ein JSON-Dokument in eine Variable ein.

`destination` muss ein einzelner Variablenname sein\; die Funktion schreibt direkt in die Variable. Das Dokument muss zum Typ der Variablen passen. Bei einem Fehler (TYPE\_MISMATCH, INVALID\_ARGUMENT, UNKNOWN\_KEY, STRING\_TOO\_LONG) bleibt die Variable unverändert.

Ein Element einer Struktur, dessen Schlüssel im Dokument fehlt oder `null` ist, behält seinen aktuellen Wert. Der Wert, den die Variable vorher hatte, liefert also die Standardwerte. Arrays und Hashes werden ganz ersetzt.

UNKNOWN\_KEY kommt nur, wenn die Variable einen TAG für `json` hat und das Dokument am Pfad dieses Tags keinen Wert hat. Ein fehlender Schlüssel innerhalb des Werts löst ihn nie aus.

- `source` STRING: das JSON-Dokument
- `destination` Variable: die Variable, die die Werte bekommt
- keine Ergebnisse

```c0de
CALL c0de/strings/FROM_JSON readError
  PARAMETER source config ;
  PARAMETER destination port ;
```
