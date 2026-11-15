```json
{
  "title": "Step by step",
  "icon": "c0de:wrench",
  "category": "getting_started",
  "ordinal": 8
}
```

- Line 0: a PROGRAM block at x=5, then a LABEL named `first`.
- Line 1: a [NOOP](^language/noop_and_comment) block at x=5. Every program needs it here.
- Line 2: another NOOP. A [FUNCTION](^language/functions) always needs a NOOP line right above it.
- Line 3: a FUNCTION block at x=5, then a LABEL named `answer`.

- Line 4: a RESULT block at x=6, a LABEL named `result` and an INTEGER [type block](^tools/type_blocks).
- Line 5: a BEGIN block at x=5.
- Line 6: an [ASSIGN](^language/assign) block at x=6, a LABEL `result`, a [LITERAL](^language/literals) 42 and an OPERATOR `;`.
- Line 7: an END block at x=5, followed by a FUNCTION block.

When all [line numbers](^getting_started/line_numbers) are dark green or faint, the program is ready. Leave the line after the END empty. The first empty line ends the program.

**Recipes**

<item;c0de:program_block>

<recipe;c0de:program_block>

<item;c0de:label_block>

<recipe;c0de:label_block>

<item;c0de:noop_block>

<recipe;c0de:noop_block>

<item;c0de:function_block>

<recipe;c0de:function_block>

<item;c0de:result_block>

<recipe;c0de:result_block>

<item;c0de:type_integer_block>

<recipe;c0de:type_integer_block>

<item;c0de:begin_block>

<recipe;c0de:begin_block>

<item;c0de:assign_block>

<recipe;c0de:assign_block>

<item;c0de:literal_block>

<recipe;c0de:literal_block>

<item;c0de:operator_block>

<recipe;c0de:operator_block>

<item;c0de:end_block>

<recipe;c0de:end_block>
