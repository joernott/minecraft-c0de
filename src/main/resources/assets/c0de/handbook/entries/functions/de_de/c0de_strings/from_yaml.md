```json
{
  "title": "FROM_YAML",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 19
}
```

Liest ein YAML-Dokument in eine Variable ein.

`destination` muss ein einzelner Variablenname sein\; die Funktion schreibt direkt in die Variable. Nur das erste Dokument wird gelesen. Bei einem Fehler (TYPE\_MISMATCH, INVALID\_ARGUMENT, UNKNOWN\_KEY, STRING\_TOO\_LONG) bleibt die Variable unverändert.

Wie bei FROM\_JSON behält ein Element einer Struktur bei einem fehlenden oder `null`-Schlüssel seinen aktuellen Wert. UNKNOWN\_KEY kommt nur, wenn die Variable einen TAG für `yaml` hat und das Dokument an seinem Pfad keinen Wert hat.

- `source` STRING: das YAML-Dokument
- `destination` Variable: die Variable, die die Werte bekommt
- keine Ergebnisse

```c0de
CALL c0de/strings/FROM_YAML readError
  PARAMETER source config ;
  PARAMETER destination settings ;
```
