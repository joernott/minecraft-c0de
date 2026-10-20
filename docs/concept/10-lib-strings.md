# The c0de/strings library

[c0de concept](../concept.md) › The c0de/strings library

The library `c0de/strings` contains all string and character functions except LEN, which is built in. It MUST be included with `INCLUDE c0de/strings`.

## String and character functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| LEFT | text STRING, length INTEGER | result STRING | The first `length` characters of `text`, or the whole text if it is shorter. | LEFT$ |
| RIGHT | text STRING, length INTEGER | result STRING | The last `length` characters of `text`, or the whole text if it is shorter. | RIGHT$ |
| MID | text STRING, start INTEGER, length INTEGER (default: rest of the text) | result STRING | `length` characters of `text`, beginning at position `start`. Returns fewer characters at the end of the text and an empty string if `start` is behind the end. | MID$ function |
| REPLACE | text STRING, start INTEGER, insert STRING | result STRING | Overwrites the characters of `text` beginning at position `start` with `insert`. The length of `text` does not change; characters of `insert` that would extend it are dropped. | MID$ statement |
| INSTR | text STRING, search STRING, start INTEGER (default 0) | result INTEGER | Position of the first occurrence of `search` in `text`, searching from position `start`. -1 if it is not found. | INSTR |
| UCASE | text STRING | result STRING | `text` with all lowercase letters converted to uppercase. | UCASE$ |
| LCASE | text STRING | result STRING | `text` with all uppercase letters converted to lowercase. | – |
| SPACE | count INTEGER | result STRING | A string of `count` spaces. `count` MUST be between 0 and 1,000,000. | SPACE$ |
| FILL | count INTEGER, char CHAR | result STRING | A string of `count` copies of `char`. `count` MUST be between 0 and 1,000,000. | STRING$ |
| ASC | char CHAR | result INTEGER | The Unicode code point of `char`. | ASC |
| CHR | code INTEGER | result CHAR | The character with the Unicode code point `code`. | CHR$ |
| STR | value BOOLEAN\|INTEGER\|FLOAT\|CHAR\|TIME | result STRING | Text representation of `value`, the same as used by the ⊕ operator. | STR$ |
| FORMAT | value BOOLEAN\|INTEGER\|FLOAT\|CHAR\|STRING\|TIME, format STRING | result STRING | Formats `value` according to the format string `format`, see [FORMAT](#format). | DATE$, TIME$, PRINT USING |
| VAL | text STRING | value FLOAT, isInteger BOOLEAN | Converts a text to a number. `isInteger` is true if the text contains an integer number, which can be converted with FIX without loss. Raises INVALID_ARGUMENT if the text is not a number. | VAL |
| HEX | value INTEGER | result STRING | Hexadecimal representation of `value` in uppercase letters. Negative values are shown in two's complement, e.g. `FFFFFFFFFFFFFFFF` for -1. | HEX$ |
| OCT | value INTEGER | result STRING | Octal representation of `value`. Negative values are shown in two's complement. | OCT$ |

## FORMAT

FORMAT converts a value to text according to a format string that is based on the format strings of `printf` in C and `String.format` in Java. The format string contains normal text and conversions, which start with `%`. All conversions refer to the same value, `value`, so a format string can show several parts of a TIME value. A conversion has the form

`%[flags][width][.precision]conversion`

  - **flags**: `-` aligns the text to the left within `width`, `0` pads numbers with zeros instead of spaces, `+` always shows the sign of a number, a space shows a space instead of a `+` for positive numbers, and `,` groups the digits of a number in thousands.
  - **width**: the minimum number of characters. Shorter texts are padded with spaces (or zeros).
  - **precision**: the number of decimal places for `f` and `e`, or the maximum number of characters for `s`.

| Conversion | Type of `value` | Result |
|---|---|---|
| `%d` | INTEGER | decimal number |
| `%x`, `%X` | INTEGER | hexadecimal number in lowercase or uppercase letters |
| `%o` | INTEGER | octal number |
| `%f` | INTEGER, FLOAT | decimal number with decimal places (default 6) |
| `%e` | INTEGER, FLOAT | number in scientific notation, e.g. `1.234560e+03` |
| `%g` | INTEGER, FLOAT | `%f` or `%e`, whichever is shorter. The flag `0` is ignored. |
| `%c` | CHAR | the character |
| `%b` | BOOLEAN | `true` or `false` |
| `%s` | any | the text representation, the same as used by the ⊕ operator |
| `%%` | – | a `%` sign |
| `%n` | – | a line break |

TIME values are formatted with conversions that start with `%t`. They use the time zone of the server:

| Conversion | Result | Example |
|---|---|---|
| `%tY` | year with four digits | `2026` |
| `%ty` | last two digits of the year | `26` |
| `%tm` | month with two digits | `10` |
| `%tB`, `%tb` | name of the month, full or abbreviated | `October`, `Oct` |
| `%td` | day of the month with two digits | `19` |
| `%te` | day of the month without leading zero | `19` |
| `%tj` | day of the year with three digits | `292` |
| `%tA`, `%ta` | name of the day of the week, full or abbreviated | `Monday`, `Mon` |
| `%tu` | day of the week according to ISO 8601 (1 = Monday) | `1` |
| `%tV` | week of the year according to ISO 8601 with two digits | `43` |
| `%tH` | hour (00–23) | `14` |
| `%tI` | hour (01–12) | `02` |
| `%tp` | `am` or `pm` | `pm` |
| `%tM` | minute with two digits | `05` |
| `%tS` | second with two digits | `09` |
| `%tL` | millisecond with three digits | `250` |
| `%tZ` | abbreviation of the time zone | `CEST` |
| `%tz` | offset of the time zone | `+0200` |
| `%ts` | Unix timestamp in seconds | `1792411509` |
| `%tQ` | Unix timestamp in milliseconds | `1792411509250` |
| `%tF` | date, the same as `%tY-%tm-%td` | `2026-10-19` |
| `%tT` | time, the same as `%tH:%tM:%tS` | `14:05:09` |
| `%tR` | hour and minute, the same as `%tH:%tM` | `14:05` |

Names of months and days of the week are written in English.

A conversion that does not fit the type of `value` raises TYPE_MISMATCH, an invalid format string raises INVALID_ARGUMENT.

| Call | Result |
|---|---|
| FORMAT(42, `"%5d"`) | `   42` |
| FORMAT(42, `"%05d"`) | `00042` |
| FORMAT(3.14159, `"%.2f"`) | `3.14` |
| FORMAT(1234567, `"%,d"`) | `1,234,567` |
| FORMAT(255, `"0x%X"`) | `0xFF` |
| FORMAT("c0de", `"[%-8s]"`) | `[c0de    ]` |
| FORMAT(now, `"%tF %tT"`) | `2026-10-19 14:05:09` |
| FORMAT(now, `"%tA, %te. %tB %tY"`) | `Monday, 19. October 2026` |

## JSON and YAML functions

| Function | Parameters | Results | Description |
|---|---|---|---|
| TO_JSON | value (any type), indent INTEGER (default 0) | result STRING | Converts `value` to a JSON document. With `indent` 0, the document is written in a single line; otherwise, nested elements are indented by `indent` spaces. `indent` MUST be between 0 and 16. |
| TO_YAML | value (any type) | result STRING | Converts `value` to a YAML document in block style with an indentation of two spaces. |
| FROM_JSON | source STRING, destination (variable) | – | Reads the JSON document `source` into the variable `destination`. |
| FROM_YAML | source STRING, destination (variable) | – | Reads the YAML document `source` into the variable `destination`. |

JSON documents follow RFC 8259, YAML documents follow YAML 1.2. FROM_YAML only reads the first document of a YAML stream. Since every JSON document is a YAML 1.2 document, FROM_JSON uses the same parser and also accepts some documents that are not strict JSON, e.g. with comments.

### Converting values

c0de has no dynamic data type (like `map[string]interface{}` in Go or a VARIANT type). Every value is converted according to the declared type of the variable, and the structure of a document MUST fit that type. Values are converted as follows:

| c0de type | JSON / YAML |
|---|---|
| BOOLEAN | `true` / `false` |
| INTEGER | integer number. A number with decimal places can't be read into an INTEGER. |
| FLOAT | number. Integer numbers can be read into a FLOAT. Infinite values and NaN can't be written (INVALID_ARGUMENT). |
| CHAR | string with exactly one character |
| TIME | string in the ISO 8601 format in UTC with milliseconds, e.g. `2026-10-19T12:05:09.250Z`. When reading, any ISO 8601 date and time (milliseconds optional; without an offset, UTC is assumed) and integer numbers (as Unix timestamp in milliseconds) are accepted. |
| STRING | string |
| ARRAY | array (JSON) / sequence (YAML). All elements MUST fit the subtype of the array. |
| HASH | object (JSON) / mapping (YAML). The keys of the hash are the keys of the object; they are written in ascending lexicographic order. All values MUST fit the subtype of the hash. |
| STRUCT | object / mapping. The keys are the element names or the names of their [tags](04-data.md#tags-of-struct-elements) for the language; elements with the tag name `-` are skipped. Elements are written in the order of their declaration. |
| ERROR | object / mapping with the keys `code`, `message`, `context` and `line` |

### Writing values

If the parameter `value` of TO_JSON or TO_YAML is provided by an expression that consists of a single LABEL without index, and the variable or constant it refers to has a [tag](04-data.md#tags-of-variables-and-constants) for the language, the function creates a structure around the value, so that it is located at the path of the tag. Keys create objects (mappings), array indexes create arrays (sequences); array elements before the index are written as `null`.

### Reading values

The parameter `destination` of FROM_JSON and FROM_YAML is an exception to the rule that parameters are passed by value: it MUST be provided by a single LABEL without index that refers to a previously declared variable. Constants and loop variables are not allowed. The function writes the result directly into that variable; it has no result. The type of the variable determines how the document is read:
  - If the variable has a [tag](04-data.md#tags-of-variables-and-constants) for the language, only the value at exactly the path of the tag is read. If the document has no value at that path, UNKNOWN_KEY is raised.
  - The tags of struct elements translate the keys of the document to element names.
  - Keys of the document that don't belong to an element of a struct are ignored.
  - Elements of a struct that are missing in the document or that are `null` keep their current value. So the current value of the variable provides the default values.
  - Arrays and hashes are replaced completely by the content of the document.
  - A value that doesn't fit the type at its position raises TYPE_MISMATCH, a malformed document raises INVALID_ARGUMENT and a STRING that exceeds its length limit raises STRING_TOO_LONG.
  - If an error is raised, the variable is not changed at all.

### Example

In the text notation of the examples, a TAG block is written as `TAG(language: "name")`.

```
VARIABLE port INTEGER 25565 TAG(json: "server.port")
…
CALL c0de/strings/TO_JSON
    PARAMETER value port ;
    RESULT result text
```

After this call, `text` contains `{"server":{"port":25565}}`. The following call reads the port from a configuration document stored in `config`. If the document can't be read, the error is stored in `readError` and `port` keeps its value 25565:

```
CALL c0de/strings/FROM_JSON readError
    PARAMETER source config ;
    PARAMETER destination port ;
```
