```json
{
  "title": "IF",
  "icon": "c0de:if_block",
  "category": "conditions",
  "ordinal": 1
}
```

IF führt seinen Codeblock nur aus, wenn sein Ausdruck wahr ist.

Der Ausdruck muss ein BOOLEAN ergeben und endet mit `;`. Danach folgt ein Codeblock. Auf das letzte END der Bedingung folgt ein IF-Block.

Es gibt kein ELSE IF. Setze stattdessen ein weiteres IF in den ELSE-Codeblock.

<recipe;c0de:if_block>

<recipe;c0de:if_block_from_else_block>

Stelle eine Strukturbasis und Weizenkörner zusammen her, oder verwandle einen ELSE-Block in einen IF-Block.

```c0de
IF candidate \ divisor = 0 ;
BEGIN
  ASSIGN prime FALSE ;
END IF
```
