# YAML format

[c0de concept](../concept.md) › YAML format

Programs are exported and imported with the [c0de disk](14-items-and-tools.md#c0de-disk) as YAML files. A file has three top-level keys:

| Key | Content |
|---|---|
| `PROGRAM` | The name of the program. |
| `INCLUDE` | An array with the names of all included libraries. MAY be omitted if there are none. |
| `code` | An array of statement objects with all other statements of the program, in their order on the sheet. |

The NOOP line after the PROGRAM statement is not written to the file; it is always placed on import.

## Statement objects

Every statement is an object with the property `keyword`. Statements that have a LABEL directly after their keyword also have a `name` property. Depending on the statement, more properties follow. If a statement contains a code block, the statements of that code block are written as a nested array in the property `code`. BEGIN and END lines are not written; they follow from the nesting.

| keyword | Properties |
|---|---|
| NOOP | – |
| COMMENT | A comment line: `text`, `disabled` (optional) |
| STRUCT | `name`, `code`: one ELEMENT object per element |
| VARIABLE, CONSTANT | `name`, `type`, `subtype` (ARRAY and HASH), `struct` (struct name, if the type or subtype is STRUCT), `length` (STRING length limit), `value` (simple types and STRING), `code` (initializer of an ARRAY, HASH or STRUCT), `tags` |
| ELEMENT | Element of a struct declaration: `name`, `type`, `subtype`, `struct`, `tags`. Element of a hash or struct initializer: `name` (key or element name) and `value` or a nested `code` |
| LITERAL | Line of an array initializer: `values` (an array of values) or a nested `code` for a nested initializer |
| FUNCTION | `name`, `parameters`, `results`, `code` |
| PARAMETER | In a FUNCTION: `name`, `type`, `subtype`, `struct`, `value` (default value). In a CALL: `name`, `expression` |
| RESULT | In a FUNCTION: `name`, `type`, `subtype`, `struct`. In a CALL: `name`, `target` |
| CALL | `name` (function name), `error` (error label, optional), `parameters`, `results` |
| RETURN | `name` (error label, optional) |
| ASSIGN | `name` (target, with index), `expression` |
| EXIT | `expression` |
| BREAK | `expression` (optional) |
| IF | `expression`, `code`, `else` (optional, the statements of the ELSE code block) |
| FOR | `name`, `type`, `from`, `to`, `step` (optional), `code` |
| EACH | `name`, `iterable`, `code` |
| WHILE | `expression`, `code` |
| REPEAT | `code`, `expression` |

`parameters` and `results` are arrays of PARAMETER and RESULT objects.

`tags` is an object with the languages as keys and the tag names as values, e.g. `tags: {json: "server.port", yaml: "port"}`.

## Comments

A COMMENT block at the end of a line is written into the object of that line:
  - `comment`: the text of the COMMENT block,
  - `disabled` (optional): the blocks after the COMMENT block, written as text.

A line that starts with a COMMENT block is written as an object with the keyword `COMMENT`, its `text` and the optional `disabled` blocks. On import, the blocks of `disabled` are placed again after the COMMENT block.

## Values and expressions

Literal values are written as YAML scalars that match their type: `true` or `false` for BOOLEAN, numbers for INTEGER and FLOAT (a FLOAT always has a decimal point, e.g. `1.0`), strings for CHAR and STRING, and ISO 8601 strings with milliseconds for TIME (e.g. `"2026-10-19T12:05:09.250Z"`).

Expressions (`expression`, `from`, `to`, `step`) are written as a single string of tokens separated by spaces, without the final `;`:
  - Operators use their symbols (`+`, `≠`, `⊕`, …) or names (`NOT`, `AND`, `OR`, `XOR`).
  - Literals are written as `TRUE`, `FALSE`, `42`, `3.14`, `'a'` (CHAR), `"text"` (STRING) or `@2026-10-19T12:05:09.250Z` (TIME).
  - Labels are written as their names with index, e.g. `values[i]` or `position.x`.
  - Parentheses are only written where the precedence of the operators requires them, e.g. `(a + b) * c` but `a + b * c`. Parentheses that were placed on the sheet without being needed are not kept.

Labels with an index (`name` of ASSIGN, `target`, `iterable`) use the same notation.

## Import

On import, the c0de disk lays the program out on the sheet: it computes the indentation and inserts the BEGIN and END lines. Expressions that don't fit into a line are continued on continuation lines. If a line can't be laid out within x=5 to x=28, the import fails with an error message. Optional parts that are not stored in the file, such as the function name after `END FUNCTION`, are not placed.

On export, the program MUST be structurally valid (all BEGIN and END blocks match, every line starts with a known keyword). Otherwise, the export fails and the disk names the first invalid line.

## Example

```yaml
PROGRAM: doorbell
INCLUDE:
  - c0de/minecraft
  - c0de/time
code:
  - keyword: CONSTANT
    name: RINGS
    type: INTEGER
    value: 3
    comment: number of beeps
  - keyword: NOOP
  - keyword: FUNCTION
    name: ring
    parameters:
      - keyword: PARAMETER
        name: visitor
        type: STRING
        value: ""
    results:
      - keyword: RESULT
        name: count
        type: INTEGER
    code:
      - keyword: FOR
        name: i
        type: INTEGER
        from: "1"
        to: "RINGS"
        code:
          - keyword: CALL
            name: c0de/minecraft/BEEP
          - keyword: CALL
            name: c0de/time/SLEEP
            parameters:
              - keyword: PARAMETER
                name: ticks
                expression: "10"
          - keyword: ASSIGN
            name: count
            expression: "count + 1"
      - keyword: IF
        expression: "visitor ≠ \"\""
        code:
          - keyword: CALL
            name: c0de/minecraft/SAY
            parameters:
              - keyword: PARAMETER
                name: message
                expression: "visitor ⊕ \" is at the door\""
        else:
          - keyword: CALL
            name: c0de/minecraft/SAY
            parameters:
              - keyword: PARAMETER
                name: message
                expression: "\"Somebody is at the door\""
```
