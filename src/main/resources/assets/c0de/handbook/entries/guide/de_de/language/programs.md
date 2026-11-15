```json
{
  "title": "Programme",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 0
}
```

Ein Programm beginnt in Zeile 0 mit einem PROGRAM-Block und einem LABEL mit seinem Namen. Der Name muss in der ganzen c0de-Dimension eindeutig sein. Zeile 1 muss eine NOOP-Zeile sein.

Danach dürfen INCLUDE-Zeilen folgen. Sie laden Bibliotheken, zum Beispiel `INCLUDE c0de/math`. Dann kommen Strukturtypen, globale Variablen, Konstanten und Funktionen, in beliebiger Reihenfolge.

Ein Name kann erst verwendet werden, nachdem er definiert wurde. Setze also alles, was eine Funktion braucht, über sie. Das Programm endet an der ersten leeren Zeile.
