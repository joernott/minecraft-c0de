```json
{
  "title": "Zahlen addieren",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 1
}
```

```c0de
PROGRAM adder
NOOP
NOOP
FUNCTION sum
  PARAMETER n INTEGER
  RESULT result INTEGER
BEGIN
  IF n = 0 ;
  BEGIN
    RETURN
  END IF
  CALL sum
    PARAMETER n n - 1 ;
    RESULT result result
  ASSIGN result result + n ;
END FUNCTION
```

Diese Funktion addiert alle Zahlen von 1 bis `n`. Sie ruft sich selbst mit `n - 1` auf, bis `n` 0 ist, und addiert dann auf dem Rückweg `n`. Für `n` = 10 ist das Ergebnis 55.

Zum Ausprobieren legst du eine INTEGER-Variablenkarte mit dem Namen `n` hinein. Jeder Aufruf braucht Platz auf dem Stack des Cursors, deshalb können sehr große Zahlen ihn abstürzen lassen.
