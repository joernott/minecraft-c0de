# The cursor

[c0de concept](../concept.md) › The cursor

The cursor is an entity that is created when a process starts. It is placed directly after the PID block it belongs to (x=4, y=PID, z=1 relative to the sheet).

A cursor looks like an upright cuboid, one block high and 3/4 of a block wide, in the middle of its block. It slowly pulses between dark green (#00AA00) and lime (#55FF55). A debug cursor pulses between dark red (#AA0000) and red (#FF5555). While its process waits (WAITING: INPUT waits for a value, WRITE for an uninitialized variable item), a cursor pulses between black (#000000) and a darker green (#006600), a debug cursor between black and a darker red (#660000).

## Movement

There are two kinds of cursors: a regular cursor and a debug cursor. The regular cursor moves automatically, the debug cursor is controlled by a player with a debugging tool.

  - A regular cursor moves to the next line every tick, if it is allowed to. If the steps of all processes take longer than the `tickBudget` of the configuration (20 ms), the remaining cursors wait for the next tick, in which they go first; measured, a step of 100 processes takes about 0.6 ms (task 11.10).
  - A debug cursor starts in the PAUSED state. Shift-left-clicking it with a debugging tool switches it to the RUNNING state, in which it moves every tick like a regular cursor; shift-left-clicking it again pauses it. Left-clicking a paused debug cursor with a debugging tool moves it by one line.
  - Right-clicking any cursor with a debugging tool opens a dialog that shows its properties, how many slots of its stack are used, and the content of its stack.

When a cursor arrives on a new line, it reads all blocks of the line (and of its continuation lines) and processes them. It is not allowed to move on before the processing is complete.

Usually, the cursor moves to the line directly after the statement it has processed. NOOP lines, comment lines, continuation lines and the content lines of declarations (struct elements, initializers, PARAMETER and RESULT lines) are not processed on their own: the cursor moves over them in the same step. A STRUCT, VARIABLE or CONSTANT statement with its code block and a FUNCTION statement with its PARAMETER and RESULT lines are processed in one step. Some statements make the cursor jump: when the cursor skips a function or a code block, follows a CALL or returns from a function, or when a loop is repeated, it moves directly to the target line in one step.

## Properties

Some standard Minecraft properties of the cursor entity carry relevant data:
  - the PID is its y coordinate
  - the line it is processing is its z coordinate

The cursor has the following specific properties:
  - the dimension and coordinates of the trigger block that started the process
  - the name of the program
  - the name of the entry function
  - the name of the function it is currently in
  - a list of available functions
  - an associative array of available types
  - an associative array of available library constants
  - a stack with 2048 slots, each of which can hold one stack item. A larger stack can be configured in the mod settings. The stack size is the only limit for the depth of nested function calls and recursion.
  - its state: RUNNING, WAITING (a CALL of INPUT or WRITE waits for a value or an item; the cursor pulses from black), PAUSED (debug cursors only) or CRASHED
  - the last keyword it processed

## The stack

The stack holds the following stack items:
  - **variable items** and **constant items** for every declared variable, constant, parameter, result and loop variable
  - **function items** (call frames) for every function that is currently running. Function items exist only on the stack, they are not Minecraft items. The cursor pushes a function item whenever it enters a function. When it leaves the function, it copies the result values to the variables of the calling function and removes everything from the stack down to and including the function item. A function item holds the name of the function, the line of its FUNCTION statement, the line the cursor returns to, the mapping of the results to the variables of the caller and the error label of the CALL statement.
  - **block markers** for every open code block. A block marker holds the keyword of the statement that opened the code block, the lines of its BEGIN and END statements and, for loops, the loop state (end value, increment, remaining elements).

When the cursor resolves a name, it searches the stack from the top down to the topmost function item, then the global variables and constants that were declared before the FUNCTION statement of the current function, then the library constants and the built-in names. If the name is not found, the cursor raises UNKNOWN_NAME. Pushing an item onto a full stack crashes the cursor with STACK_OVERFLOW.

## Error handling

Errors are either *raised* or make the cursor *crash*.

A **crash** ends the process immediately (see [Ending a process](13-running-programs.md#ending-a-process)). The cursor state is set to CRASHED. All syntax errors and the errors marked with "crash" in the [error code table](16-error-codes.md) cause a crash.

A **raised** error ends the current function as if a RETURN statement with that error had been executed. The `context` of the error is the name of the current function and its `line` is the z coordinate of the line in which it was raised. The error is passed to the caller:
  - If the CALL statement has an error label, the error is stored in that variable and the caller continues normally. The error label always receives the error returned by the function, which has the code 0 if there was no error.
  - Otherwise, the error is raised again in the caller. It keeps its `context` and `line`, so they always describe where the error occurred first.
  - If the error is raised in the entry function, the cursor crashes with that error.

A function raises a user-defined error by assigning a code of 1000 or higher to a variable of type ERROR and returning it with RETURN.

## Processing statements

### When a cursor is created

When a cursor is created, the list of available functions is initialized with the built-in functions, the list of types with the built-in types and the list of constants with the [built-in constants](04-data.md#built-in-constants) (the names of all [error codes](16-error-codes.md), the colors, SHEET and PID with the values of the process, and CURSOR, which the cursor computes whenever it is read). The PROGRAM statement has already been validated by the trigger block, so the cursor starts in the line directly after it, which MUST contain a NOOP statement.

### When a cursor reaches a new line

When the cursor reads a line, it stops at the first COMMENT block: the COMMENT block and all blocks after it are ignored for the rest of the processing. A line that starts with a COMMENT block is skipped (see [Skipping NOOP and comment lines](#skipping-noop-and-comment-lines)).

The cursor then checks the indentation of the line according to the rules in [Lines, statements and indentation](02-language-basics.md):
  - Lines beginning with PROGRAM, STRUCT or FUNCTION MUST have their keyword block at x=5. Global VARIABLE and CONSTANT lines (outside of functions) MUST also have their keyword block at x=5.
  - INCLUDE lines MUST have their keyword block at x=5.
  - PARAMETER and RESULT blocks MUST have an x coordinate that is one greater than the x coordinate of the FUNCTION or CALL block they belong to.
  - BEGIN blocks MUST always be at the same x coordinate as the keyword block of the statement that requires them.
  - The keyword blocks of all lines between a BEGIN line and its matching END line MUST have an x coordinate that is one greater than the x coordinate of the BEGIN block, unless they are continuation lines or belong to a nested code block.
  - END blocks MUST always be at the same x coordinate as the matching BEGIN block.

If the cursor encounters a wrong indentation, it crashes with SYNTAX_ERROR_INDENTATION.

The cursor then checks whether the statement is allowed at that position:
  - INCLUDE lines MUST come before any STRUCT, VARIABLE, CONSTANT or FUNCTION line.
  - PARAMETER lines are only allowed directly after a FUNCTION line, a CALL line or another PARAMETER line.
  - RESULT lines are only allowed directly after a FUNCTION line, a CALL line, a PARAMETER line or another RESULT line.
  - An END block MUST NOT appear without a matching BEGIN block before it.
  - A BEGIN block MUST only appear as part of a statement that requires a code block.
  - An ELSE line MUST only appear directly after the standalone END line of the first code block of an IF statement.
  - Lines that start with FUNCTION, STRUCT or INCLUDE MUST NOT be placed inside a code block.
  - Lines that start with CALL, ASSIGN, IF, ELSE, FOR, EACH, WHILE, REPEAT, BREAK, RETURN or EXIT MUST be placed inside the code block of a function.
  - BREAK lines MUST be placed inside a loop of the current function.

If a line violates one of these rules, the cursor crashes with SYNTAX_ERROR_PLACEMENT. If the first block of a line is not a keyword block and the line is neither a continuation line nor a content line of a struct declaration or initializer, the cursor crashes with SYNTAX_ERROR_UNKNOWN_STATEMENT.

When the cursor encounters a BEGIN statement, it scans ahead for the matching END statement, which is the first END block at the same x coordinate. If there is none or the END statement is not followed by the keyword block it requires, the cursor crashes with SYNTAX_ERROR_BEGIN_WITHOUT_END.

### Skipping NOOP and comment lines

When a cursor reaches a line that starts with a NOOP block or a COMMENT block, the line is skipped as if it were not there. If a NOOP block is followed by any other block (including a COMMENT block), the cursor crashes with SYNTAX_ERROR_PLACEMENT. This check is made before COMMENT blocks are removed from the line.

### When a cursor reaches an empty line

The program ends at the first empty line. If the cursor reaches it, the entry function has not been executed, and the cursor crashes with UNKNOWN_FUNCTION.

### When a cursor reaches an INCLUDE statement

When a cursor reaches an INCLUDE statement, it reads the LABEL next to it and tries to read the elements provided by that library. If the label is missing or not initialized, the cursor crashes with SYNTAX_ERROR_PLACEMENT. If the library is not available, it crashes with UNKNOWN_LIBRARY.

It first reads the list of types provided by the library. The list MAY be empty but MUST be defined. The types are added to the types known by the cursor with names in the format `mod/library/type`. If the cursor can't read that list, it crashes with BROKEN_LIBRARY.

It then reads the list of constants provided by the library. The list MAY be empty but MUST be defined. The constants are added to the constants known by the cursor with names in the format `mod/library/constant`. If the cursor can't read that list, it crashes with BROKEN_LIBRARY.

As a last step, it reads the list of functions provided by the library. The list MAY be empty but MUST be defined. The functions are added to the functions known by the cursor with names in the format `mod/library/function`. If the cursor can't read that list, it crashes with BROKEN_LIBRARY.

### When a cursor reaches a STRUCT statement

The cursor starts a new type definition in its list of types. The name of the type is read from the LABEL directly following the STRUCT keyword. If the name is not valid or not unique, or any other block follows the label, the cursor crashes with SYNTAX_ERROR_STRUCT.

The cursor then moves to the next line. If that line does not contain a BEGIN block at the x coordinate of the STRUCT keyword (x=5), the cursor crashes with SYNTAX_ERROR_STRUCT.

It then reads the lines following the BEGIN line and adds an element to the type definition for each of them. It expects a LABEL with the name of the element at x=6, directly followed by a type block at x=7. If these requirements are not met or the element name is used twice, the cursor crashes with SYNTAX_ERROR_STRUCT. If the type is ARRAY or HASH, a subtype is expected at x=8. If the type (or subtype) is STRUCT, a LABEL containing the name of an already known struct is expected directly after it. If that label is missing or not initialized, the cursor crashes with SYNTAX_ERROR_STRUCT. If the referenced struct is not known, the cursor crashes with UNKNOWN_STRUCT_ENTITY.

After the type, zero or more TAG blocks MAY follow. Every tag MUST have a language and a name that is not empty and contains no dot, and there MUST NOT be two tags for the same language in one line. The tags are stored with the element in the type definition. If a tag is invalid or any other block follows, the cursor crashes with SYNTAX_ERROR_STRUCT.

When the cursor reaches the line containing the END keyword, it expects the END block at the same x coordinate as the BEGIN block, directly followed by a STRUCT block. If the line is formatted incorrectly, the cursor crashes with SYNTAX_ERROR_STRUCT. The struct type is now known and the cursor moves on to the next line.

### When a cursor reaches a VARIABLE statement

When a cursor reaches a line starting with a VARIABLE block, it creates a new variable item. It reads the LABEL directly following the VARIABLE block to set its name property. If there is no LABEL directly following the VARIABLE block or the name is invalid, not unique or not initialized, the cursor crashes with SYNTAX_ERROR_VARIABLE.

The LABEL MUST be directly followed by a type block, which sets the type property of the variable item. If the type is missing, the cursor crashes with SYNTAX_ERROR_VARIABLE.

If the type is ARRAY or HASH, another type block MUST directly follow the first one. It sets the subtype of the array or hash. If the subtype is missing, the cursor crashes with SYNTAX_ERROR_VARIABLE. If the type or subtype is STRUCT, a LABEL with the name of a known struct MUST directly follow; if it is missing, the cursor crashes with SYNTAX_ERROR_VARIABLE, if the struct is unknown, with UNKNOWN_STRUCT_ENTITY.

If the type of the variable is STRING, an INTEGER LITERAL MAY directly follow the type block to limit its length.

If the type of the variable is a simple type or STRING, a LITERAL of that type MAY follow. If it exists, its value initializes the variable. If the literal has a different type, the cursor crashes with SYNTAX_ERROR_TYPE_MISMATCH. If a STRING literal exceeds the length limit, the cursor crashes with STRING_TOO_LONG. Without a literal, the variable is initialized with the default value of its type.

After the type (and the optional literal), zero or more TAG blocks MAY follow. Every tag MUST have a language and a path of one or more non-empty keys separated by dots, and there MUST NOT be two tags for the same language. The tags are stored in the `tags` property of the variable item. If a tag is invalid or any other block follows, the cursor crashes with SYNTAX_ERROR_VARIABLE.

If the type of the variable is ARRAY, HASH or STRUCT, the next line MAY contain a BEGIN block at the x coordinate of the VARIABLE block. In that case, the value of the variable is initialized with the data defined in the lines between the BEGIN and the matching `END VARIABLE` statement, as described in [Initializing complex values](04-data.md#initializing-complex-values):
  - For an array, every line MAY contain one or more LITERAL blocks, which are read from left to right and appended to the array.
  - For a hash, every line MUST contain exactly one LABEL specifying the key, directly followed by a LITERAL specifying the value. A corresponding element is added to the hash. If the key is used twice, the cursor crashes with SYNTAX_ERROR_VARIABLE.
  - For a struct, every line MUST contain exactly one LABEL with the name of an element of the struct type, directly followed by a LITERAL with its value. If the struct type has no element with that name, the cursor crashes with UNKNOWN_STRUCT_ENTITY.
  - Nested initializers are processed recursively.

If a literal is not initialized or its type does not match the expected type, the cursor crashes with SYNTAX_ERROR_TYPE_MISMATCH. If the content lines do not follow these rules, it crashes with SYNTAX_ERROR_VARIABLE.

If the variable item was created successfully, it is pushed onto the stack. The cursor moves on to the line after the VARIABLE line or, if there was an initializer, after its END line.

### When a cursor reaches a CONSTANT statement

A CONSTANT statement is processed like a VARIABLE statement, with the following differences:
  - The cursor creates a constant item instead of a variable item.
  - Formatting errors raise SYNTAX_ERROR_CONSTANT instead of SYNTAX_ERROR_VARIABLE.
  - A constant of a simple type or STRING MUST be initialized by a LITERAL. If the literal is missing, the cursor crashes with SYNTAX_ERROR_CONSTANT.
  - A constant of type ARRAY, HASH or STRUCT MUST be initialized by a code block ending with `END CONSTANT`. If the code block is missing, the cursor crashes with SYNTAX_ERROR_CONSTANT.

### When a cursor reaches a FUNCTION statement

When the cursor reaches a line starting with a FUNCTION block, it reads the function name from the LABEL block directly following it. If the LABEL is missing, not initialized or contains an invalid or non-unique name, the cursor crashes with SYNTAX_ERROR_FUNCTION.

The cursor creates a new entry in its list of functions and initializes it with the name of the function and the line of the FUNCTION statement.

The cursor then reads the following lines until it encounters the BEGIN block and records the parameters (PARAMETER lines) and results (RESULT lines) in that entry. If a parameter or result has no LABEL, the LABEL is not initialized or the name is used twice in this function, the cursor crashes with SYNTAX_ERROR_FUNCTION. The LABEL MUST be followed by a type (and, if required, a subtype and struct name). If the type is missing, the cursor crashes with SYNTAX_ERROR_FUNCTION. If a default value does not match the type of the parameter, the cursor crashes with SYNTAX_ERROR_TYPE_MISMATCH.

When the BEGIN block of the function is reached, the cursor checks whether the function is the entry function. If it is not, the cursor jumps to the line after the matching `END FUNCTION` line. The function is now known and can be called by functions declared after it.

If it is the entry function, the cursor
  1. pushes a function item onto the stack. The function item has no return line: when the function ends, the process ends.
  2. pushes a block marker for the code block of the function.
  3. takes one variable or constant item for every parameter from the input inventory of the trigger block, matched by name, and pushes it onto the stack. Constant items are converted to variable items. If there is no item for a parameter that has no default value, the cursor crashes with FUNCTION_MISSING_INPUT. If an item has a different type than its parameter, the cursor crashes with TYPE_MISMATCH. Parameters with a default value and without a matching item are initialized with their default value.
  4. pushes one variable item, initialized with the default value of its type, for every result. If the function has more results than parameters, one empty variable item per missing item is consumed from the input inventory. If there are not enough empty variable items, the cursor crashes with FUNCTION_MISSING_INPUT.
  5. passes all other initialized items through to the output inventory; empty items that are not needed stay in the input inventory.
  6. moves on to the first line of the code block.

If the stack is full when pushing an item, the cursor crashes with STACK_OVERFLOW.

### When a cursor reaches a BEGIN statement

A BEGIN statement is usually processed as part of the statement that requires it. When the cursor enters a code block, it pushes a block marker onto the stack and moves on to the line directly after the BEGIN line.

### When a cursor reaches an END statement

When the cursor reaches an END statement, it pops all items from the stack down to and including the block marker of the code block, which removes all variables and constants declared inside the block. What happens next depends on the keyword that follows the END block:
  - **IF (or a standalone END of an IF code block)**: the cursor moves on to the line after the end of the whole IF statement, i.e. it skips the ELSE part if there is one.
  - **FOR, EACH, WHILE, REPEAT**: see the respective loop statement.
  - **FUNCTION**: the function ends, see [Returning from a function](#when-a-cursor-returns-from-a-function).

The END lines of STRUCT, VARIABLE and CONSTANT statements are processed as part of these statements.

### When a cursor reaches an ASSIGN statement

The cursor reads the LABEL directly following the ASSIGN block. If it is missing or not initialized, the cursor crashes with SYNTAX_ERROR_ASSIGN. It resolves the label to a variable; if the name can't be found, it raises UNKNOWN_NAME. If the label refers to a constant or a loop variable, the cursor crashes with SYNTAX_ERROR_ASSIGN.

The cursor then evaluates the expression following the label (see [Evaluating expressions](#evaluating-expressions)). If the value does not match the type of the target, it raises TYPE_MISMATCH.

If the label has an index, the cursor evaluates the index entries from left to right:
  - An INTEGER index into an array MUST be between 0 and the length of the array. If it equals the length, a new element is appended. Otherwise, the cursor raises INDEX_OUT_OF_BOUNDS.
  - A STRING index into a hash sets or adds the element with that key.
  - An element name of a struct or error MUST exist in the type. Otherwise, the cursor raises UNKNOWN_STRUCT_ENTITY.
  - Strings can't be changed through an index. The cursor raises TYPE_MISMATCH.

If the target is a STRING with a length limit and the new value is longer, the cursor raises STRING_TOO_LONG.

Finally, the cursor stores a copy of the value in the variable item and moves on to the line after the statement.

### When a cursor reaches a CALL statement

The cursor reads the LABEL directly following the CALL block and looks up the function in its list of functions. If the label is missing or not initialized, the cursor crashes with SYNTAX_ERROR_CALL. If the function is not known (yet), it crashes with UNKNOWN_FUNCTION. If a second LABEL follows, it MUST refer to a variable of type ERROR; otherwise the cursor crashes with SYNTAX_ERROR_CALL.

The cursor then reads the PARAMETER and RESULT continuation lines. If a line refers to a parameter or result that the function does not have, or refers to it twice, the cursor crashes with SYNTAX_ERROR_CALL. If a parameter without a default value is not provided, it crashes with FUNCTION_MISSING_INPUT. If the target of a RESULT line is a constant or a loop variable, it crashes with SYNTAX_ERROR_CALL.

The cursor evaluates the parameter expressions from top to bottom in the scope of the caller. If a value does not match the type of the parameter, it raises TYPE_MISMATCH. If the expression of a PARAMETER line consists of a single LABEL without index, the cursor passes the tags of the referenced variable or constant along with the value. Only the [JSON and YAML functions](10-lib-strings.md#json-and-yaml-functions) use them. The `destination` parameter of FROM_JSON and FROM_YAML MUST be a single LABEL without index that refers to a variable; otherwise the cursor crashes with SYNTAX_ERROR_CALL. The cursor passes a reference to that variable instead of a copy of its value, and the function writes the value read from the document directly into the variable.

For a function of the program, the cursor then
  1. pushes a function item that holds the return line (the line after the last continuation line of the CALL statement), the mapping of the results to the target variables and the error label,
  2. pushes a variable item for every parameter (with the provided or default value) and for every result (with the default value of its type),
  3. jumps to the BEGIN line of the called function, pushes a block marker for its code block and moves on to the first line of the code block.

[Built-in](08-builtin-functions.md#built-in-functions) and library functions are executed directly by the mod. They receive the parameter values and return result values and an error the same way as program functions. SLEEP keeps the cursor on the CALL line for the requested number of ticks before it continues. Afterwards, the cursor continues as described in [Returning from a function](#when-a-cursor-returns-from-a-function).

If the stack is full when pushing an item, the cursor crashes with STACK_OVERFLOW.

### When a cursor reaches a RETURN statement

If a LABEL follows the RETURN block, the cursor resolves it. It MUST refer to a variable or constant of type ERROR; otherwise the cursor crashes with SYNTAX_ERROR_FUNCTION. Its value is the error returned by the function. Without a label, the function returns an error with code 0. Any other block after the RETURN block makes the cursor crash with SYNTAX_ERROR_FUNCTION.

The cursor then returns from the function.

### When a cursor returns from a function

A function returns when the cursor reaches its `END FUNCTION` line, executes a RETURN statement or an error is raised in it. The cursor
  1. reads the current values of all result variables of the function,
  2. pops all items from the stack down to and including the function item, which also closes all open code blocks of the function,
  3. if the function is the entry function, ends the process: the result values are written to variable items in the output inventory of the trigger block (see [Exchanging data with a process](13-running-programs.md#exchanging-data-with-a-process)). If the returned error has a code other than 0, the cursor crashes with that error instead.
  4. otherwise, assigns the result values to the target variables of the RESULT lines of the CALL statement (raising TYPE_MISMATCH if a type does not match) and handles the returned error as described in [Error handling](#error-handling),
  5. moves to the return line.

### When a cursor reaches an EXIT statement

The cursor evaluates the expression following the EXIT block. If it is missing, the cursor crashes with SYNTAX_ERROR_EXIT. If the value is not an INTEGER between 0 and 15, the cursor crashes with INVALID_EXIT_CODE.

The process then ends with this exit code. The current values of the results of the entry function are written to the output inventory of the trigger block as if the entry function had returned normally.

### When a cursor reaches a BREAK statement

If an expression follows the BREAK block, the cursor evaluates it. If its value is not a BOOLEAN, the cursor raises TYPE_MISMATCH. If it is false, the cursor moves on to the next line.

Otherwise, the cursor pops all items from the stack down to and including the block marker of the innermost loop, which also closes all code blocks nested in the loop and removes the loop variable. It then moves on to the line after the END line of that loop.

### When a cursor reaches an IF statement

The cursor evaluates the expression following the IF block. If it is missing, the cursor crashes with SYNTAX_ERROR_CONDITION. If its value is not a BOOLEAN, the cursor raises TYPE_MISMATCH.

The cursor then checks the structure of the condition: the next line MUST be a BEGIN line. Its matching END is either followed by an IF block, or it stands alone and the next line is an ELSE line, followed by a BEGIN line whose matching END is followed by an IF block. Otherwise, the cursor crashes with SYNTAX_ERROR_CONDITION.

  - If the value is true, the cursor enters the first code block.
  - If the value is false and there is an ELSE part, the cursor jumps to the BEGIN line after the ELSE line and enters the second code block.
  - If the value is false and there is no ELSE part, the cursor jumps to the line after the `END IF` line.

### When a cursor reaches an ELSE statement

An ELSE line is never reached by moving on from the line before it, because the end of the first code block of an IF statement jumps to the end of the whole statement. If the cursor reaches an ELSE line in any other way, it crashes with SYNTAX_ERROR_PLACEMENT.

### When a cursor reaches a FOR statement

The cursor reads the LABEL directly following the FOR block, which defines the loop variable. If it is missing, not initialized or not unique, the cursor crashes with SYNTAX_ERROR_LOOP. The label MUST be directly followed by a TYPE_INTEGER or TYPE_FLOAT block; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

It then evaluates the start, end and optional increment expressions. If there are fewer than two or more than three expressions, the cursor crashes with SYNTAX_ERROR_LOOP. If a value is not numeric or a FLOAT value is used with an INTEGER loop variable, it raises TYPE_MISMATCH. If the increment is 0, it raises INVALID_LOOP_STEP.

The next line MUST be a BEGIN line whose matching END is followed by a FOR block; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

The cursor pushes a block marker holding the end value and the increment and the loop variable initialized with the start value. It then checks the loop condition (loop variable ≤ end value for a positive increment, loop variable ≥ end value for a negative increment):
  - If it is true, the cursor pushes a block marker for the code block and moves on to the first line of the code block.
  - If it is false, the cursor pops the loop variable and the loop marker and jumps to the line after the `END FOR` line.

When the cursor reaches the `END FOR` line, it closes the code block, adds the increment to the loop variable and checks the loop condition again. If it is true, it jumps back into the code block (to the line after the BEGIN line, pushing a new block marker). If it is false, it pops the loop variable and the loop marker and moves on to the line after the `END FOR` line.

### When a cursor reaches an EACH statement

The cursor reads the LABEL directly following the EACH block, which defines the loop variable. If it is missing, not initialized or not unique, the cursor crashes with SYNTAX_ERROR_LOOP. It then reads the second LABEL and resolves it. If it is missing, the cursor crashes with SYNTAX_ERROR_LOOP. If the name can't be found, it raises UNKNOWN_NAME. If the value is neither an ARRAY nor a HASH, it raises TYPE_MISMATCH.

The next line MUST be a BEGIN line whose matching END is followed by an EACH block; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

The cursor pushes a block marker holding a copy of the elements of an array or the sorted keys of a hash. If there are no elements, it pops the marker and jumps to the line after the `END EACH` line. Otherwise, it pushes the loop variable initialized with the first element or key, pushes a block marker for the code block and moves on to the first line of the code block.

When the cursor reaches the `END EACH` line, it closes the code block. If there are more elements, it sets the loop variable to the next one and jumps back into the code block. Otherwise, it pops the loop variable and the loop marker and moves on to the line after the `END EACH` line.

### When a cursor reaches a WHILE statement

The cursor evaluates the expression following the WHILE block. If it is missing, the cursor crashes with SYNTAX_ERROR_LOOP. If the value is not a BOOLEAN, it raises TYPE_MISMATCH.

The next line MUST be a BEGIN line whose matching END is followed by a WHILE block; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

  - If the value is true, the cursor pushes a block marker for the loop and moves on to the first line of the code block.
  - If it is false, the cursor jumps to the line after the `END WHILE` line.

When the cursor reaches the `END WHILE` line, it closes the code block and jumps back to the WHILE line, which is processed again.

### When a cursor reaches a REPEAT statement

If any block follows the REPEAT block, the cursor crashes with SYNTAX_ERROR_LOOP. The next line MUST be a BEGIN line whose matching END is followed by a REPEAT block and an expression; otherwise the cursor crashes with SYNTAX_ERROR_LOOP.

The cursor pushes a block marker for the loop and moves on to the first line of the code block.

When the cursor reaches the `END REPEAT` line, it closes the code block and evaluates the expression following the REPEAT block. If the value is not a BOOLEAN, it raises TYPE_MISMATCH.
  - If the value is true, the cursor pushes a new block marker and jumps back to the first line of the code block.
  - If it is false, the cursor moves on to the line after the `END REPEAT` line.

### Evaluating expressions

To evaluate an expression, the cursor reads the blocks from left to right, continuing on continuation lines, until it reaches an OPERATOR block with the symbol `;`. If there is no such block, the cursor crashes with SYNTAX_ERROR_EXPRESSION.

  - A LITERAL block provides its value. If it is not initialized, the cursor crashes with SYNTAX_ERROR_EXPRESSION.
  - A LABEL block is resolved to a variable or constant (raising UNKNOWN_NAME if it can't be found), then its index entries are applied from left to right. An array index outside of the array raises INDEX_OUT_OF_BOUNDS, a missing hash key raises UNKNOWN_KEY, an unknown struct element raises UNKNOWN_STRUCT_ENTITY.
  - OPERATOR blocks are processed according to the precedence of their symbols (e.g. with the shunting-yard algorithm). An OPERATOR block without a symbol, unbalanced parentheses, more than 8 levels of parentheses, missing operands and two operands without an operator between them make the cursor crash with SYNTAX_ERROR_EXPRESSION.
  - Operands of the wrong type raise TYPE_MISMATCH, a division or modulus by 0 raises DIVISION_BY_ZERO, an INTEGER result that does not fit into 64 bits raises NUMERIC_OVERFLOW.
