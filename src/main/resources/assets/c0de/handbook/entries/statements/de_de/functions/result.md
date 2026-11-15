```json
{
  "title": "RESULT",
  "icon": "c0de:result_block",
  "category": "functions",
  "ordinal": 2
}
```

Eine RESULT-Zeile ist eine Ausgabe einer Funktion.

Unter einer FUNCTION hat sie einen Namen und einen Typ. Sie beginnt mit dem Standardwert ihres Typs. Wenn die Funktion endet, geht ihr letzter Wert zurück an den Aufrufer. Unter einem CALL nennt sie ein Ergebnis und die Variable, die es bekommt.

RESULT-Zeilen kommen nach den PARAMETER-Zeilen. Ergebnisse, die der Aufrufer nicht aufführt, werden weggeworfen.

<recipe;c0de:result_block>

Verwandle einen PARAMETER-Block in einen RESULT-Block.

```c0de
CALL c0de/math/ROUND
  PARAMETER value 12.5 ;
  RESULT result rounded
```

Das Ergebnis `result` von ROUND kommt in die Variable `rounded`.
