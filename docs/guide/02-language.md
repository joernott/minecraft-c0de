# The Language

This chapter explains the c0de language in short. Every part is a block you place on a sheet. The examples use the text notation from the first chapter.

## Programs

A program starts at line 0 with a PROGRAM block and a LABEL with its name. The name must be unique in the whole c0de dimension. Line 1 must be a NOOP line.

After that, INCLUDE lines may follow. They load libraries, like `INCLUDE c0de/math`. Then come struct types, global variables, constants and functions, in any order.

A name can only be used after it was defined. So put everything a function needs above it. The program ends at the first empty line.

## Lines and indentation

Global lines start at x=5: PROGRAM, INCLUDE, STRUCT, FUNCTION and global VARIABLE and CONSTANT lines. Lines inside a code block are one block further east than its BEGIN.

A code block starts with BEGIN and ends with END. END is followed by the keyword that opened the block, like `END FUNCTION` or `END WHILE`. A code block may not be empty.

Variables and constants made inside a code block only live inside it. They are forgotten when the block ends.

## NOOP and COMMENT

NOOP means "no operation". A NOOP line does nothing. Use it to separate parts of your program. A NOOP line may not hold any other block. Every FUNCTION needs a NOOP line right above it.

A COMMENT block holds a note for humans. Put it at the end of a line. The cursor ignores it and every block after it. So a COMMENT in front of blocks switches them off.

A line that starts with a COMMENT block is a comment line. It is skipped like a NOOP line.

```c0de
// A comment line is skipped completely
VARIABLE time INTEGER 1 // a comment at the end
ASSIGN count time ; COMMENT("disabled") + 100 ;
```

In the last line, `COMMENT("disabled")` is a COMMENT block with the text "disabled". The `+ 100 ;` after it is switched off.

## Names

LABEL blocks hold names. A name uses the letters a-z and A-Z, the digits 0-9 and the underscore. It must start with a letter.

Keywords in capitals are reserved: `RESULT` is not a valid name, but `result` and `Result` are. Library names use slashes, like `c0de/math/PI`.

A label can point into a value with an **index**, like `list[0]`, `stock["gold"]` or `problem.code`. You enter the index as text in the LABEL dialog.

## Types

A type block follows the name of a variable, constant, parameter or result. Simple types:

- BOOLEAN: true or false
- INTEGER: whole numbers
- FLOAT: numbers with decimals
- CHAR: one character
- TIME: a point in time

Complex types:

- STRING: text, optionally with a length limit
- ARRAY: a list, numbered from 0
- HASH: values stored under text keys
- STRUCT: your own type with named elements
- ERROR: an error with code, message, context and line

ARRAY and HASH are followed by the type of their content, like `ARRAY INTEGER`. A STRUCT type is followed by a LABEL with the struct name. Arrays and hashes grow and shrink by themselves.

## Variables and constants

A VARIABLE line has a name, a type and an optional start value. Without one, it holds the default: false, 0, 0.0, a space, an empty text or list.

A CONSTANT line looks the same, but it must have a value. You can't change it later.

```c0de
VARIABLE b BOOLEAN TRUE
VARIABLE i INTEGER
CONSTANT BASE INTEGER 42
```

Lists, hashes and structs get their start values in a code block. Each line holds literals or a key and a value:

```c0de
VARIABLE list ARRAY INTEGER
BEGIN
    1 2 3
END VARIABLE
VARIABLE stock HASH INTEGER
BEGIN
    gold 1
END VARIABLE
```

## Structs

A STRUCT declares your own type. Each line in its code block is an element: a name and a type.

```c0de
STRUCT point
BEGIN
    x INTEGER
    y INTEGER
END STRUCT
```

A variable of this type is declared as `VARIABLE p STRUCT point`. Its elements are reached with an index, like `p.x`.

Every program also has a few constants it doesn't declare. `SHEET` describes its sheet, e.g. `SHEET.number` and `SHEET.owner`. `PID` describes the running process, e.g. `PID.pid`, `PID.activator` (who started it) and `PID.x`, `PID.y`, `PID.z` (where its trigger block is). `CURSOR` describes the cursor right now: `CURSOR.line`, `CURSOR.function`, `CURSOR.depth` and how much of its stack is used, `CURSOR.stack_used` of `CURSOR.stack_size`. The names of the errors and of the colors are constants too.

## Literals

A LITERAL block holds a fixed value. In its dialog you pick the type and enter the value. The text notation writes them like this:

- `TRUE` and `FALSE`
- `42` and `-7` for integers, `3.14` for floats
- `'a'` for a CHAR, `"Hello"` for a STRING
- `@2026-10-19T12:05:09Z` for a TIME

## Expressions

An expression calculates a value from literals, variables, constants and operators. It always ends with an OPERATOR block showing `;`. An expression may go on in the next line, one block further east.

Calculations follow the usual rules: `*` before `+`. Use `(` and `)` to group. A division with `/` always gives a FLOAT.

You can't call a function inside an expression. Use a CALL statement instead.

## Operators

- math: `+` `-` `*` `/` `^` (power) `\` (remainder)
- compare: `=` `≠` `<` `>` `≤` `≥`
- logic: `NOT` `AND` `OR` `XOR`
- text: `⊕` joins two values into a text
- bits: `&` `|` `⊻` `¬` `←` `→`

`=` only compares. To store a value, use ASSIGN. `⊕` turns numbers and lists into text, so `'a' ⊕ 1 ⊕ "b"` gives `"a1b"`.

## ASSIGN

ASSIGN stores the value of an expression in a variable. The value must fit the type of the variable. An INTEGER may go into a FLOAT, and a CHAR into a STRING.

```c0de
ASSIGN arithmetic 1 + 2 * ( 3 + 3 ) \ 10 * 2 + 10 ;
ASSIGN list[0] 10 ;
ASSIGN list[1] 20 ;
ASSIGN list[2] list[0] + list[1] ;
```

Assigning to the index right after the end of a list adds a new element. A new key adds a new element to a hash.

## Functions

A FUNCTION line starts at x=5 with the function name. PARAMETER lines below it name the inputs, RESULT lines the outputs. Then follows the code block.

A parameter may have a default value. A caller can leave such a parameter out. Results start with the default value of their type.

```c0de
FUNCTION greet
    PARAMETER name STRING "World"
    PARAMETER mark CHAR '!'
    RESULT result STRING
BEGIN
    ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```

When the function ends, the last values of its results go back to the caller.

## CALL

CALL runs a function. PARAMETER lines give it values, RESULT lines say which variable gets which result. Results you don't list are thrown away.

```c0de
CALL greet
    PARAMETER name "c0de" ;
    RESULT result greeting
```

Here `greeting` becomes "Hello, c0de!". A function can only call functions that are defined above it, or itself.

## RETURN

RETURN ends the function right away. It may name a variable of type ERROR to report a problem to the caller.

```c0de
FUNCTION sum
    PARAMETER n INTEGER
    RESULT result INTEGER
BEGIN
    IF n = 0 ;
    BEGIN
        RETURN
    END IF
    CALL sum
        PARAMETER n n - 1 ;
        RESULT result result
    ASSIGN result result + n ;
END FUNCTION
```

This function calls itself. It adds up all numbers from 1 to n.

## IF and ELSE

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

## FOR loops

FOR counts. It has a loop variable, its type (INTEGER or FLOAT), a start value and an end value. An optional third value is the step, which is 1 if you leave it out.

The end value is included. You can't change the loop variable yourself.

```c0de
FOR i INTEGER 1 ; 10 ; 2 ;
BEGIN
    NOOP
END FOR
```

## EACH loops

EACH goes through an array or a hash. For an array, the loop variable gets each element. For a hash, it gets each key in alphabetical order.

```c0de
EACH key h
BEGIN
    ASSIGN keys keys ⊕ key ⊕ "=" ⊕ h[key] ⊕ " " ;
END EACH
```

## WHILE and REPEAT

WHILE checks its condition first. If it is false right away, the block never runs. REPEAT checks at the end, so its block runs at least once. Its condition comes after `END REPEAT`.

```c0de
WHILE n > 0 ;
BEGIN
    ASSIGN countdown countdown ⊕ n ⊕ " " ;
    ASSIGN n n - 1 ;
END WHILE
REPEAT
BEGIN
    ASSIGN repeats repeats + 1 ;
END REPEAT n > 0 ;
```

## BREAK

BREAK leaves the innermost loop at once. It may have a condition: then it only leaves the loop if the condition is true.

## EXIT

EXIT ends the whole process at once, no matter how deep the cursor is. It needs a number from 0 to 15: the **exit code**. A comparator next to the trigger block can read it.

```c0de
EXIT 3 ;
```

Exit code 15 means the program crashed. You may use `EXIT 15 ;` for a fatal problem too.

## Errors

When something goes wrong, like a division by zero, the function stops and **raises** an error. The error goes to the caller, and then up to its caller.

If the error reaches the entry function, the process **crashes**. Mistakes in the program itself, like a wrong indentation, crash it at once.

To catch an error, add a variable of type ERROR to the CALL line. The caller then goes on, and you can check the error's `code`, `message`, `context` and `line`.

```c0de
VARIABLE problem ERROR
CALL fail problem
ASSIGN code problem.code ;
```

## Your own errors

You can raise your own errors. Their codes must be 1000 or higher. Set the code of an ERROR variable and return it, or use the built-in function RAISE.

```c0de
FUNCTION fail
BEGIN
    VARIABLE e ERROR
    ASSIGN e.code 1000 ;
    RETURN e
END FUNCTION
```
