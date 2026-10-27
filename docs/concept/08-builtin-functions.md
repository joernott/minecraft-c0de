# Built-in functions and libraries

[c0de concept](../concept.md) › Built-in functions and libraries

The functions provided by c0de are split into built-in functions, which are always available, and libraries, which MUST be included with an INCLUDE statement before their functions and types can be used:

| Library | Content |
|---|---|
| built-in | LEN, array and hash functions, RAISE, input and output (PRINT, WRITE, INPUT) and the screen functions |
| `c0de/math` | numeric functions and random numbers |
| `c0de/strings` | string and character functions, conversions, JSON and YAML |
| `c0de/time` | time, date and SLEEP |
| `c0de/minecraft` | interaction with Minecraft |

Built-in functions have the empty string as parent and are called by their names. Library functions are called by their qualified names. All of them are called with a CALL statement like any other function, using named PARAMETER and RESULT lines:

```
INCLUDE c0de/strings
…
CALL c0de/strings/MID
    PARAMETER text name ;
    PARAMETER start 2 ;
    PARAMETER length 3 ;
    RESULT result part
```

Many functions are taken from AmigaBASIC. AmigaBASIC functions that deal with files, graphics, memory, input devices, menus or speech output have no equivalent in c0de; the text screen functions (CLS, LOCATE, CSRLIN, POS, COLOR, INKEY) work on the screens of display blocks, laptops and tablets. The `$` suffix of the AmigaBASIC string function names is dropped, as `$` is not allowed in names.

Conventions:
  - A function with a single result names it `result`.
  - Some parameters accept several types, written as `INTEGER|FLOAT`. Where the result type is written as "like `value`", the result has the type of that parameter.
  - The built-in functions and the libraries `c0de/math`, `c0de/strings` and `c0de/time` accept a CHAR wherever a STRING parameter is expected. The library `c0de/minecraft` only accepts a STRING for its STRING parameters.
  - Parameters are passed by value, so functions that change a string, array or hash return the changed value as `result`. To change a variable, use the same variable as parameter and result target. The only exceptions are the `destination` parameter of [FROM_JSON and FROM_YAML](10-lib-strings.md#reading-values) and the `variable` parameter of [INPUT](19-input-output.md#input), which write directly into a variable.
  - Positions in strings and arrays start at 0, unlike AmigaBASIC, where string positions start at 1.
  - An invalid argument (e.g. the square root of a negative number) raises INVALID_ARGUMENT unless stated otherwise.

The input and output functions PRINT, WRITE and INPUT and the screen functions CLS, LOCATE, CSRLIN, POS, COLOR and INKEY are built-in functions too; they are described in [Input and output](19-input-output.md).

Besides the functions, every program has [built-in constants](04-data.md#built-in-constants): the names of the error codes, the color numbers `COLOR_BLACK` to `COLOR_WHITE`, and `SHEET` and `PID` with information about the sheet and the process.

## Built-in functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| LEN | value STRING\|ARRAY\|HASH | result INTEGER | Number of characters of a string or number of elements of an array or hash. | LEN, UBOUND |
| HAS_KEY | hash HASH, key STRING | result BOOLEAN | True if `hash` contains `key`. | – |
| REMOVE | container ARRAY\|HASH, index INTEGER (optional), key STRING (optional) | result like `container` | `container` without the element. An array element is selected with `index`, a hash element with `key`; leaving out the parameter the container needs raises INVALID_ARGUMENT. Following array elements move down by one. An index that does not exist raises INDEX_OUT_OF_BOUNDS, a key that does not exist raises UNKNOWN_KEY. | – |
| INSERT | array ARRAY, index INTEGER, value (subtype of `array`) | result like `array` | `array` with `value` inserted at position `index`. Following elements move up by one. `index` MUST be between 0 and the length of the array, otherwise INDEX_OUT_OF_BOUNDS is raised. A `value` that does not fit the subtype of `array` raises TYPE_MISMATCH. | – |
| CLEAR | container ARRAY\|HASH | result like `container` | An empty array or hash of the same type. | ERASE |
| SWAP | a (any type), b (same type as `a`) | a, b | Returns the two values swapped. Values of different types raise TYPE_MISMATCH (length limits of STRINGs are ignored). | SWAP |
| RAISE | code INTEGER, message STRING (default empty) | – | Returns an error with `code` and `message` to the caller, which raises it or stores it in the error label of the CALL statement. `code` MUST be 1000 or higher, otherwise INVALID_ARGUMENT is raised. The `context` and `line` of the error are those of the CALL statement. | ERROR |
| PRINT | value (any type) | – | Writes `value` on the bound screens and ends the line, see [Input and output](19-input-output.md#print). | PRINT |
| WRITE | value (any type), name STRING (optional) | – | Puts `value` into the bound I/O blocks as a variable item; waits for an uninitialized variable item, see [Input and output](19-input-output.md#write). | WRITE |
| INPUT | variable (a variable) | – | Reads a value from the bound I/O blocks or a typed line into `variable`, which is passed by reference; waits for a value, see [Input and output](19-input-output.md#input). | INPUT |
