```json
{
  "title": "Datum und Uhrzeit",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 4
}
```

```c0de
PROGRAM time
NOOP
INCLUDE c0de/strings
INCLUDE c0de/time
NOOP
FUNCTION main
  RESULT now STRING
  RESULT week INTEGER
  RESULT weekday INTEGER
  RESULT later TIME
BEGIN
  VARIABLE t TIME
  CALL c0de/time/NOW
    RESULT result t
  CALL c0de/strings/FORMAT
    PARAMETER value t ;
    PARAMETER format "%tF %tT" ;
    RESULT result now
  CALL c0de/time/WEEK
    RESULT result week
  CALL c0de/time/WEEKDAY
    PARAMETER time t ;
    RESULT result weekday
  ASSIGN later t + 1000 ;
  CALL c0de/time/SLEEP
    PARAMETER ticks 2 ;
END FUNCTION
```

NOW liest die Uhr, FORMAT macht daraus einen Text wie "2026-10-19 12:05:09". WEEK und WEEKDAY ergeben die Kalenderwoche und den Wochentag (1 ist Montag). Wenn du 1000 zu einer TIME addierst, kommt eine Sekunde dazu. SLEEP wartet vor dem Ende zwei Spielticks.
