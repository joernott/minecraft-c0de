```json
{
  "title": "Funktionen",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 11
}
```

Eine FUNCTION-Zeile beginnt bei x=5 mit dem Namen der Funktion. PARAMETER-Zeilen darunter benennen die Eingaben, RESULT-Zeilen die Ausgaben. Dann folgt der Codeblock.

Ein Parameter darf einen Standardwert haben. Ein Aufrufer kann einen solchen Parameter weglassen. Ergebnisse beginnen mit dem Standardwert ihres Typs.

```c0de
FUNCTION greet
  PARAMETER name STRING "World"
  PARAMETER mark CHAR '!'
  RESULT result STRING
BEGIN
  ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```

Wenn die Funktion endet, gehen die letzten Werte ihrer Ergebnisse an den Aufrufer zurück.

Alle Parameter und Ergebnisse einer Funktion brauchen verschiedene Namen. Ein Parameter `value` und ein Ergebnis `value` ergeben SYNTAX\_ERROR\_FUNCTION. Nenne das Ergebnis zum Beispiel `result`. Der Aufrufer darf trotzdem eine Variable für beides verwenden, siehe CALL.

**Rezepte**

<item;c0de:function_block>

<recipe;c0de:function_block>

<item;c0de:parameter_block>

<recipe;c0de:parameter_block>

<item;c0de:result_block>

<recipe;c0de:result_block>

<item;c0de:call_block>

<recipe;c0de:call_block>
