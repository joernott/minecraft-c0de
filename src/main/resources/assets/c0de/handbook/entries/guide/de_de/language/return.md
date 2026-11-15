```json
{
  "title": "RETURN",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 13
}
```

RETURN beendet die Funktion sofort. Es kann eine Variable vom Typ ERROR nennen, um dem Aufrufer ein Problem zu melden.

```c0de
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

Diese Funktion ruft sich selbst auf. Sie addiert alle Zahlen von 1 bis n.
