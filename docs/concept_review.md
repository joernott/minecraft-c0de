# Concept review (2026-10-18)

This file lists the content changes made to [concept.md](concept.md) during the review and the questions that are still open. Spelling and grammar fixes are not listed. Every decision below is a proposal. Revert it in the concept if it does not match your intent.

## 1. Contradictions and errors that were fixed

| # | Problem in the original | Resolution |
|---|---|---|
| 1 | The PID range was given as "-0 to 1023", but the PID equals the y coordinate, which goes up to the build height (up to 2052). | PID = y coordinate, from 0 to the maximum build height. Unique per program, globally identified by program name + PID. |
| 2 | Build height "5 to 2052, default 260" with the floor at y=-4. Minecraft requires the bottom y and the height of a dimension to be multiples of 16. | The dimension starts at y=-16, the maximum build height is configurable as 16·k−1 between 15 and 2031 (default 255). |
| 3 | A program had to be followed by "a single empty line", but a program also ends at the first empty line. Functions needed "an empty line above". | The separator is a NOOP line. An *empty line* (only air) is now defined as the end of a program. |
| 4 | FUNCTION blocks could only be placed at "x=3", but code starts at x=5. | x=5. |
| 5 | Struct elements were expected at x=7/8/9, but the indentation rules give x=6/7/8. | x=6 (label), x=7 (type), x=8 (subtype or struct name). |
| 6 | INCLUDE was "indented by one block" (x=6) in one place and "at x=5" in the cursor rules. | x=6 everywhere. |
| 7 | "Chunk relative" and "sheet relative" x coordinates were mixed. | Everything is relative to the sheet (absolute x mod 32). |
| 8 | The program blocks' y level was never defined. | Code layer y=-3, directly on top of the floor. |
| 9 | LABEL and INCLUDE had the same recipe (NOOP + paper). | INCLUDE is now NOOP + empty map, with a map texture. |
| 10 | "Yellow sand" and "orange sand" are not vanilla items, but all blocks must be craftable from vanilla materials. | Crafted from sand and red sand. The texture still shows yellow/orange sand. |
| 11 | Wrong recipe results: EXIT produced `begin_block`, RETURN produced `parameter`, wrench and hammer produced `debug_tool`, typo `redstrone_block`. | Fixed to `exit_block`, `return_block`, `wrench`, `hammer`, `redstone_block`. IDs are now consistently `*_block` (`parameter_block`, `literal_block`). |
| 12 | Text and recipe disagreed on the count: EXIT and PARAMETER "sixteen" vs. `"count": 8`. | 8, like all other "eight NOOPs around X" recipes. |
| 13 | The CONSTANT block section described a VARIABLE block. The RETURN block talked about a "result line". The WHILE/REPEAT/EACH blocks talked about a "for statement". | Copy-paste errors fixed. |
| 14 | The function item recipe was a copy of the literal item recipe. | Removed: function items exist only on the stack (answered question 9). |
| 15 | The label dialog was opened by "right click" in one place and "shift right click" in another. | Right-click with an empty hand. |
| 16 | A LABEL "MUST directly follow" only FUNCTION, VARIABLE, CONSTANT, TYPE or CALL, but labels are also used after PROGRAM, INCLUDE, STRUCT, PARAMETER, RESULT, ASSIGN, FOR, EACH, RETURN, END FUNCTION and in expressions. The `kind` property was used but not defined. | List completed, `kind` defined. |
| 17 | Library names (`c0de/net`) violate the label naming rules. | Slash-separated names are allowed for libraries and qualified library elements. |
| 18 | The END rule "MUST be followed by the keyword that required the block" conflicts with IF/ELSE, where only the last END is followed by IF. | The END of the IF branch stands alone when an ELSE follows. ELSE is a line of its own. |
| 19 | The BEGIN placement list missed FOR, EACH, STRUCT and the VARIABLE/CONSTANT initializers. | Completed. Initializers end with `END VARIABLE` / `END CONSTANT`. |
| 20 | Precedence list: number 7 was missing, logical XOR was missing, there was no bitwise XOR, and assignment was listed as an operator. | Renumbered, XOR added (logical) and ⊻ added (bitwise). Assignment is only a statement. |
| 21 | A STRING type MAY be followed by a literal that limits its length, and a VARIABLE type MAY be followed by a literal that initializes it. The two were ambiguous. | Literals carry their type: an INTEGER literal is the limit, a STRING literal is the value. |
| 22 | A literal was described as "LITERAL keyword followed by a value and TYPE". Elsewhere it is used like a single block. | A LITERAL block holds type and value as properties (like the literal item). A dialog was added to set them. |
| 23 | STRUCT: "There MUST NOT be another keyword after STRUCT", but the struct name follows it, and STRUCT as a type needs a label too. | Two uses described separately: declaring and using a struct type. |
| 24 | EACH: the label dialog shows an input field for a new name, but the statement only names the iterable. Without a loop variable the elements can't be accessed. | `EACH <loop variable> <iterable>`. |
| 25 | Parameters were fetched "when the cursor is created" in one section and "at the function's BEGIN" in another. | At the BEGIN of the entry function. |
| 26 | "Constants are added to the array of types" (INCLUDE). "Output directory" (FUNCTION). | Array of constants. Output inventory. |
| 27 | Triggers "can be placed in all dimensions" vs. "in other dimensions than the c0de dimension". | Any dimension; in the c0de dimension only in the negative area (answered question 4). |
| 28 | "Chunks with negative x **and** z" are black. That leaves chunks with only one negative coordinate undefined. | Negative x **or** z. |
| 29 | Indentation rule: "all *blocks* after BEGIN must have x+1". | Applies to the *keyword blocks of lines*, except continuation lines and nested blocks. |
| 30 | "UTF-8 character" for CHAR. | Unicode code point (UTF-8 is an encoding, Java chars are UTF-16). |

## 2. Gaps that were filled (new design decisions)

- **Glossary**: added sheet, relative x, code layer, line, empty line, keyword block, continuation line and scope. A line holds 24 blocks (x=5..28).
- **NOOP comments out a line**: a line starting with NOOP is skipped entirely. That follows from the original rule and is now stated explicitly.
- **Code blocks** MUST contain at least one statement. This follows from "END MUST NOT be placed directly after BEGIN".
- **BREAK** ends the innermost *loop*, not just any code block. A BREAK inside an IF would otherwise only leave the IF block, which makes the built-in condition pointless.
- **REPEAT** repeats *while* its condition is true (same polarity as WHILE, i.e. do-while, not Pascal's REPEAT … UNTIL).
- **FOR**: the type is INTEGER or FLOAT, start/end/increment are evaluated once, the end is inclusive, an increment of 0 is an error, and the loop variable is read-only and scoped to the loop.
- **EACH**: arrays yield values in index order, hashes yield keys in sorted order. Iteration runs over a copy.
- **Expressions**: the `;` block was required but did not exist. It is now an operator-style block. Multi-line expressions continue on continuation lines indented by one block. Type rules were added (int/float promotion, `/` always returns FLOAT, `\` integers only, ⊕ converts to text). Function calls are not allowed inside expressions.
- **Label index**: defined as up to 8 entries, each a literal or the name of a variable. Written as `a[i][2]` / `p.x`.
- **ASSIGN**: copy semantics, INTEGER→FLOAT and CHAR→STRING widening, append by assigning index = length, add hash keys by assignment.
- **CALL**: parameter lines are `PARAMETER <param name> <expression> ;` and result lines are `RESULT <result name> <target variable>`. The original only had "PARAMETER followed by a variable/literal and its TYPE", which can't map values to parameters, so optional parameters couldn't be skipped.
- **Error model**: syntax errors and a few fatal errors *crash* the process. Run-time errors are *raised*: they end the current function and are passed to the caller's error label or propagated upwards, and crash the process when they leave the entry function. Numeric codes are assigned, codes ≥ 1000 are user-defined, and the error names are built-in constants. New codes: SYNTAX_ERROR_UNKNOWN_STATEMENT, _FUNCTION (was used but missing from the table), _CALL, _ASSIGN, _CONDITION, _LOOP, _EXPRESSION, _EXIT, UNKNOWN_LIBRARY, UNKNOWN_FUNCTION, INVALID_EXIT_CODE, UNKNOWN_NAME, TYPE_MISMATCH, DIVISION_BY_ZERO, NUMERIC_OVERFLOW, INDEX_OUT_OF_BOUNDS, UNKNOWN_KEY, STRING_TOO_LONG, INVALID_LOOP_STEP.
- **Stack layout**: the stack holds variable/constant items, function items (call frames with return line) and block markers. Name resolution is lexical: the current frame, then globals declared before the current function, then library and built-in names.
- **Process lifecycle**: a process ends at the END/RETURN of the entry function, at EXIT or at a crash. Reaching the empty line means the entry function wasn't found. The trigger stores the exit code and the last error. Input items are mapped to parameters by name. On a crash, all inputs are returned unchanged.
- **Cursor sections added**: empty line, BEGIN, END, ASSIGN, CALL, RETURN, returning from a function, EXIT, BREAK, IF, ELSE, FOR, EACH, WHILE, REPEAT, evaluating expressions.
- **Debug cursor**: starts PAUSED. Shift-left-click toggles RUNNING/PAUSED, left-click steps one line.
- **Editing**: all changes to a sheet are blocked while a process of its program runs (answered question 5). Chunks of a running sheet are force-loaded.
- **Variable/constant items** can be edited by right-clicking them in the main hand. Without that, there was no way to give them a name or value.
- **Appendix**: a block reference table with colors and recipes. It makes duplicate recipes easy to spot.

## 3. Answered questions (2026-10-18)

| # | Question | Answer | Implemented in the concept as |
|---|---|---|---|
| 1 | How are operator blocks crafted? | A single block whose symbol is chosen in a dialog. | One OPERATOR block for all operators, `(`, `)` and `;`, with the symbol chosen in a dialog. Recipe: 8 NOOP around red wool → 8. A block without a symbol is invalid. |
| 2 | Built-in functions | The proposed functions, plus AmigaBASIC functions, except file system and graphics. | New section *Built-in functions* (see below). New error code INVALID_ARGUMENT (309). |
| 3 | Exit code as comparator signal? | Yes. | The comparator output of the trigger block is the exit code of the last process. A crashed process has exit code 15. Programs MAY use `EXIT 15 ;` to signal a fatal run-time error (second round). |
| 4 | Triggers in the c0de dimension? | Allowed in the negative area. | Only where x or z is negative. |
| 5 | Editing running programs? | Forbidden. | New section *Protection of running programs*: no block changes by any means, dialogs read-only, wrench and hammer blocked. The marker still works. |

### Selection of the built-in functions

Taken from AmigaBASIC (`$` dropped from the names, string positions start at 0 instead of 1):
ABS, SGN, SQR, EXP, LOG, SIN, COS, TAN, ATN, INT, FIX, ROUND (CINT/CLNG), TO_FLOAT (CDBL/CSNG), RANDOM (RND), RANDOMIZE, LEN (also covers UBOUND; LBOUND is always 0), LEFT, RIGHT, MID, REPLACE (MID$ statement), INSTR, UCASE, SPACE, FILL (STRING$, renamed because STRING is a keyword), ASC, CHR, STR, VAL, HEX, OCT, CLEAR (ERASE), SWAP, TIMER, DATE (DATE$), TIME (TIME$), SLEEP, RAISE (ERROR, renamed because ERROR is a keyword).

Added by c0de: DIV, LCASE, HAS_KEY, REMOVE, INSERT.

In the optional library `c0de/minecraft` (second round): BEEP and SOUND (from AmigaBASIC, as note block sounds) and TICKS (added by c0de).

Not taken over:
  - **Files**: CVI/CVL/CVS/CVD and MKI$/MKL$/MKS$/MKD$ (random-access record conversions), EOF, LOC, LOF.
  - **Graphics and screen**: POINT, POS, CSRLIN, PTAB, TAB, SPC, WINDOW, COLLISION, OBJECT.*.
  - **Not applicable in Minecraft, although neither file nor graphics related**:
    - INKEY$, INPUT$, MOUSE, STICK, STRIG, MENU: there is no keyboard, mouse or joystick attached to a process.
    - LPOS: printer.
    - FRE, PEEK/PEEKW/PEEKL, SADD, VARPTR: memory access.
    - TRANSLATE$ (and SAY): speech output.
    - ERR, ERL: covered by the ERROR type.

## 4. Answered questions, second round (2026-10-18)

| # | Question | Answer | Implemented in the concept as |
|---|---|---|---|
| 6 | Processes of one program share no state? | Yes. | Stated in the glossary entry *process*. |
| 7 | Threading | Leave it out for now. | All statements about threads were removed. |
| 8 | Literal item | Not needed at the moment. | Removed. |
| 9 | Function item | Pushed onto the stack by the cursor whenever it enters a function. When leaving, result values are copied to the caller's variables and everything on the stack down to the function item is removed. | The craftable function item was removed; function items are described in *The stack*. |
| 10 | YAML import/export | A dedicated tool. | New item *c0de disk* (looks like a 3.5" floppy). Right-click the PROGRAM block to export to `<world>/c0de/programs/<name>.yaml`; right-click the program position of an empty sheet to import. In survival mode, the blocks are taken from the player's inventory. |
| 11 | Label display | Don't show the index. Show block properties in a dialog when clicked with the debug tool. | Right-clicking any c0de block with the debugging tool opens a read-only property dialog. |
| 12 | Line width | Intentional, to encourage small functions and simple expressions. | Stated in the glossary entry *line*. |
| 13 | Recursion depth | The stack size is the limit. | Stated in the cursor properties. |
| 14 | Initialization order | Intended. | Stated in *Programs*. |

Additional instructions:
  - All Minecraft-related functions (BEEP, SOUND, TICKS) were moved from the built-in functions to the new optional library `c0de/minecraft`, which has to be included.
  - Exit code 15 can be set by the program to indicate a fatal run-time error. A crashed process also has exit code 15.

## 5. Answered questions, third round (2026-10-18)

| # | Question | Answer | Implemented in the concept as |
|---|---|---|---|
| 1 | YAML format | Key `PROGRAM` with the name, `INCLUDE` with an array of includes, and `code` with an array of nested statement objects. Each object has `keyword` and `name` plus further properties. Code blocks become a nested `code` array. | New *Appendix: YAML format* (see decisions below). |
| 2 | SLEEP stays built-in? | Yes. | Unchanged. |
| 3 | c0de/minecraft contents | Buttons, levers, pressure plates, doors, trapdoors, fence gates, block data at coordinates, chat messages. Inventory changes, placing/removing blocks, spawning and teleporting need a config setting for non-admins. | Library section extended (see decisions below). |

### Decisions made for the YAML format

- `name` is only present for statements that have a label after their keyword. IF, WHILE, REPEAT, EXIT, BREAK and NOOP have no name.
- New pseudo keywords for lines without a keyword block: ELEMENT (struct element and hash/struct initializer line) and LITERAL (array initializer line).
- PARAMETER and RESULT lines go into `parameters` / `results` arrays of the FUNCTION or CALL object. The ELSE code block goes into `else` of the IF object.
- Expressions are single strings of space-separated tokens without the final `;`. Literal syntax inside expressions: `TRUE`, `42`, `3.14`, `'a'`, `"text"`.
- BEGIN/END lines and the NOOP after PROGRAM are implied. The optional function name after END FUNCTION is not stored.
- Disabled lines (NOOP followed by blocks) keep their content as text in `content`.
- The import lays the program out automatically and fails if a line doesn't fit into x=5..28. The export requires a structurally valid program.

### Decisions made for c0de/minecraft

- **Owner**: the player who configured the trigger block last. Library functions act on their behalf. Administrator = operator level ≥ 2, checked at call time.
- **Coordinates** are passed as a library struct `c0de/minecraft/POSITION` (x, y, z, dimension; an empty dimension means the trigger's dimension). Further library types: `ITEM_STACK` and `ENTITY`.
- **Unrestricted**: TICKS, BEEP, SOUND, SAY (chat; to a named player or to all players within `sayRadius`, see fourth round), FIND_ENTITIES (fourth round), GET_BLOCK, GET_INVENTORY (reading only), GET/SET_LEVER, GET_BUTTON/PRESS_BUTTON, GET_PRESSURE_PLATE (read-only, since plates are pressed by entities), GET/SET_OPEN.
- **Restricted**, one configuration key per group, all `false` by default: `allowInventoryChanges` (MOVE_ITEMS, DELETE_ITEMS), `allowBlockChanges` (PLACE_BLOCK, REMOVE_BLOCK, and SET_OPEN on iron doors/trapdoors), `allowSpawning` (SPAWN_ITEM, SPAWN_ENTITY), `allowTeleporting` (TELEPORT).
- **Always forbidden**: teleporting players unless the owner is an admin, and changing c0de sheets.
- **Chunks**: functions never load chunks.
- **New error codes**: CHUNK_NOT_LOADED (310), PERMISSION_DENIED (311).

## 6. Answered questions, fourth round (2026-10-18)

| # | Question | Answer | Implemented in the concept as |
|---|---|---|---|
| 1 | Should FIND_ENTITIES be restricted? | Unrestricted. | Removed from the `allowTeleporting` group, which now only contains TELEPORT. Players are included in the results. |
| 2 | SAY radius configurable? | Yes. | Configuration key `sayRadius`, default 64 blocks. |

There are currently no open questions.
