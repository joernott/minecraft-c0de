```json
{
  "title": "PARAMETER",
  "icon": "c0de:parameter_block",
  "category": "functions",
  "ordinal": 1
}
```

Eine PARAMETER-Zeile ist eine Eingabe einer Funktion.

Unter einer FUNCTION hat sie einen Namen, einen Typ und einen optionalen Standardwert. Unter einem CALL hat sie den Namen eines Parameters und einen Ausdruck mit seinem Wert. PARAMETER-Zeilen stehen einen Block weiter östlich als ihre FUNCTION oder ihr CALL.

Ein Aufrufer darf Parameter mit Standardwert weglassen. Parameter sind Kopien: Wenn du sie änderst, ändern sich die Variablen des Aufrufers nicht. Ein Parameter und ein Ergebnis derselben Funktion können nicht denselben Namen haben (SYNTAX\_ERROR\_FUNCTION).

<recipe;c0de:parameter_block>

<recipe;c0de:parameter_block_from_result_block>

Lege einen Trichter in die Mitte und acht NOOP-Blöcke darum herum für acht PARAMETER-Blöcke, oder verwandle einen RESULT-Block in einen PARAMETER-Block.

```c0de
CALL greet
  PARAMETER name "c0de" ;
  RESULT result greeting
```
