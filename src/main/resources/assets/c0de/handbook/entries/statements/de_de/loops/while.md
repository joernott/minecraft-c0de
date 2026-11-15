```json
{
  "title": "WHILE",
  "icon": "c0de:while_block",
  "category": "loops",
  "ordinal": 2
}
```

WHILE führt seinen Codeblock aus, solange seine Bedingung wahr ist.

Es prüft die Bedingung vor jeder Runde. Ist sie gleich zu Beginn falsch, läuft der Codeblock nie. Die Bedingung muss ein BOOLEAN ergeben und endet mit `;`.

<recipe;c0de:while_block>

<recipe;c0de:while_block_from_repeat_block>

Stelle eine Strukturbasis und einen Dioritblock zusammen her, oder verwandle einen REPEAT-Block in einen WHILE-Block.

```c0de
VARIABLE n INTEGER 3
WHILE n > 0 ;
BEGIN
  ASSIGN countdown countdown ⊕ n ⊕ " " ;
  ASSIGN n n - 1 ;
END WHILE
```

Jetzt ist `countdown` "3 2 1 ".
