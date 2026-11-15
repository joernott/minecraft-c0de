```json
{
  "title": "Primzahlen",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 2
}
```

```c0de
PROGRAM primefinder
NOOP
NOOP
FUNCTION main
  RESULT primes STRING
BEGIN
  FOR candidate INTEGER 2 ; 20 ;
  BEGIN
    VARIABLE prime BOOLEAN TRUE
    FOR divisor INTEGER 2 ; candidate - 1 ;
    BEGIN
      IF candidate \ divisor = 0 ;
      BEGIN
        ASSIGN prime FALSE ;
        BREAK
      END IF
    END FOR
    IF prime ;
    BEGIN
      ASSIGN primes primes ⊕ candidate ⊕ " " ;
    END IF
  END FOR
END FUNCTION
```

Die äußere Schleife probiert jede Zahl von 2 bis 20 aus. Die innere Schleife prüft, ob eine kleinere Zahl sie ohne Rest (`\`) teilt. Wenn ja, ist sie keine Primzahl, und [BREAK](^language/break) beendet die innere Schleife. Das Ergebnis ist "2 3 5 7 11 13 17 19 ".

**Rezepte**

<item;c0de:break_block>

<recipe;c0de:break_block>
