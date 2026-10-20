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

## 7. COMMENT, TAG, libraries and document split (2026-10-19)

The concept was split into one document per topic. [concept.md](concept.md) now contains the abstract, the introduction, the requirement keywords, the glossary and a list of the documents in [concept/](concept/). Section names referenced in this review can be found there.

### COMMENT

- Placed at the end of a line. The cursor ignores the COMMENT block and everything after it, which comments out the rest of the line.
- **Decision:** a line that *starts* with a COMMENT block is a comment line and is skipped like a NOOP line. It does not count as a statement, so a code block with only comment lines is still empty.
- **Decision:** a COMMENT inside an unfinished expression (before `;`) lets the expression continue on the next continuation line.
- **Decision on looks:** a glass block with a yellow note showing `//` on its sides. The text is shown above the block when a player looks at it, and is edited in a multi-line dialog.
- **YAML:** the object of the line gets `comment` (the text) and `disabled` (the blocks after the comment, as text). Comment lines get the keyword `COMMENT`.

### TAG

- A TAG has a `language` (`json` or `yaml`) and a `name`. At most one tag per language in a line.
- **Struct elements**: the name replaces the key. **Decision:** the name `-` skips the element, as in Go. Element tags must not contain dots.
- **Variables and constants**: the name is a dot-separated path (`server.port`). **Decision:** tags are also allowed on CONSTANT, but they are only used for writing (TO_JSON/TO_YAML), as constants can't be a result target. **Decision:** a path can only address object/mapping keys, not array elements.
- **Decision on looks:** brown (#AA7744) or dark brown (#553311) with a price tag labeled with the language.
- In text examples, a TAG is written as `TAG(json: "name")`.

### Libraries

- **Built-in** (no include needed): LEN, HAS_KEY, REMOVE, INSERT, CLEAR, SWAP, RAISE.
- **c0de/math**: all numeric functions (ABS … RANDOMIZE, including DIV and TO_FLOAT).
- **c0de/strings**: all string and character functions except LEN, including the conversions STR, VAL, HEX, OCT, ASC, CHR. Also the new functions TO_JSON, TO_YAML, FROM_JSON and FROM_YAML.
- **c0de/time**: TIMER, DATE, TIME and SLEEP. **Decision:** TICKS stays in `c0de/minecraft`, because it is the Minecraft game time.

### TO_JSON, TO_YAML, FROM_JSON, FROM_YAML

- **Decision:** the names are written in capitals like all other c0de functions.
- **Decision on how tags reach the function:** parameters are passed by value, so a value alone carries no tags. When a PARAMETER expression is a single LABEL without index, the cursor passes the variable's tags along with the value.
- **FROM_JSON/FROM_YAML** have a parameter `template`: the result has the template's type, the template's variable tag selects the path, and the template's values are the defaults for missing or `null` elements. Unknown keys are ignored. A missing path raises UNKNOWN_KEY, a malformed document raises INVALID_ARGUMENT.
- TO_JSON has an optional `indent` (0 = single line). Hash keys are sorted, struct elements keep their declaration order.

## 8. Answered questions and changes, sixth round (2026-10-19)

| # | Question | Answer | Implemented in the concept as |
|---|---|---|---|
| 1 | NOOP lines with content? | NOOP lines must be empty. | A NOOP line must not contain any other block, not even a COMMENT. Otherwise the cursor crashes with SYNTAX_ERROR_PLACEMENT. The `content` property of NOOP objects in YAML was removed. |
| 2 | Arrays in tag paths? | Yes. | A path key consisting only of digits addresses an array index (`servers.0.port`). **Decision:** when reading, it falls back to an object key if the value at that position is an object. When writing, it creates an array and fills earlier elements with `null`. |

**FROM_JSON / FROM_YAML** (as requested):
  - The parameters are now `source` (STRING) and `destination` (a previously declared variable). There is no `template` parameter and no result anymore.
  - `destination` is the only by-reference parameter in c0de. It must be a single label without index that refers to a variable (not a constant or loop variable), otherwise SYNTAX_ERROR_CALL.
  - The variable's tags select the path and its struct elements' tags translate the keys. Unknown keys are ignored, and type mismatches raise TYPE_MISMATCH.
  - The documentation now says explicitly that there is no dynamic type: the document must fit the declared type.
  - **Decision:** struct elements missing in the document (or `null`) keep their current value, arrays and hashes are replaced completely, and the variable stays unchanged if an error is raised.

## 9. TIME, time functions, PI and FORMAT (2026-10-19)

As requested:
  - New simple data type TIME (Unix timestamp).
  - `c0de/time`: NOW, SECOND, MINUTE, HOUR, DAY, MONTH, YEAR, WEEKDAY (ISO 8601, 1 = Monday) and WEEK (ISO 8601).
  - Constant `c0de/math/PI`.
  - `c0de/strings/FORMAT(value, format)` based on printf. DATE and TIME were removed; the name TIME is now the type keyword anyway.

Decisions made:
  - **TIME** holds whole seconds as a signed 64 bit integer, with a default of 0 (1970-01-01 00:00:00 UTC).
    - Its text representation (⊕, STR, JSON, YAML) is ISO 8601 in UTC, e.g. `2026-10-19T12:05:09Z`.
    - Literals are entered in ISO 8601; without an offset, the server's time zone applies.
    - Block: TYPE_TIME with a clock, crafted from a type base and a clock.
  - **Arithmetic**: `TIME ± INTEGER` (seconds) results in TIME, and `TIME − TIME` in INTEGER seconds. TIME values compare chronologically.
  - **SECOND … WEEK** take an optional parameter `time` (default: now), so they also work on stored TIME values. They use the server's time zone. TIMER and SLEEP stay in `c0de/time`.
  - **FORMAT**:
    - Flags `- 0 + space ,`, width and precision.
    - Conversions `%d %x %X %o %f %e %g %c %b %s %% %n`.
    - Time conversions follow Java's `%t…` (`%tY %tm %td %tH %tM %tS %tF %tT …`), plus `%tu` and `%tV` for the ISO weekday and week.
    - All conversions refer to the single `value`. Month and day names are in English.
  - **YAML**: TIME literals inside expressions are written as `@2026-10-19T12:05:09Z`.

## 10. Answered questions (2026-10-19)

| # | Question | Answer | Implemented in the concept as |
|---|---|---|---|
| 1 | Time zone | The server's time zone. | Unchanged. The time library now states explicitly that all functions and FORMAT always use the server's time zone. |
| 2 | Precision of TIME | Milliseconds. | TIME holds milliseconds since 1970-01-01 UTC. `TIME ± INTEGER` and `TIME − TIME` now work in milliseconds. The text representation includes milliseconds (`2026-10-19T12:05:09.250Z`); in literals they are optional. JSON/YAML read integer numbers as milliseconds. **Added:** the function MILLISECOND and the FORMAT conversions `%tL` (milliseconds) and `%tQ` (timestamp in milliseconds); `%ts` stays in seconds. |

There are currently no open questions.

## 11. Decisions made while implementing sprint 3 (2026-10-20)

Questions the concept didn't answer, and one contradiction:

- **Keywords are reserved only in capital letters** (correction). The first review had added "names are compared to keywords case-insensitively". That contradicts the libraries: single results are named `result` (RESULT is a keyword), and parameters are named `char`, `time`, `array` and `hash`. Now only the capitalized spelling is reserved: `RESULT` is not a valid name, `result` is.
- **Exponentiation**: INTEGER ^ INTEGER is an INTEGER if the exponent is not negative, otherwise a FLOAT (`2 ^ -3` is `0.125`).
- **Modulus**: the result has the sign of the dividend (`-7 \ 2` is `-1`), as in Java and most BASIC dialects.
- **AND / OR** only evaluate the right operand when needed (short-circuit evaluation), so conditions like `x ≠ 0 AND 10 / x > 1` are safe.
- **CHAR and STRING** can be compared with each other (`'a' = "a"`); texts are ordered by Unicode code points.
- **Shift counts** use only their lowest 6 bits, as in Java.
- **Text representation** (⊕, STR): `true`/`false`, Java's FLOAT format (`2.5`, `1.0E20`), ISO 8601 for TIME, `[1, 2]` for arrays, `{key: value}` for hashes and structs.
- **Initializers** accept INTEGER literals for FLOAT elements and CHAR literals for STRING elements, like ASSIGN.
- **`.name` on a hash** is read as the key `"name"` (`inventory.diamond` is the same as `inventory["diamond"]`).
- **Syntax errors in element lines** (struct elements, PARAMETER lines, END lines) are reported with the z coordinate of that line, not of the statement's first line.
- **A missing END** is detected when a line at or left of the BEGIN's x coordinate follows that is not the matching END. Taking "the first END at the same x coordinate" literally would match the END of the next function when a function's END is missing.

## 12. Decisions made while implementing sprint 4 (2026-10-20)

- **Steps**: NOOP lines, comment lines, continuation lines and the content lines of declarations don't take a step of their own. A STRUCT/VARIABLE/CONSTANT statement with its code block and a FUNCTION statement with its PARAMETER and RESULT lines take one step. Every other statement and every END line takes one step. Waiting with SLEEP takes one step per tick.
- **Error labels** always receive the returned error, with code 0 if there was none, so a program can check `problem.code = 0` after every call.
- **Propagated errors** keep the `context` and `line` of the place where they occurred first.
- **Raised errors in the entry function** crash the process with that error and exit code 15. EXIT keeps the current values of the results of the entry function, even if EXIT is called in a nested function.
- **Functions** must be declared in a line before the CALL statement, except for recursive calls of the current function. Mutual recursion is therefore not possible.
- **FOR loops**: start, end and increment must be numbers. With an INTEGER loop variable, FLOAT values raise TYPE_MISMATCH.
- **Persistence**: a running cursor is stored as a tree of plain data (maps, lists, strings, numbers, booleans, without null values) that refers to statements by their lines. The mod stores it as NBT (sprint 8). The random number generator of a process (SplitMix64) is part of the stored state, so a process continues the same sequence of random numbers after a restart.
- **Interface to the mod**: libraries are Java objects (types, constants, native functions). A native function gets its arguments, the tags of single-label arguments, the variables of by-reference parameters and the process (random numbers, clock, time zone, access to the mod). It returns results, an error or a number of steps to wait.

## 13. Decisions made while implementing sprint 5 (2026-10-20)

- **JSON without Gson**: the plan named Gson for JSON. JSON is written by a small writer of our own, and FROM_JSON parses with SnakeYAML Engine, because every JSON document is a YAML 1.2 document. This keeps a single embedded library (SnakeYAML Engine 3.2, jar-in-jar on both loaders). The parser is lenient: it also accepts some documents that are not strict JSON.
- **Number parameters**: parameters of type FLOAT also accept INTEGER values (e.g. `SQR 9`, `ROUND 3`).
- **Substrings**: LEFT and RIGHT with a length beyond the text return the whole text; MID with a start behind the end returns an empty string. Negative lengths and positions raise INVALID_ARGUMENT.
- **HEX and OCT**: HEX uses uppercase letters; negative values are shown in two's complement.
- **Limits**: SPACE and FILL create at most 1,000,000 characters; the indentation of TO_JSON is 0 to 16.
- **YAML output**: TIME values are written as unquoted ISO 8601 timestamps (`joined: 2026-10-19T12:05:09.250Z`). When reading, a time without an offset is UTC.
- **`%g`** ignores the flag `0`.
- **RAISE** returns the error as an error of the calling function: its `context` is the function that called RAISE, not RAISE itself.
- **Start-up check**: the mod converts a small JSON document to YAML at start-up and logs the result, so a missing embedded library shows up in the server log (and fails the smoke test).

## 14. Decisions made while implementing sprint 6 (2026-10-20)

- **Block IDs**: `<name>_block` in lowercase, type blocks `type_<type>_block`; the items `type_base` and `structural_base`.
- **Block state `valid`**: only blocks with an invalid color have it. Until the validation of sprint 7 exists, placed blocks show their invalid color.
- **Empty hand**: dialogs only open with an empty main hand. With an item, the item is used, so blocks can be placed against LABEL, LITERAL, OPERATOR, COMMENT and TAG blocks.
- **LABEL dialog**: instead of a strict dropdown, an input field with a list of entries that filters while typing. The lists are static for now (all libraries including `c0de/minecraft`, the built-in functions and all library functions); the functions, parameters, struct types and variables of the program follow in task 7.5. The index is entered as text (`[i][2]`, `["key"]`, `.element`, at most 8 entries). Kind and parent are determined again when the dialog is saved. Function names are entered qualified for library functions (`c0de/math/SIN`) and split into parent and name.
- **Keys of hash initializers** are entered like other new names and must therefore be valid names.
- **OPERATOR dialog**: a grid of buttons; clicking a symbol saves it immediately.
- **LITERAL**: the value is stored as entered and parsed when it is used, so a TIME without an offset is in the time zone of the server. STRING values are shown in quotes on the block.
- **Limits**: names, values and comments have at most 1000 characters. Above a COMMENT block, at most 8 lines of its text are shown.
- **Server checks**: the server checks every dialog input again, and only accepts it from players within their interaction range plus two blocks. Sheets with running processes (sprint 8) will open dialogs read-only and reject changes.
- **Data generation**: the JSON resources are generated by `scripts/generate_content.py` and the placeholder textures by `scripts/generate_textures.py`, instead of the data generators of the loaders. Both loaders use the same files, and CI checks that the JSON files are up to date. The recipes use the ingredient format of Minecraft 1.21.1 (`{"item": "minecraft:stick"}`); the concept shows the shorter format of later versions.
- **Text on blocks** is drawn by a block entity renderer on all six sides, scaled to fit.

## 15. Decisions made while implementing sprint 7 (2026-10-20)

- **Validation per line**: all blocks of a line share its validity. Statements that the parser can't finish (e.g. BEGIN without END) make all lines it skipped invalid. Lines after the empty line that ends a program are invalid.
- **Whole programs**: after a change, the whole program of the sheet is validated at the end of the server tick, because a change can affect lines far away. Several changes in one tick cause one validation. Sheets with lines in chunks that are not loaded are not validated.
- **Names, not types**: the static validation checks the syntax and the names like the cursor (task 7.2). Types of expressions are only checked at runtime, because they depend on values (e.g. EACH variables).
- **Declared before use**: a function may only use global variables, constants, struct types and functions declared in lines before it (concept 03), except itself for recursion. The validation is therefore a bit stricter than the cursor, which also finds globals declared between the function and the entry function.
- **Placement rules** (task 7.4): only the rules for NOOP lines, FUNCTION lines, empty code blocks and PARAMETER/RESULT lines are checked when a block is placed. Everything else is reported by the validation, so that programs can be written in any order.
- **Program registry** (task 7.6): program names and their functions are stored with the c0de dimension, so they are known for sheets in unloaded chunks. A second sheet with a used name gets an invalid PROGRAM line and is not registered.
- **Code area only** (task 7.4.1): on a sheet, c0de blocks can only be placed in the code area of the code layer (x=5 to x=28, directly on the floor), not on the margins, the cursor rail or on top of other blocks.
- **Existing programs** (task 7.6.1): sheets are validated when the chunk of their PROGRAM line is loaded, so programs that were never validated (e.g. written before sprint 7) are registered. If two sheets use the same name, the one registered first keeps it.
- **LABEL lists** (task 7.5) come from the program: callable functions (built-in, included, declared before or the current function), the parameters and results of the called function, struct types and the variables and constants in scope, innermost first. c0de/minecraft is not listed until it exists (sprint 10).
