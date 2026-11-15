```json
{
  "title": "Runden",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 12
}
```

INT rundet ab: -12.85 ergibt -13. FIX schneidet einfach die Nachkommastellen ab: -12.85 ergibt -12. ROUND geht zur nächsten ganzen Zahl. Ein .5 geht zum geraden Nachbarn, also ergibt 12.5 die 12 und 13.5 die 14.

```c0de
CALL c0de/math/ROUND
  PARAMETER value 12.5 ;
  RESULT result rounded
```
