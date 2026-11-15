```json
{
  "title": "CALL",
  "icon": "c0de:call_block",
  "category": "functions",
  "ordinal": 4
}
```

Eine CALL-Zeile führt eine Funktion aus.

Danach folgt ein LABEL mit dem Funktionsnamen. PARAMETER-Zeilen geben ihr Werte, RESULT-Zeilen fangen ihre Ergebnisse auf. Ein zweites LABEL in der CALL-Zeile darf eine ERROR-Variable nennen. Sie fängt einen Fehler ab, und deine Funktion läuft weiter.

Ohne diese ERROR-Variable stoppt ein Fehler auch deine Funktion und geht weiter an ihren Aufrufer. Du kannst keine Funktion innerhalb eines Ausdrucks aufrufen.

Die ERROR-Variable bekommt den Fehler jedes Aufrufs, auch „kein Fehler“: Nach einem CALL ohne Fehler ist ihr `code` wieder 0.

<recipe;c0de:call_block>

Stelle einen NOOP-Block und einen glatten Stein zusammen her.

```c0de
VARIABLE problem ERROR
CALL c0de/math/DIV problem
  PARAMETER dividend -7 ;
  PARAMETER divisor 2 ;
  RESULT result quotient
```
