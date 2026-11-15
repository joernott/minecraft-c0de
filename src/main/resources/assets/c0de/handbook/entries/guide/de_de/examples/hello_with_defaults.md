```json
{
  "title": "Hallo mit Standardwerten",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 0
}
```

```c0de
PROGRAM greeting
NOOP
NOOP
FUNCTION greet
  PARAMETER name STRING "World"
  PARAMETER mark CHAR '!'
  RESULT result STRING
BEGIN
  ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```

Die Funktion fügt mit `⊕` drei Teile zu einem Text zusammen. Beide Parameter haben Standardwerte, deshalb darf ein Aufrufer sie weglassen. Mit dem Namen "c0de" aufgerufen, ist das Ergebnis "Hello, c0de\!".

Zum Ausprobieren stellst du den Auslöserblock auf `greet`. Leg eine STRING-Variablenkarte mit dem Namen `name` und deinem Namen als Wert hinein.
