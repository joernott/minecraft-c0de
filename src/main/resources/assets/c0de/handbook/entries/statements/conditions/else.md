```json
{
  "title": "ELSE",
  "icon": "c0de:else_block",
  "category": "conditions",
  "ordinal": 2
}
```

ELSE runs its code block when the IF condition was false.

The ELSE line holds only the ELSE block, at the same x as its IF. It comes right after the first END, which then stands alone. The second code block ends with `END IF`.

<recipe;c0de:else_block>

<recipe;c0de:else_block_from_if_block>

Craft a structural base and wheat, or turn an IF block into an ELSE block.

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
