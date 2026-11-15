```json
{
  "title": "Mathe-Bibliothek",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 3
}
```

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
NOOP
FUNCTION main
  RESULT rounded INTEGER
  RESULT quotient INTEGER
  RESULT root FLOAT
  RESULT circle STRING
BEGIN
  CALL c0de/math/ROUND
    PARAMETER value 12.5 ;
    RESULT result rounded
  CALL c0de/math/DIV
    PARAMETER dividend -7 ;
    PARAMETER divisor 2 ;
    RESULT result quotient
  CALL c0de/math/SQR
    PARAMETER value 9 ;
    RESULT result root
  CALL c0de/strings/FORMAT
    PARAMETER value c0de/math/PI ;
    PARAMETER format "%.2f" ;
    RESULT result circle
END FUNCTION
```

Dieses Programm lädt zwei Bibliotheken. ROUND macht aus 12.5 die 12, DIV teilt -7 durch 2 und ergibt -3, und SQR ergibt 3.0. FORMAT zeigt PI mit zwei Nachkommastellen: "3.14". Die Funktion hat vier Ergebnisse, also legst du vier nicht initialisierte Variablenkarten hinein.

**Rezepte**

<item;c0de:variable_item>

<recipe;c0de:variable_item_uninitialized>
