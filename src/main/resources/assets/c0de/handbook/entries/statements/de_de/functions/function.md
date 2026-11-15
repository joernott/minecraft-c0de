```json
{
  "title": "FUNCTION",
  "icon": "c0de:function_block",
  "category": "functions",
  "ordinal": 0
}
```

Eine FUNCTION-Zeile beginnt eine Funktion.

Sie steht bei x=5, direkt unter einer NOOP-Zeile. Danach folgt ein LABEL mit dem Funktionsnamen. Als Nächstes können PARAMETER- und RESULT-Zeilen kommen, dann der Codeblock. Sie endet mit `END FUNCTION`.

Eine Funktion kann nur Funktionen über ihr aufrufen, oder sich selbst. Struktur- und Funktionsdeklarationen sind innerhalb einer Funktion nicht erlaubt.

<recipe;c0de:function_block>

Stelle einen NOOP-Block und einen Steinblock zusammen her.

```c0de
NOOP
FUNCTION greet
  PARAMETER name STRING "World"
  PARAMETER mark CHAR '!'
  RESULT result STRING
BEGIN
  ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```
