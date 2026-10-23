# Expressions and assignments

[c0de concept](../concept.md) › Expressions and assignments

## Expressions

An expression is a combination of literals, variables, constants and operators that can be evaluated to a result.

A primitive expression is
  - a LITERAL,
  - a LABEL referring to a variable or constant (optionally with an index).

A complex expression is
  - a unary operator directly followed by an expression,
  - an expression followed by a binary operator and another expression, or
  - an opening parenthesis, an expression and a closing parenthesis.

An expression is read from left to right (in ascending x order). Operations are executed in the order of their [precedence](#precedence). Operators with the same precedence are executed from left to right, except for exponentiation (^), which is executed from right to left.

The end of an expression MUST be marked by an OPERATOR block with the end-of-expression symbol `;`. This allows several expressions in one line (e.g. in a FOR statement). An expression MAY span several lines: if a line ends before the `;` block is reached, the expression continues on the following continuation line, which is indented by one block relative to the first line of the statement.

Function calls are not part of expressions. A function is always called with a [CALL statement](06-functions.md#calling-functions).

### Type rules

  - Arithmetic operators accept INTEGER and FLOAT operands. If one operand is a FLOAT, the result is a FLOAT. Division (/) always returns a FLOAT. Exponentiation (^) of two INTEGERs returns an INTEGER if the exponent is not negative, otherwise a FLOAT. Modulus (`\`) requires INTEGER operands; the result has the sign of the dividend (`-7 \ 2` is `-1`).
  - TIME values: an INTEGER number of milliseconds can be added to or subtracted from a TIME value (`TIME + INTEGER`, `INTEGER + TIME`, `TIME - INTEGER`), which results in a TIME. Subtracting two TIME values (`TIME - TIME`) results in the difference in milliseconds as INTEGER. No other arithmetic operator accepts TIME operands.
  - Comparison operators require operands of the same type. INTEGER and FLOAT can be compared with each other, and so can CHAR and STRING. CHAR and STRING values are compared lexicographically by their Unicode code points (`"Z" < "a"`), TIME values chronologically. Only = and ≠ can be used with BOOLEAN, ARRAY, HASH, STRUCT and ERROR operands.
  - Logical operators require BOOLEAN operands and return a BOOLEAN. AND and OR only evaluate their right operand if the left one doesn't decide the result already, so `x ≠ 0 AND 10 / x > 1` doesn't raise DIVISION_BY_ZERO.
  - Bitwise operators require INTEGER operands and return an INTEGER. Only the lowest 6 bits of the shift count are used (as in Java), so shifting by 64 is the same as shifting by 0.
  - The concatenation operator (⊕) converts both operands to their text representation and returns a STRING. The text representation of a BOOLEAN is `true` or `false`, of a FLOAT for example `2.5` or `1.0E20`, of a TIME the ISO 8601 format, of an ARRAY `[1, 2, 3]` and of a HASH or STRUCT `{key: value, key: value}`.

Applying an operator to operands of the wrong type raises a TYPE_MISMATCH error.

## Operators

### Minecraft block

All operators, the parentheses and the end-of-expression symbol (`;`) are represented by a single OPERATOR block. Its symbol is chosen in a dialog that opens when the block is right-clicked with an empty hand. The dialog shows a button for every operator symbol, `(`, `)` and `;`; clicking a button saves the symbol and closes the dialog. A newly crafted OPERATOR block has no symbol yet.

An OPERATOR block is its symbol in dark red (color #AA0000), lying on the floor and extruded 4 pixels high. Until a symbol is chosen, it is a question mark.

#### Crafting

Eight OPERATOR blocks can be crafted from a red wool block surrounded by eight NOOP blocks.

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "AAA",
    "ABA",
    "AAA"
  ],
  "key": {
    "A": "c0de:noop_block",
    "B": "minecraft:red_wool"
  },
  "result": {
    "id": "c0de:operator_block",
    "count": 8
  }
}
```

### Grouping

Parts of an expression can be grouped with parentheses ( and ). Parentheses can be nested up to a depth of 8 levels.

### Arithmetic operators

  - Addition: +
  - Subtraction: - (also unary negation)
  - Multiplication: *
  - Division: /
  - Exponentiation: ^
  - Modulus (remainder): `\`

### Comparison operators

  - Equal: =
  - Not equal: ≠
  - Greater than: >
  - Less than: <
  - Greater or equal: ≥
  - Less or equal: ≤

The equal sign is only a comparison operator. Values are assigned with the [ASSIGN statement](#assigning-values).

### Logical operators

  - NOT (unary)
  - AND
  - OR
  - XOR

### String operators

  - Concatenation: ⊕

### Bitwise operators

  - Bitwise and: &
  - Bitwise or: |
  - Bitwise exclusive or: ⊻
  - Bitwise negation: ¬ (unary)
  - Shift left: ←
  - Shift right: → (arithmetic shift, keeps the sign)

### Precedence

From highest to lowest:
  1. Parentheses: ( )
  2. Bitwise negation: ¬
  3. Logical negation: NOT
  4. Exponentiation: ^
  5. Unary minus: - (e.g. -X)
  6. Multiplication and division: *, /
  7. Modulus: `\`
  8. Addition and subtraction: +, -
  9. Shift: ←, →
  10. String concatenation: ⊕
  11. Comparison: =, ≠, <, >, ≤, ≥
  12. Bitwise and: &
  13. Bitwise exclusive or: ⊻
  14. Bitwise or: |
  15. Logical AND
  16. Logical XOR
  17. Logical OR

## Assigning values

An ASSIGN statement assigns a value to a variable. The variable MUST be defined before it can be assigned a value.

An ASSIGN statement consists of the ASSIGN keyword, directly followed by a LABEL referring to the target variable (optionally with an index), directly followed by an expression.

The value of the expression MUST match the type of the target. An INTEGER value MAY be assigned to a FLOAT target and a CHAR value MAY be assigned to a STRING target; all other combinations of different types raise a TYPE_MISMATCH error. Complex values are copied: after `ASSIGN a b;`, changing `a` does not change `b`.

Assigning to the index of an array that equals the length of the array appends a new element. Assigning to a key of a hash that does not exist yet adds a new element.

### Minecraft block

ASSIGN blocks are dark aqua slabs (color #00AAAA). Their top shows a white equilateral triangle with one corner pointing towards positive x.

#### Crafting

Eight ASSIGN blocks can be crafted from a cyan wool block surrounded by eight NOOP blocks.

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "AAA",
    "ABA",
    "AAA"
  ],
  "key": {
    "A": "c0de:noop_block",
    "B": "minecraft:cyan_wool"
  },
  "result": {
    "id": "c0de:assign_block",
    "count": 8
  }
}
```
