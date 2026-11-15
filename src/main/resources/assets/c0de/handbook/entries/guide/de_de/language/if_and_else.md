```json
{
  "title": "IF und ELSE",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 14
}
```

IF führt seinen [Codeblock](^language/lines_and_indentation) nur aus, wenn sein [Ausdruck](^language/expressions) wahr ist. Sonst läuft ein ELSE-Block. Mit einem ELSE steht das erste END allein. Auf das letzte END folgt IF.

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

Es gibt kein ELSE IF. Setze stattdessen ein weiteres IF in den ELSE-Block.

**Rezepte**

<item;c0de:if_block>

<recipe;c0de:if_block>

<item;c0de:else_block>

<recipe;c0de:else_block>

<item;c0de:end_block>

<recipe;c0de:end_block>
