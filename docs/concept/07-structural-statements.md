# Structural statements

[c0de concept](../concept.md) › Structural statements

Structural statements are conditions and loops. They are only allowed inside code blocks of functions.

## Minecraft item

The structural base item is used to craft the structural blocks.

### Crafting

Eight structural base items can be crafted from a green wool block surrounded by eight NOOP blocks.

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
    "B": "minecraft:green_wool"
  },
  "result": {
    "id": "c0de:structural_base",
    "count": 8
  }
}
```

## Conditional execution

A condition is a structural statement that starts with an IF keyword directly followed by an expression that evaluates to a BOOLEAN. A code block MUST directly follow the IF line. That code block is only executed if the expression evaluates to true.

That code block MAY be followed by a line containing only the ELSE keyword (at the same indentation as the IF keyword) and another code block, which is only executed if the expression evaluated to false.

The last END keyword of a condition MUST be directly followed by an IF block. If the condition has an ELSE part, the END of the first code block stands alone.

```
IF x > 0 ;
BEGIN
    …
END
ELSE
BEGIN
    …
END IF
```

There is no ELSE IF. Conditions are chained by nesting another IF statement in the ELSE code block.

### Minecraft blocks

The IF block is dark green (color #00AA00) and as high as a slab; seen from above, it is a diamond. Its top shows a lime question mark (?).

The ELSE block is dark green (color #00AA00) and as high as a slab; seen from above, it is a diamond. Its top shows an inverted lime question mark (¿).

#### Crafting

An IF block can be crafted shapelessly from a structural base item and wheat seeds. It can also be crafted shapelessly from an ELSE block.

An ELSE block can be crafted shapelessly from a structural base item and wheat. It can also be crafted shapelessly from an IF block.

## Counting loop

A FOR loop is a multi-line structural statement that starts with the FOR keyword, directly followed by a LABEL with the unique name of the loop variable and its type, which MUST be INTEGER or FLOAT. These are followed by an expression that defines the start value, an expression that defines the end value and, optionally, an expression that defines the increment. If the increment is omitted, it is 1. The increment MUST NOT be 0. A code block MUST directly follow the FOR line. Its END keyword MUST be directly followed by a FOR keyword.

The loop variable is only visible inside the loop and MUST NOT be the target of an ASSIGN statement or a CALL result. The start, end and increment values are evaluated once, before the loop starts. The end value is inclusive: with a positive increment, the loop runs as long as the loop variable is less than or equal to the end value, with a negative increment as long as it is greater than or equal to the end value.

```
FOR i INTEGER 1 ; 10 ; 2 ;
BEGIN
    …
END FOR
```

### Minecraft block

The FOR block is a dark green slab (color #00AA00). Its top shows a thin lime circular arrow around a # sign.

#### Crafting

A FOR block can be crafted shapelessly from a structural base item and a dirt block. It can also be crafted shapelessly from an EACH block.

## Iterating loop

An EACH loop is a multi-line structural statement that starts with the EACH keyword, directly followed by a LABEL with the unique name of the loop variable, directly followed by a LABEL referring to a previously defined iterable variable or constant, i.e. an ARRAY or a HASH. A code block MUST directly follow the EACH line. Its END keyword MUST be directly followed by an EACH keyword.

For an array, the loop variable has the subtype of the array and receives the elements in the order of their index. For a hash, the loop variable is a STRING and receives the keys of the hash in ascending lexicographic order; the value is accessed with the key as index. The loop iterates over a copy of the iterable, so changing the iterable inside the loop does not affect the iteration. The loop variable MUST NOT be the target of an ASSIGN statement or a CALL result.

### Minecraft block

The EACH block is a dark green slab (color #00AA00). Its top shows a thin lime circular arrow around a square.

#### Crafting

An EACH block can be crafted shapelessly from a structural base item and a cobblestone block. It can also be crafted shapelessly from a FOR block.

## Conditional loops

There are two conditional loop statements. The WHILE statement evaluates its expression before entering the loop. If it is already false, the code block of the loop is never entered.

The REPEAT statement evaluates its expression at the end of the loop, so its code block is executed at least once.

A WHILE loop starts with a line containing the WHILE keyword, directly followed by an expression that evaluates to a BOOLEAN. A code block MUST directly follow. Its END keyword is directly followed by a WHILE keyword. The code block is executed as long as the expression evaluates to true.

A REPEAT loop starts with a line containing only the REPEAT keyword. A code block MUST directly follow. Its END keyword is directly followed by the REPEAT keyword and an expression that evaluates to a BOOLEAN. The code block is executed again as long as the expression evaluates to true.

### Minecraft blocks

The WHILE block is a dark green slab (color #00AA00). Its top shows a thin lime circular arrow around a question mark.

The REPEAT block is a dark green slab (color #00AA00). Its top shows a lime square around a question mark.

#### Crafting

A WHILE block can be crafted shapelessly from a structural base item and a diorite block. It can also be crafted shapelessly from a REPEAT block.

A REPEAT block can be crafted shapelessly from a structural base item and a granite block. It can also be crafted shapelessly from a WHILE block.
