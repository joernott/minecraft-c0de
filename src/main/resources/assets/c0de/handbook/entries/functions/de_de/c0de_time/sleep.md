```json
{
  "title": "SLEEP",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 11
}
```

Hält das Programm für eine Anzahl von Ticks an.

20 Ticks sind etwa eine Sekunde. `ticks` muss zwischen 1 und 72000 (eine Stunde) liegen, sonst wird INVALID\_ARGUMENT ausgelöst.

- `ticks` INTEGER: die Anzahl der Ticks, 1 bis 72000

```c0de
CALL c0de/time/SLEEP
  PARAMETER ticks 20 ;
```
