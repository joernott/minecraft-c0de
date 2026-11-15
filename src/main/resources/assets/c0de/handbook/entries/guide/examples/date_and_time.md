```json
{
  "title": "Date and time",
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

NOW reads the clock, FORMAT turns it into text like "2026-10-19 12:05:09". WEEK and WEEKDAY give the week of the year and the day (1 is Monday). Adding 1000 to a TIME adds one second. SLEEP waits two game ticks before the end.
