# Error codes

[c0de concept](../concept.md) › Error codes

| Code | Name | Handling | Description |
|---:|---|---|---|
| 0 | OK | – | No error. |
| 100 | SYNTAX_ERROR_INDENTATION | crash | A line has a wrong indentation. |
| 101 | SYNTAX_ERROR_PLACEMENT | crash | A line is not allowed in this context or code block. |
| 102 | SYNTAX_ERROR_BEGIN_WITHOUT_END | crash | A BEGIN line has no matching END line, or the END line is not followed by the required keyword. |
| 103 | SYNTAX_ERROR_UNKNOWN_STATEMENT | crash | A line starts with a block that is not a keyword. |
| 104 | SYNTAX_ERROR_STRUCT | crash | A part of a struct declaration is wrong. |
| 105 | SYNTAX_ERROR_VARIABLE | crash | A part of a variable declaration is wrong. |
| 106 | SYNTAX_ERROR_CONSTANT | crash | A part of a constant declaration is wrong. |
| 107 | SYNTAX_ERROR_TYPE_MISMATCH | crash | A constant, variable or parameter is initialized with a literal that does not match its type. |
| 108 | SYNTAX_ERROR_FUNCTION | crash | A part of a function declaration or a RETURN statement is wrong. |
| 109 | SYNTAX_ERROR_CALL | crash | A part of a CALL statement is wrong. |
| 110 | SYNTAX_ERROR_ASSIGN | crash | A part of an ASSIGN statement is wrong, or it assigns to a constant or loop variable. |
| 111 | SYNTAX_ERROR_CONDITION | crash | A part of an IF statement is wrong. |
| 112 | SYNTAX_ERROR_LOOP | crash | A part of a FOR, EACH, WHILE or REPEAT statement is wrong. |
| 113 | SYNTAX_ERROR_EXPRESSION | crash | An expression is malformed. |
| 114 | SYNTAX_ERROR_EXIT | crash | An EXIT statement has no expression. |
| 200 | UNKNOWN_LIBRARY | crash | An included library does not exist. |
| 201 | BROKEN_LIBRARY | crash | A library could not be read or does not contain the required elements. |
| 202 | UNKNOWN_FUNCTION | crash | A called function or the entry function is not known (yet). |
| 203 | STACK_OVERFLOW | crash | The stack of the cursor is full and a new item was pushed onto it. |
| 204 | FUNCTION_MISSING_INPUT | crash | A function has been called and not all mandatory parameters have been provided, or the trigger block did not provide enough empty variable items for the results. |
| 205 | INVALID_EXIT_CODE | crash | The value of an EXIT expression is not an INTEGER between 0 and 15. |
| 206 | PROCESS_ABORTED | crash | A player aborted the process with the debugging tool (shift-right-click on its PID block). |
| 300 | UNKNOWN_NAME | raise | A label refers to a variable or constant that is not in scope. |
| 301 | UNKNOWN_STRUCT_ENTITY | raise | A declaration references a struct that has not been declared (yet), or an index references an element the struct does not have. Raised when it occurs at run time, crash when it occurs in a declaration. |
| 302 | TYPE_MISMATCH | raise | A value does not have the type required at this place. |
| 303 | DIVISION_BY_ZERO | raise | A division or modulus by 0. |
| 304 | NUMERIC_OVERFLOW | raise | An INTEGER result does not fit into 64 bits. |
| 305 | INDEX_OUT_OF_BOUNDS | raise | An array or string index is outside of the valid range. |
| 306 | UNKNOWN_KEY | raise | A hash does not contain the requested key. |
| 307 | STRING_TOO_LONG | raise | A value exceeds the length limit of a STRING. Crash when it occurs in a declaration. |
| 308 | INVALID_LOOP_STEP | raise | The increment of a FOR loop is 0. |
| 309 | INVALID_ARGUMENT | raise | A built-in or library function received a parameter value outside of its valid range. |
| 310 | CHUNK_NOT_LOADED | raise | A library function accessed a position in a chunk that is not loaded. |
| 311 | PERMISSION_DENIED | raise | A library function is not permitted for the owner of the trigger block. |
| ≥1000 | user-defined | raise | Errors raised by programs. |
