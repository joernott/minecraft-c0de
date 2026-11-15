```json
{
  "title": "FOR loops",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 15
}
```

FOR counts. It has a loop variable, its type (INTEGER or FLOAT), a start value and an end value. An optional third value is the step, which is 1 if you leave it out.

The end value is included. You can't change the loop variable yourself.

```c0de
FOR i INTEGER 1 ; 10 ; 2 ;
BEGIN
  NOOP
END FOR
```
