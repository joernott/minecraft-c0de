```json
{
  "title": "OPERATOR",
  "icon": "c0de:operator_block",
  "category": "expressions",
  "ordinal": 1
}
```

An OPERATOR block holds one symbol of an expression.

Right-click it with an empty hand and click a symbol. It can be an operator, `(`, `)` or the `;` that ends every expression. A new block shows a question mark.

- math: `+` `-` `*` `/` `^` (power) `\` (remainder)
- compare: `=` `≠` `<` `>` `≤` `≥`
- logic: `NOT` `AND` `OR` `XOR`, text: `⊕`, bits: `&` `|` `⊻` `¬` `←` `→`

<recipe;c0de:operator_block>

Put a red wool block in the middle and eight NOOP blocks around it. You get eight OPERATOR blocks.

`*` comes before `+`, and `(` and `)` group. `=` only compares\; to store a value, use ASSIGN.

`\` binds weaker than `*` and `/`, `⊕` weaker than `+`, and AND tighter than OR. A `-` in front of a name or a `(` turns the sign around: `- x`. `\` takes only INTEGERs and keeps the sign of the left number: `-7 \ 2` is `-1`. Dividing by zero raises DIVISION\_BY\_ZERO, also for FLOATs.

```c0de
ASSIGN arithmetic 1 + 2 * ( 3 + 3 ) \ 10 * 2 + 10 ;
```
