```json
{
  "title": "Zeilen und Einrückung",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 1
}
```

Globale Zeilen beginnen bei x=5: PROGRAM, INCLUDE, STRUCT, FUNCTION sowie globale VARIABLE- und CONSTANT-Zeilen. Zeilen in einem Codeblock stehen einen Block weiter östlich als sein BEGIN.

Ein Codeblock beginnt mit BEGIN und endet mit END. Auf END folgt das Schlüsselwort, das den Block geöffnet hat, zum Beispiel `END FUNCTION` oder `END WHILE`. Ein Codeblock darf nicht leer sein.

Variablen und Konstanten, die in einem Codeblock angelegt werden, leben nur darin. Sie werden vergessen, wenn der Block endet.
