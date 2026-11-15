```json
{
  "title": "Expressions",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 8
}
```

An expression calculates a value from literals, variables, constants and operators. It always ends with an OPERATOR block showing `;`. An expression may go on in the next line, one block further east.

Calculations follow the usual rules: `*` before `+`. Use `(` and `)` to group. A division with `/` always gives a FLOAT.

A minus sign in front of a name or a `(` turns the sign around: `- x * 2 ;`. The minus sign is an OPERATOR block of its own, so the text notation writes `- x`, not `-x`. `-7` in one piece is a LITERAL with a negative number.

Some rules are less usual. `\` binds weaker than `*` and `/`: `7 \ 3 * 2` is `7 \ 6`, so use parentheses. `⊕` binds weaker than `+`, and AND binds tighter than OR: `a OR b AND c` means `a OR ( b AND c )`.

You can't call a function inside an expression. Use a CALL statement instead.

**Recipes**

<item;c0de:operator_block>

<recipe;c0de:operator_block>

<item;c0de:type_float_block>

<recipe;c0de:type_float_block>

<item;c0de:literal_block>

<recipe;c0de:literal_block>

<item;c0de:call_block>

<recipe;c0de:call_block>
