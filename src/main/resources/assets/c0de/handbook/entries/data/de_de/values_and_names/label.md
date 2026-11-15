```json
{
  "title": "LABEL",
  "icon": "c0de:label_block",
  "category": "values_and_names",
  "ordinal": 0
}
```

Ein LABEL-Block enthält einen Namen.

Labels benennen Programme, Funktionen, Variablen und mehr. Ein Name besteht aus a-z, A-Z, 0-9 und dem Unterstrich, und er beginnt mit einem Buchstaben. Schlüsselwörter in Großbuchstaben sind reserviert: `RESULT` ist kein gültiger Name, `result` aber schon.

Ein Label kann mit einem Index in einen Wert zeigen, zum Beispiel `list[0]`, `stock["gold"]` oder `problem.code`. Klicke den Block mit leerer Hand mit rechts an, um den Namen und den Index einzugeben.

Jeder Teil eines Index ist eine Zahl, ein Text in Anführungszeichen, ein Elementname oder der Name einer Variablen oder Konstanten. Eine Rechnung wie `list[i - 1]` geht nicht\; speichere `i - 1` zuerst in einer Variablen.

<recipe;c0de:label_block>

Stelle einen NOOP-Block und ein Papier zusammen her.

```c0de
VARIABLE score INTEGER
ASSIGN score score + 1 ;
```

Alle drei `score`-Blöcke sind LABEL-Blöcke. Der erste benennt die Variable, die anderen verweisen auf sie.
