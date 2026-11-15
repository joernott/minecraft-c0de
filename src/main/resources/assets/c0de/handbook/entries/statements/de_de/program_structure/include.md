```json
{
  "title": "INCLUDE",
  "icon": "c0de:include_block",
  "category": "program_structure",
  "ordinal": 3
}
```

Eine INCLUDE-Zeile lädt eine Bibliothek.

Auf den Block folgt ein LABEL mit dem Bibliotheksnamen, zum Beispiel `c0de/math`. INCLUDE-Zeilen beginnen bei x=5, direkt nach dem NOOP in Zeile 1. Sie stehen vor allen Strukturen, Variablen, Konstanten und Funktionen.

<recipe;c0de:include_block>

Stelle einen NOOP-Block und eine leere Karte zusammen her.

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
```

Jetzt kann das Programm Funktionen wie `c0de/math/ROUND` aufrufen.
