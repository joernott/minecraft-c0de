```json
{
  "title": "WHILE und REPEAT",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 17
}
```

WHILE prüft seine Bedingung zuerst. Ist sie gleich zu Beginn falsch, läuft der Block nie. REPEAT prüft am Ende, also läuft sein Block mindestens einmal. Seine Bedingung kommt nach `END REPEAT`.

```c0de
WHILE n > 0 ;
BEGIN
  ASSIGN countdown countdown ⊕ n ⊕ " " ;
  ASSIGN n n - 1 ;
END WHILE
REPEAT
BEGIN
  ASSIGN repeats repeats + 1 ;
END REPEAT n > 0 ;
```

**Rezepte**

<item;c0de:while_block>

<recipe;c0de:while_block>

<item;c0de:repeat_block>

<recipe;c0de:repeat_block>
