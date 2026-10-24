# Functions

A function call runs a function with a CALL line. The PARAMETER lines below it give the values the function needs. The RESULT lines name the variables that receive what the function returns. Results you don't list are thrown away.

Built-in functions work everywhere. Library functions need an INCLUDE line for their library first, e.g. `INCLUDE c0de/math`. Then you call them by their full name, e.g. `c0de/math/SIN`. If a call fails and has no error label, the error is raised.

## Built-in functions

Built-in functions work with arrays, hashes and errors and need no INCLUDE.

### LEN

Counts the characters of a string or the elements of an array or hash.

**Parameters and results**

- `value` STRING|ARRAY|HASH: the string, array or hash to count
- result `result` INTEGER: the number of characters or elements

**Example**

```c0de
CALL LEN
    PARAMETER value names ;
    RESULT result count
```

### HAS_KEY

Checks if a hash contains a key.

**Parameters and results**

- `hash` HASH: the hash to look in
- `key` STRING: the key to look for
- result `result` BOOLEAN: true if the key exists

**Example**

```c0de
CALL HAS_KEY
    PARAMETER hash stock ;
    PARAMETER key "gold" ;
    RESULT result hasGold
```

### REMOVE

Removes one element from an array or a hash.

Use `index` for an array and `key` for a hash. The following array elements move down by one. A missing index raises INDEX_OUT_OF_BOUNDS, a missing key raises UNKNOWN_KEY. Leaving out `index` or `key` raises INVALID_ARGUMENT.

**Parameters and results**

- `container` ARRAY|HASH: the array or hash
- `index` INTEGER: position of the element to remove (optional, needed for arrays)
- `key` STRING: key of the element to remove (optional, needed for hashes)
- result `result` like `container`: the array or hash without the element

**Example**

```c0de
CALL REMOVE
    PARAMETER container list ;
    PARAMETER index 1 ;
    RESULT result list
```

### INSERT

Inserts a value into an array.

The following elements move up by one. `index` must be between 0 and the length of the array, otherwise INDEX_OUT_OF_BOUNDS is raised. Use the length to add the value at the end.

**Parameters and results**

- `array` ARRAY: the array
- `index` INTEGER: the position of the new element
- `value` the type of the elements: the value to insert
- result `result` like `array`: the array with the new element

**Example**

```c0de
CALL INSERT
    PARAMETER array list ;
    PARAMETER index 0 ;
    PARAMETER value 42 ;
    RESULT result list
```

### CLEAR

Returns an empty array or hash of the same type.

**Parameters and results**

- `container` ARRAY|HASH: the array or hash
- result `result` like `container`: an empty array or hash

**Example**

```c0de
CALL CLEAR
    PARAMETER container stock ;
    RESULT result stock
```

### SWAP

Swaps two values.

Both values must have the same type, otherwise TYPE_MISMATCH is raised. To swap two variables, use them as parameters and as result targets.

**Parameters and results**

- `a` any type: the first value
- `b` the type of `a`: the second value
- result `a`: the value of `b`
- result `b`: the value of `a`

**Example**

```c0de
CALL SWAP
    PARAMETER a left ;
    PARAMETER b right ;
    RESULT a left
    RESULT b right
```

### RAISE

Returns your own error to the caller of the function.

The caller raises the error or stores it in the error label of its CALL line. `code` must be 1000 or higher, otherwise INVALID_ARGUMENT is raised. The error gets the context and line of the CALL statement.

**Parameters and results**

- `code` INTEGER: the error code, 1000 or higher
- `message` STRING: the error message (optional, default empty)

**Example**

```c0de
CALL RAISE
    PARAMETER code 1001 ;
    PARAMETER message "too big" ;
```

## c0de/math

The math library has number functions and random numbers and needs INCLUDE c0de/math.

### ABS

Returns the absolute value of a number.

A result that doesn't fit into an INTEGER raises NUMERIC_OVERFLOW.

**Parameters and results**

- `value` INTEGER|FLOAT: the number
- result `result` like `value`: the number without its sign

**Example**

```c0de
CALL c0de/math/ABS
    PARAMETER value distance ;
    RESULT result steps
```

### SGN

Returns the sign of a number.

The result is -1 for negative numbers, 0 for 0 and 1 for positive numbers.

**Parameters and results**

- `value` INTEGER|FLOAT: the number
- result `result` INTEGER: -1, 0 or 1

**Example**

```c0de
CALL c0de/math/SGN
    PARAMETER value balance ;
    RESULT result sign
```

### SQR

Returns the square root of a number.

A negative number raises INVALID_ARGUMENT.

**Parameters and results**

- `value` INTEGER|FLOAT: the number, not negative
- result `result` FLOAT: the square root

**Example**

```c0de
CALL c0de/math/SQR
    PARAMETER value area ;
    RESULT result side
```

### EXP

Returns e raised to the power of a number.

**Parameters and results**

- `value` INTEGER|FLOAT: the power
- result `result` FLOAT: e to the power of `value`

**Example**

```c0de
CALL c0de/math/EXP
    PARAMETER value rate ;
    RESULT result growth
```

### LOG

Returns the natural logarithm of a number.

The number must be greater than 0, otherwise INVALID_ARGUMENT is raised.

**Parameters and results**

- `value` INTEGER|FLOAT: the number, greater than 0
- result `result` FLOAT: the natural logarithm

**Example**

```c0de
CALL c0de/math/LOG
    PARAMETER value amount ;
    RESULT result logarithm
```

### SIN

Returns the sine of an angle.

The angle is in radians. A full circle is 2 × `c0de/math/PI`.

**Parameters and results**

- `value` INTEGER|FLOAT: the angle in radians
- result `result` FLOAT: the sine

**Example**

```c0de
CALL c0de/math/SIN
    PARAMETER value angle ;
    RESULT result sine
```

### COS

Returns the cosine of an angle.

The angle is in radians. A full circle is 2 × `c0de/math/PI`.

**Parameters and results**

- `value` INTEGER|FLOAT: the angle in radians
- result `result` FLOAT: the cosine

**Example**

```c0de
CALL c0de/math/COS
    PARAMETER value angle ;
    RESULT result cosine
```

### TAN

Returns the tangent of an angle.

The angle is in radians. A full circle is 2 × `c0de/math/PI`.

**Parameters and results**

- `value` INTEGER|FLOAT: the angle in radians
- result `result` FLOAT: the tangent

**Example**

```c0de
CALL c0de/math/TAN
    PARAMETER value angle ;
    RESULT result tangent
```

### ATN

Returns the arc tangent of a number.

The result is an angle in radians.

**Parameters and results**

- `value` INTEGER|FLOAT: the tangent
- result `result` FLOAT: the angle in radians

**Example**

```c0de
CALL c0de/math/ATN
    PARAMETER value slope ;
    RESULT result angle
```

### INT

Rounds a number down to an integer.

-12.85 becomes -13. A number too large for an INTEGER raises NUMERIC_OVERFLOW.

**Parameters and results**

- `value` INTEGER|FLOAT: the number
- result `result` INTEGER: the largest integer not greater than `value`

**Example**

```c0de
CALL c0de/math/INT
    PARAMETER value height ;
    RESULT result blocks
```

### FIX

Cuts off the decimal places of a number.

There is no rounding: -12.85 becomes -12. A number too large for an INTEGER raises NUMERIC_OVERFLOW.

**Parameters and results**

- `value` INTEGER|FLOAT: the number
- result `result` INTEGER: the number without decimal places

**Example**

```c0de
CALL c0de/math/FIX
    PARAMETER value price ;
    RESULT result coins
```

### ROUND

Rounds a number to the nearest integer.

A .5 goes to the nearest even integer: 12.5 becomes 12, 13.5 becomes 14. A number too large for an INTEGER raises NUMERIC_OVERFLOW.

**Parameters and results**

- `value` INTEGER|FLOAT: the number
- result `result` INTEGER: the rounded number

**Example**

```c0de
CALL c0de/math/ROUND
    PARAMETER value 12.5 ;
    RESULT result rounded
```

### TO_FLOAT

Converts a number to a FLOAT.

**Parameters and results**

- `value` INTEGER|FLOAT: the number
- result `result` FLOAT: the same number as FLOAT

**Example**

```c0de
CALL c0de/math/TO_FLOAT
    PARAMETER value count ;
    RESULT result amount
```

### DIV

Divides two integers and rounds towards 0.

-7 divided by 2 is -3. A divisor of 0 raises DIVISION_BY_ZERO.

**Parameters and results**

- `dividend` INTEGER: the number to divide
- `divisor` INTEGER: the number to divide by, not 0
- result `result` INTEGER: the quotient without remainder

**Example**

```c0de
CALL c0de/math/DIV
    PARAMETER dividend items ;
    PARAMETER divisor 64 ;
    RESULT result stacks
```

### RANDOM

Returns a random number between 0 and 1.

0 is possible, 1 is not. Each process has its own random number generator.

**Parameters and results**

- result `result` FLOAT: a random number from 0 up to below 1

**Example**

```c0de
CALL c0de/math/RANDOM
    RESULT result chance
```

### RANDOMIZE

Sets the seed of the random number generator.

The same seed always gives the same sequence of random numbers. This only affects the current process.

**Parameters and results**

- `seed` INTEGER: the seed

**Example**

```c0de
CALL c0de/math/RANDOMIZE
    PARAMETER seed 42 ;
```

## c0de/strings

The strings library has text functions, number conversions, JSON and YAML and needs INCLUDE c0de/strings.

### LEFT

Returns the first characters of a text.

If the text is shorter, you get the whole text. A negative length raises INVALID_ARGUMENT.

**Parameters and results**

- `text` STRING: the text
- `length` INTEGER: the number of characters
- result `result` STRING: the first `length` characters

**Example**

```c0de
CALL c0de/strings/LEFT
    PARAMETER text name ;
    PARAMETER length 3 ;
    RESULT result short
```

### RIGHT

Returns the last characters of a text.

If the text is shorter, you get the whole text. A negative length raises INVALID_ARGUMENT.

**Parameters and results**

- `text` STRING: the text
- `length` INTEGER: the number of characters
- result `result` STRING: the last `length` characters

**Example**

```c0de
CALL c0de/strings/RIGHT
    PARAMETER text name ;
    PARAMETER length 3 ;
    RESULT result ending
```

### MID

Returns a part of a text.

Positions start at 0. Near the end you get fewer characters. A start behind the end gives an empty text. A negative start or length raises INVALID_ARGUMENT.

**Parameters and results**

- `text` STRING: the text
- `start` INTEGER: the position of the first character
- `length` INTEGER: the number of characters (optional, default the rest of the text)
- result `result` STRING: the part of the text

**Example**

```c0de
CALL c0de/strings/MID
    PARAMETER text name ;
    PARAMETER start 2 ;
    PARAMETER length 3 ;
    RESULT result part
```

### REPLACE

Overwrites characters of a text with another text.

The length of the text stays the same. Characters of `insert` that would make it longer are dropped. A start behind the end of the text raises INVALID_ARGUMENT.

**Parameters and results**

- `text` STRING: the text
- `start` INTEGER: the position where overwriting begins
- `insert` STRING: the new characters
- result `result` STRING: the changed text

**Example**

```c0de
CALL c0de/strings/REPLACE
    PARAMETER text "Hello Alex" ;
    PARAMETER start 6 ;
    PARAMETER insert "Stev" ;
    RESULT result greeting
```

### INSTR

Finds a text inside another text.

The result is the position of the first match, or -1 if there is none.

**Parameters and results**

- `text` STRING: the text to search in
- `search` STRING: the text to find
- `start` INTEGER: where the search begins (optional, default 0)
- result `result` INTEGER: the position, or -1

**Example**

```c0de
CALL c0de/strings/INSTR
    PARAMETER text message ;
    PARAMETER search "diamond" ;
    RESULT result position
```

### UCASE

Converts a text to uppercase.

**Parameters and results**

- `text` STRING: the text
- result `result` STRING: the text in uppercase letters

**Example**

```c0de
CALL c0de/strings/UCASE
    PARAMETER text command ;
    RESULT result command
```

### LCASE

Converts a text to lowercase.

**Parameters and results**

- `text` STRING: the text
- result `result` STRING: the text in lowercase letters

**Example**

```c0de
CALL c0de/strings/LCASE
    PARAMETER text answer ;
    RESULT result answer
```

### SPACE

Returns a text of spaces.

`count` must be between 0 and 1,000,000, otherwise INVALID_ARGUMENT is raised.

**Parameters and results**

- `count` INTEGER: the number of spaces
- result `result` STRING: the spaces

**Example**

```c0de
CALL c0de/strings/SPACE
    PARAMETER count 8 ;
    RESULT result indent
```

### FILL

Returns a text that repeats one character.

`count` must be between 0 and 1,000,000, otherwise INVALID_ARGUMENT is raised.

**Parameters and results**

- `count` INTEGER: how often the character is repeated
- `char` CHAR: the character
- result `result` STRING: the repeated character

**Example**

```c0de
CALL c0de/strings/FILL
    PARAMETER count 20 ;
    PARAMETER char '-' ;
    RESULT result line
```

### ASC

Returns the Unicode code point of a character.

**Parameters and results**

- `char` CHAR: the character
- result `result` INTEGER: its code point, e.g. 65 for A

**Example**

```c0de
CALL c0de/strings/ASC
    PARAMETER char letter ;
    RESULT result code
```

### CHR

Returns the character with a Unicode code point.

A number that is not a code point raises INVALID_ARGUMENT.

**Parameters and results**

- `code` INTEGER: the code point
- result `result` CHAR: the character

**Example**

```c0de
CALL c0de/strings/CHR
    PARAMETER code 65 ;
    RESULT result letter
```

### STR

Converts a value to text.

The text is the same as the ⊕ operator creates.

**Parameters and results**

- `value` BOOLEAN|INTEGER|FLOAT|CHAR|TIME: the value
- result `result` STRING: the value as text

**Example**

```c0de
CALL c0de/strings/STR
    PARAMETER value score ;
    RESULT result scoreText
```

### FORMAT

Formats a value as text with a format string.

Conversions start with `%`, e.g. `%5d` (integer, 5 wide), `%.2f` (2 decimal places), `%s` (any value), `%tF` and `%tT` (date and time). A wrong type raises TYPE_MISMATCH, a bad format string INVALID_ARGUMENT.

**Parameters and results**

- `value` BOOLEAN|INTEGER|FLOAT|CHAR|STRING|TIME: the value
- `format` STRING: the format string
- result `result` STRING: the formatted text

**Example**

```c0de
CALL c0de/strings/FORMAT
    PARAMETER value now ;
    PARAMETER format "%tF %tT" ;
    RESULT result text
```

### VAL

Converts a text to a number.

`isInteger` is true if the text is a whole number. Use FIX to turn the value into an INTEGER then. A text that is not a number raises INVALID_ARGUMENT.

**Parameters and results**

- `text` STRING: the text, e.g. "42" or "3.5"
- result `value` FLOAT: the number
- result `isInteger` BOOLEAN: true if the text is a whole number

**Example**

```c0de
CALL c0de/strings/VAL
    PARAMETER text input ;
    RESULT value number
    RESULT isInteger whole
```

### HEX

Returns the hexadecimal text of an integer.

Letters are uppercase. Negative numbers use two's complement, so -1 becomes FFFFFFFFFFFFFFFF.

**Parameters and results**

- `value` INTEGER: the number
- result `result` STRING: the hexadecimal text

**Example**

```c0de
CALL c0de/strings/HEX
    PARAMETER value 255 ;
    RESULT result hexText
```

### OCT

Returns the octal text of an integer.

Negative numbers use two's complement.

**Parameters and results**

- `value` INTEGER: the number
- result `result` STRING: the octal text

**Example**

```c0de
CALL c0de/strings/OCT
    PARAMETER value 8 ;
    RESULT result octText
```

### TO_JSON

Converts a value to a JSON document.

With `indent` 0 the document is one line. If `value` is a single variable with a json tag, the value is placed at the path of the tag. `indent` outside 0 to 16 raises INVALID_ARGUMENT.

**Parameters and results**

- `value` any type: the value to convert
- `indent` INTEGER: spaces per level, 0 to 16 (optional, default 0)
- result `result` STRING: the JSON document

**Example**

```c0de
CALL c0de/strings/TO_JSON
    PARAMETER value config ;
    PARAMETER indent 2 ;
    RESULT result json
```

### TO_YAML

Converts a value to a YAML document.

The document uses block style with two spaces indentation. If `value` is a single variable with a yaml tag, the value is placed at the path of the tag.

**Parameters and results**

- `value` any type: the value to convert
- result `result` STRING: the YAML document

**Example**

```c0de
CALL c0de/strings/TO_YAML
    PARAMETER value config ;
    RESULT result yaml
```

### FROM_JSON

Reads a JSON document into a variable.

`destination` must be a single variable name; the function writes into it directly. The document must fit the type of the variable. On an error (TYPE_MISMATCH, INVALID_ARGUMENT, UNKNOWN_KEY, STRING_TOO_LONG) the variable stays unchanged.

**Parameters and results**

- `source` STRING: the JSON document
- `destination` variable: the variable that receives the values
- no results

**Example**

```c0de
CALL c0de/strings/FROM_JSON readError
    PARAMETER source config ;
    PARAMETER destination port ;
```

### FROM_YAML

Reads a YAML document into a variable.

`destination` must be a single variable name; the function writes into it directly. Only the first document is read. On an error (TYPE_MISMATCH, INVALID_ARGUMENT, UNKNOWN_KEY, STRING_TOO_LONG) the variable stays unchanged.

**Parameters and results**

- `source` STRING: the YAML document
- `destination` variable: the variable that receives the values
- no results

**Example**

```c0de
CALL c0de/strings/FROM_YAML readError
    PARAMETER source config ;
    PARAMETER destination settings ;
```

## c0de/time

The time library has real-world time and date functions and SLEEP and needs INCLUDE c0de/time.

### NOW

Returns the current time.

The TIME value is a Unix timestamp in milliseconds. Use FORMAT to turn it into text.

**Parameters and results**

- result `result` TIME: the current time

**Example**

```c0de
CALL c0de/time/NOW
    RESULT result now
```

### MILLISECOND

Returns the milliseconds of a time (0 to 999).

Without `time` the function uses the current time. It uses the time zone of the server.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the millisecond, 0 to 999

**Example**

```c0de
CALL c0de/time/MILLISECOND
    PARAMETER time start ;
    RESULT result millis
```

### SECOND

Returns the second of a time (0 to 59).

Without `time` the function uses the current time. It uses the time zone of the server.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the second, 0 to 59

**Example**

```c0de
CALL c0de/time/SECOND
    PARAMETER time start ;
    RESULT result second
```

### MINUTE

Returns the minute of a time (0 to 59).

Without `time` the function uses the current time. It uses the time zone of the server.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the minute, 0 to 59

**Example**

```c0de
CALL c0de/time/MINUTE
    RESULT result minute
```

### HOUR

Returns the hour of a time (0 to 23).

Without `time` the function uses the current time. It uses the time zone of the server.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the hour, 0 to 23

**Example**

```c0de
CALL c0de/time/HOUR
    RESULT result hour
```

### DAY

Returns the day of the month (1 to 31).

Without `time` the function uses the current time. It uses the time zone of the server.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the day, 1 to 31

**Example**

```c0de
CALL c0de/time/DAY
    PARAMETER time birthday ;
    RESULT result day
```

### MONTH

Returns the month of a time (1 to 12).

Without `time` the function uses the current time. It uses the time zone of the server.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the month, 1 to 12

**Example**

```c0de
CALL c0de/time/MONTH
    PARAMETER time birthday ;
    RESULT result month
```

### YEAR

Returns the year of a time, e.g. 2026.

Without `time` the function uses the current time. It uses the time zone of the server.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the year

**Example**

```c0de
CALL c0de/time/YEAR
    RESULT result year
```

### WEEKDAY

Returns the day of the week, from 1 (Monday) to 7 (Sunday).

Without `time` the function uses the current time. It uses the time zone of the server.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the day of the week, 1 to 7

**Example**

```c0de
CALL c0de/time/WEEKDAY
    PARAMETER time t ;
    RESULT result weekday
```

### WEEK

Returns the ISO 8601 week of the year (1 to 53).

Week 1 contains the first Thursday of the year. So days around New Year can belong to week 52 or 53 of the old year or week 1 of the new one. Without `time` the current time is used.

**Parameters and results**

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the week, 1 to 53

**Example**

```c0de
CALL c0de/time/WEEK
    RESULT result week
```

### TIMER

Returns the seconds since midnight.

This is the real time of the server, not the game time. Use TICKS from c0de/minecraft for the game time.

**Parameters and results**

- result `result` FLOAT: the seconds since midnight

**Example**

```c0de
CALL c0de/time/TIMER
    RESULT result seconds
```

### SLEEP

Pauses the program for a number of game ticks.

20 ticks are about one second. `ticks` must be between 1 and 72000 (one hour), otherwise INVALID_ARGUMENT is raised.

**Parameters and results**

- `ticks` INTEGER: the number of game ticks, 1 to 72000

**Example**

```c0de
CALL c0de/time/SLEEP
    PARAMETER ticks 20 ;
```

## c0de/minecraft

The Minecraft library acts in the world for the owner of the trigger block, uses the struct type c0de/minecraft/POSITION (x, y, z, dimension) for places and needs INCLUDE c0de/minecraft.

### TICKS

Returns the game time in ticks.

It is the game time of the dimension of the trigger block.

**Parameters and results**

- result `result` INTEGER: the game time in ticks

**Example**

```c0de
CALL c0de/minecraft/TICKS
    RESULT result ticks
```

### BEEP

Plays a short bell sound at the trigger block.

**Parameters and results**

- no parameters and no results

**Example**

```c0de
CALL c0de/minecraft/BEEP
```

### SOUND

Plays a note at the trigger block like a note block.

`note` is 0 to 24, like the clicks on a note block. Instruments are note block instruments such as harp, bass, bell or flute. A wrong note or instrument raises INVALID_ARGUMENT.

**Parameters and results**

- `note` INTEGER: the note, 0 to 24
- `instrument` STRING: the instrument (optional, default "harp")

**Example**

```c0de
CALL c0de/minecraft/SOUND
    PARAMETER note 12 ;
    PARAMETER instrument "bell" ;
```

### SAY

Writes a message to the chat.

The message starts with the program name and PID. With `player`, only that player gets it; if they are not online, INVALID_ARGUMENT is raised. Otherwise all players near the trigger block get it (64 blocks by default).

**Parameters and results**

- `message` STRING: the message
- `player` STRING: the name of one player (optional, default empty for all players nearby)

**Example**

```c0de
CALL c0de/minecraft/SAY
    PARAMETER message "The farm is full!" ;
    PARAMETER player "Alex" ;
```

### GET_BLOCK

Reads the block at a position.

You get its ID, its block state properties as text and the redstone power it receives. An unloaded chunk raises CHUNK_NOT_LOADED. An empty `dimension` in the position means the dimension of the trigger block.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the block is
- result `id` STRING: the block ID, e.g. minecraft:oak_door
- result `properties` HASH STRING: the block states, e.g. facing is north
- result `power` INTEGER: the redstone power it receives, 0 to 15

**Example**

```c0de
CALL c0de/minecraft/GET_BLOCK
    PARAMETER position spot ;
    RESULT id blockId
    RESULT properties states
    RESULT power power
```

### GET_INVENTORY

Reads the inventory of a block, such as a chest.

You get one ITEM_STACK (id, count) per slot. An empty slot has the id "" and the count 0. A block without inventory raises INVALID_ARGUMENT.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the block is
- result `result` ARRAY STRUCT c0de/minecraft/ITEM_STACK: the slots

**Example**

```c0de
CALL c0de/minecraft/GET_INVENTORY
    PARAMETER position chest ;
    RESULT result slots
```

### GET_LEVER

Checks if a lever is on.

A block that is not a lever raises INVALID_ARGUMENT.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the lever is
- result `result` BOOLEAN: true if the lever is on

**Example**

```c0de
CALL c0de/minecraft/GET_LEVER
    PARAMETER position lever ;
    RESULT result isOn
```

### SET_LEVER

Switches a lever on or off.

A block that is not a lever raises INVALID_ARGUMENT. Levers on the sheets of the c0de dimension can't be changed (PERMISSION_DENIED).

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the lever is
- `on` BOOLEAN: true to switch it on, false to switch it off

**Example**

```c0de
CALL c0de/minecraft/SET_LEVER
    PARAMETER position lever ;
    PARAMETER on TRUE ;
```

### GET_BUTTON

Checks if a button is pressed.

A block that is not a button raises INVALID_ARGUMENT.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the button is
- result `result` BOOLEAN: true if the button is pressed

**Example**

```c0de
CALL c0de/minecraft/GET_BUTTON
    PARAMETER position button ;
    RESULT result pressed
```

### PRESS_BUTTON

Presses a button like a player would.

A block that is not a button raises INVALID_ARGUMENT. Buttons on the sheets of the c0de dimension can't be pressed (PERMISSION_DENIED).

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the button is

**Example**

```c0de
CALL c0de/minecraft/PRESS_BUTTON
    PARAMETER position button ;
```

### GET_PRESSURE_PLATE

Reads the signal strength of a pressure plate.

Pressure plates can only be read; entities press them. A block that is not a pressure plate raises INVALID_ARGUMENT.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the pressure plate is
- result `result` INTEGER: the signal strength, 0 to 15

**Example**

```c0de
CALL c0de/minecraft/GET_PRESSURE_PLATE
    PARAMETER position plate ;
    RESULT result strength
```

### GET_OPEN

Checks if a door, trapdoor or fence gate is open.

Any other block raises INVALID_ARGUMENT.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the door is
- result `result` BOOLEAN: true if it is open

**Example**

```c0de
CALL c0de/minecraft/GET_OPEN
    PARAMETER position door ;
    RESULT result isOpen
```

### SET_OPEN

Opens or closes a door, trapdoor or fence gate.

Any other block raises INVALID_ARGUMENT. Iron doors and iron trapdoors need the permission allowBlockChanges or an administrator as owner, otherwise PERMISSION_DENIED is raised.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the door is
- `open` BOOLEAN: true to open, false to close

**Example**

```c0de
CALL c0de/minecraft/SET_OPEN
    PARAMETER position door ;
    PARAMETER open TRUE ;
```

### MOVE_ITEMS

Moves items from one inventory to another.

It moves up to `count` items and returns how many were moved. This needs allowInventoryChanges or an administrator as owner, otherwise PERMISSION_DENIED is raised. Blocks without inventory raise INVALID_ARGUMENT.

**Parameters and results**

- `from` STRUCT c0de/minecraft/POSITION: the block to take items from
- `to` STRUCT c0de/minecraft/POSITION: the block to put items into
- `id` STRING: the item ID, e.g. minecraft:wheat
- `count` INTEGER: the most items to move
- result `result` INTEGER: the number of moved items

**Example**

```c0de
CALL c0de/minecraft/MOVE_ITEMS
    PARAMETER from farmChest ;
    PARAMETER to storage ;
    PARAMETER id "minecraft:wheat" ;
    PARAMETER count 64 ;
    RESULT result moved
```

### DELETE_ITEMS

Removes items from an inventory.

It removes up to `count` items and returns how many were removed. This needs allowInventoryChanges or an administrator as owner, otherwise PERMISSION_DENIED is raised.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: the block with the inventory
- `id` STRING: the item ID, e.g. minecraft:rotten_flesh
- `count` INTEGER: the most items to remove
- result `result` INTEGER: the number of removed items

**Example**

```c0de
CALL c0de/minecraft/DELETE_ITEMS
    PARAMETER position trashChest ;
    PARAMETER id "minecraft:rotten_flesh" ;
    PARAMETER count 64 ;
    RESULT result removed
```

### PLACE_BLOCK

Places a block at a position.

The position must hold air or a replaceable block like grass or water, otherwise INVALID_ARGUMENT is raised. This needs allowBlockChanges or an administrator as owner, otherwise PERMISSION_DENIED is raised.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the block goes
- `id` STRING: the block ID, e.g. minecraft:stone
- `properties` HASH STRING: block states such as facing (optional, default empty)

**Example**

```c0de
CALL c0de/minecraft/PLACE_BLOCK
    PARAMETER position spot ;
    PARAMETER id "minecraft:torch" ;
```

### REMOVE_BLOCK

Removes the block at a position.

With `drop`, the block drops as an item like when it is mined. This needs allowBlockChanges or an administrator as owner, otherwise PERMISSION_DENIED is raised.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the block is
- `drop` BOOLEAN: true to drop the block as an item (optional, default TRUE)

**Example**

```c0de
CALL c0de/minecraft/REMOVE_BLOCK
    PARAMETER position spot ;
    PARAMETER drop FALSE ;
```

### SPAWN_ITEM

Spawns items on the ground at a position.

`count` must be 1 to 64. The result is the UUID of the new item entity. This needs allowSpawning or an administrator as owner, otherwise PERMISSION_DENIED is raised.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the items appear
- `id` STRING: the item ID, e.g. minecraft:bread
- `count` INTEGER: the number of items, 1 to 64
- result `result` STRING: the UUID of the item entity

**Example**

```c0de
CALL c0de/minecraft/SPAWN_ITEM
    PARAMETER position spot ;
    PARAMETER id "minecraft:bread" ;
    PARAMETER count 3 ;
    RESULT result itemId
```

### SPAWN_ENTITY

Spawns an entity, such as a pig, at a position.

The result is the UUID of the new entity. This needs allowSpawning or an administrator as owner, otherwise PERMISSION_DENIED is raised. An unknown type raises INVALID_ARGUMENT.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: where the entity appears
- `type` STRING: the entity type, e.g. minecraft:pig
- result `result` STRING: the UUID of the entity

**Example**

```c0de
CALL c0de/minecraft/SPAWN_ENTITY
    PARAMETER position pen ;
    PARAMETER type "minecraft:pig" ;
    RESULT result pigId
```

### FIND_ENTITIES

Finds all entities near a position.

Items count as entities too. Each ENTITY has uuid, type, name and position. `radius` must be 1 to 64, otherwise INVALID_ARGUMENT is raised.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: the center of the search
- `radius` INTEGER: the search radius in blocks, 1 to 64
- `type` STRING: only this entity type (optional, default empty for all types)
- result `result` ARRAY STRUCT c0de/minecraft/ENTITY: the entities found

**Example**

```c0de
CALL c0de/minecraft/FIND_ENTITIES
    PARAMETER position pen ;
    PARAMETER radius 8 ;
    PARAMETER type "minecraft:sheep" ;
    RESULT result sheep
```

### TELEPORT

Teleports an entity or item to a position.

This needs allowTeleporting or an administrator as owner. Only administrators can teleport players. Both raise PERMISSION_DENIED otherwise. An entity that is not loaded raises INVALID_ARGUMENT.

**Parameters and results**

- `uuid` STRING: the UUID of the entity
- `position` STRUCT c0de/minecraft/POSITION: the destination

**Example**

```c0de
CALL c0de/minecraft/TELEPORT
    PARAMETER uuid pigId ;
    PARAMETER position barn ;
```
