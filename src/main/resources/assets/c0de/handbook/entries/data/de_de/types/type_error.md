```json
{
  "title": "TYPE_ERROR",
  "icon": "c0de:type_error_block",
  "category": "types",
  "ordinal": 10
}
```

TYPE\_ERROR steht für den Typ ERROR: eine Fehlermeldung.

Ein Fehler hat vier Elemente: `code`, `message`, `context` (die Funktion) und `line`. Code 0 bedeutet: kein Fehler. Deine eigenen Fehlercodes müssen 1000 oder höher sein.

Ein CALL mit einer ERROR-Variablen schreibt jedes Mal in sie. Nach einem CALL ohne Fehler ist ihr `code` wieder 0. Die Namen der Fehler sind Konstanten, auch die der Bibliotheken von c0de, wie PERMISSION\_DENIED oder CHUNK\_NOT\_LOADED. Fehler anderer Mods sind nur Konstanten, wenn die Mod sie hinzufügt.

<recipe;c0de:type_error_block>

Stelle eine Typbasis und ein Lagerfeuer zusammen her.

```c0de
VARIABLE problem ERROR
CALL fail problem
ASSIGN code problem.code ;
```

Das CALL fängt den Fehler von `fail` in `problem` ab.
