```json
{
  "title": "IF and ELSE",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 14
}
```

IF runs its code block only when its expression is true. An ELSE block runs otherwise. With an ELSE, the first END stands alone. The last END is followed by IF.

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
