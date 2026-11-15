```json
{
  "title": "Ein absichtlicher Absturz",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 5
}
```

```c0de
PROGRAM errors
NOOP
NOOP
FUNCTION divide
  PARAMETER a INTEGER
  PARAMETER b INTEGER
  RESULT result INTEGER
BEGIN
  VARIABLE quotient FLOAT
  ASSIGN quotient a / b ;
END FUNCTION
NOOP
FUNCTION main
BEGIN
  CALL divide
    PARAMETER a 1 ;
    PARAMETER b 0 ;
END FUNCTION
```

Die Division durch null löst in `divide` den Fehler DIVISION\_BY\_ZERO aus. Der CALL in `main` hat keine ERROR-Variable, die ihn abfängt, also wandert der Fehler nach oben. In der Einstiegsfunktion lässt er den Prozess abstürzen. Der Auslöserdialog zeigt den Fehler und Zeile 9, und ein Komparator liest 15.

**Rezepte**

<item;c0de:call_block>

<recipe;c0de:call_block>

<item;c0de:type_error_block>

<recipe;c0de:type_error_block>
