```json
{
  "title": "Namen",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 3
}
```

LABEL-Blöcke enthalten Namen. Ein Name besteht aus den Buchstaben a-z und A-Z, den Ziffern 0-9 und dem Unterstrich. Er muss mit einem Buchstaben beginnen.

Schlüsselwörter in Großbuchstaben sind reserviert: `RESULT` ist kein gültiger Name, aber `result` und `Result` sind es. Bibliotheksnamen verwenden Schrägstriche, zum Beispiel `c0de/math/PI`.

Ein Label kann mit einem **Index** in einen Wert hineinzeigen, zum Beispiel `list[0]`, `stock["gold"]` oder `problem.code`. Den Index gibst du als Text im LABEL-Dialog ein.

Jeder Teil eines Index ist eine Zahl, ein Text in Anführungszeichen, ein Elementname oder der Name einer Variablen oder Konstanten, zum Beispiel `list[i]`. Er kann keine Rechnung sein: `list[i - 1]` geht nicht. Speichere `i - 1` zuerst in einer Variablen und verwende diese.

**Rezepte**

<item;c0de:label_block>

<recipe;c0de:label_block>
