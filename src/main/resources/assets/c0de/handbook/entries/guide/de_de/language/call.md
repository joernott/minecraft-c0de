```json
{
  "title": "CALL",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 12
}
```

CALL führt eine Funktion aus. PARAMETER-Zeilen geben ihr Werte, RESULT-Zeilen sagen, welche Variable welches Ergebnis bekommt. Ergebnisse, die du nicht aufführst, werden verworfen.

```c0de
CALL greet
  PARAMETER name "c0de" ;
  RESULT result greeting
```

Hier wird `greeting` zu "Hello, c0de\!". Eine Funktion kann nur Funktionen aufrufen, die über ihr definiert sind, oder sich selbst.

Dieselbe Variable darf einen Parameter liefern und ein Ergebnis bekommen, zum Beispiel `PARAMETER name greeting ;` und `RESULT result greeting`.
