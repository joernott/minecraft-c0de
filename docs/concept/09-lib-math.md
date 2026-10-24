# The c0de/math library

[c0de concept](../concept.md) › The c0de/math library

The library `c0de/math` contains the numeric functions and constants. It MUST be included with `INCLUDE c0de/math`.

## Constants

| Constant | Type | Value | Description |
|---|---|---|---|
| `c0de/math/PI` | FLOAT | 3.141592653589793 | The ratio of the circumference of a circle to its diameter (π). |

## Functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| ABS | value INTEGER\|FLOAT | result like `value` | Absolute value. The absolute value of the smallest INTEGER (-9223372036854775808) raises NUMERIC_OVERFLOW. | ABS |
| SGN | value INTEGER\|FLOAT | result INTEGER | -1 if `value` < 0, 0 if `value` = 0, 1 if `value` > 0. | SGN |
| SQR | value INTEGER\|FLOAT | result FLOAT | Square root. `value` MUST NOT be negative. | SQR |
| EXP | value INTEGER\|FLOAT | result FLOAT | e raised to the power of `value`. | EXP |
| LOG | value INTEGER\|FLOAT | result FLOAT | Natural logarithm. `value` MUST be greater than 0. | LOG |
| SIN | value INTEGER\|FLOAT | result FLOAT | Sine of an angle in radians. | SIN |
| COS | value INTEGER\|FLOAT | result FLOAT | Cosine of an angle in radians. | COS |
| TAN | value INTEGER\|FLOAT | result FLOAT | Tangent of an angle in radians. | TAN |
| ATN | value INTEGER\|FLOAT | result FLOAT | Arc tangent in radians. | ATN |
| INT | value INTEGER\|FLOAT | result INTEGER | Largest integer less than or equal to `value` (rounds down: -12.85 → -13). An INTEGER is returned unchanged; a FLOAT too big for an INTEGER (or NaN) raises NUMERIC_OVERFLOW. | INT |
| FIX | value INTEGER\|FLOAT | result INTEGER | Removes the decimal places without rounding (-12.85 → -12). An INTEGER is returned unchanged; a FLOAT too big for an INTEGER (or NaN) raises NUMERIC_OVERFLOW. | FIX |
| ROUND | value INTEGER\|FLOAT | result INTEGER | Rounds to the nearest integer, .5 to the nearest even integer (12.5 → 12, 13.5 → 14). An INTEGER is returned unchanged; a FLOAT too big for an INTEGER (or NaN) raises NUMERIC_OVERFLOW. | CINT, CLNG |
| TO_FLOAT | value INTEGER\|FLOAT | result FLOAT | Converts a number to a float. | CDBL, CSNG |
| DIV | dividend INTEGER, divisor INTEGER | result INTEGER | Integer division, rounded towards 0. A divisor of 0 raises DIVISION_BY_ZERO. The smallest INTEGER divided by -1 raises NUMERIC_OVERFLOW. | – |
| RANDOM | – | result FLOAT | Next random number between 0 (inclusive) and 1 (exclusive). Each process has its own random number generator. | RND |
| RANDOMIZE | seed INTEGER | – | Sets the seed of the random number generator of the process, so that the same seed always produces the same sequence. | RANDOMIZE |
