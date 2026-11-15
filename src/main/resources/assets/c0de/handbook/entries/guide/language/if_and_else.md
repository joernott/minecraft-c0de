```json
{
  "title": "IF and ELSE",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 14
}
```

IF runs its [code block](^language/lines_and_indentation) only when its [expression](^language/expressions) is true. An ELSE block runs otherwise. With an ELSE, the first END stands alone. The last END is followed by IF.

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

There is no ELSE IF. Put another IF inside the ELSE block instead.

**Recipes**

<item;c0de:if_block>

<recipe;c0de:if_block>

<item;c0de:else_block>

<recipe;c0de:else_block>

<item;c0de:end_block>

<recipe;c0de:end_block>
