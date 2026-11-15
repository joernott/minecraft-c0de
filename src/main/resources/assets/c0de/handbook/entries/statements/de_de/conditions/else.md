```json
{
  "title": "ELSE",
  "icon": "c0de:else_block",
  "category": "conditions",
  "ordinal": 2
}
```

ELSE führt seinen Codeblock aus, wenn die IF-Bedingung falsch war.

Die ELSE-Zeile enthält nur den ELSE-Block, beim selben x wie ihr IF. Sie kommt direkt nach dem ersten END, das dann allein steht. Der zweite Codeblock endet mit `END IF`.

<recipe;c0de:else_block>

<recipe;c0de:else_block_from_if_block>

Stelle eine Strukturbasis und Weizen zusammen her, oder verwandle einen IF-Block in einen ELSE-Block.

```c0de
IF prime ;
BEGIN
  ASSIGN primes primes ⊕ candidate ⊕ " " ;
END
ELSE
BEGIN
  NOOP
END IF
```
