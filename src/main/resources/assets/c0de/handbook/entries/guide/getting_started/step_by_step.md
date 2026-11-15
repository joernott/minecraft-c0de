```json
{
  "title": "Step by step",
  "icon": "c0de:wrench",
  "category": "getting_started",
  "ordinal": 8
}
```

- Line 0: a PROGRAM block at x=5, then a LABEL named `first`.
- Line 1: a NOOP block at x=5. Every program needs it here.
- Line 2: another NOOP. A FUNCTION always needs a NOOP line right above it.
- Line 3: a FUNCTION block at x=5, then a LABEL named `answer`.

- Line 4: a RESULT block at x=6, a LABEL named `result` and an INTEGER type block.
- Line 5: a BEGIN block at x=5.
- Line 6: an ASSIGN block at x=6, a LABEL `result`, a LITERAL 42 and an OPERATOR `;`.
- Line 7: an END block at x=5, followed by a FUNCTION block.

When all line numbers are dark green or faint, the program is ready. Leave the line after the END empty. The first empty line ends the program.
