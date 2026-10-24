# Reference

The handbooks of c0de: one page per block, item and function, each with a description, its recipe and an example. The [guide](../guide/01-getting-started.md) explains how to write and run programs. With the mod Patchouli, every handbook is also a red book in the game (crafted from a book and the item named below).

## [Data](data.md)

Book: crafted from a book and type base.

- **Types**: [Type base](data.md#type-base), [TYPE_BOOLEAN](data.md#type_boolean), [TYPE_INTEGER](data.md#type_integer), [TYPE_FLOAT](data.md#type_float), [TYPE_CHAR](data.md#type_char), [TYPE_STRING](data.md#type_string), [TYPE_ARRAY](data.md#type_array), [TYPE_HASH](data.md#type_hash), [TYPE_STRUCT](data.md#type_struct), [TYPE_TIME](data.md#type_time), [TYPE_ERROR](data.md#type_error)
- **Values and names**: [LABEL](data.md#label), [LITERAL](data.md#literal), [VARIABLE](data.md#variable), [CONSTANT](data.md#constant), [TAG](data.md#tag)

## [Statements](statements.md)

Book: crafted from a book and structural base.

- **Program structure**: [NOOP](statements.md#noop), [COMMENT](statements.md#comment), [PROGRAM](statements.md#program), [INCLUDE](statements.md#include), [EXIT](statements.md#exit)
- **Code blocks**: [BEGIN](statements.md#begin), [END](statements.md#end)
- **Functions**: [FUNCTION](statements.md#function), [PARAMETER](statements.md#parameter), [RESULT](statements.md#result), [RETURN](statements.md#return), [CALL](statements.md#call)
- **Expressions**: [ASSIGN](statements.md#assign), [OPERATOR](statements.md#operator)
- **Conditions**: [Structural base](statements.md#structural-base), [IF](statements.md#if), [ELSE](statements.md#else)
- **Loops**: [FOR](statements.md#for), [EACH](statements.md#each), [WHILE](statements.md#while), [REPEAT](statements.md#repeat), [BREAK](statements.md#break)
- **Input and output**: [PRINT](statements.md#print), [WRITE](statements.md#write), [INPUT](statements.md#input)

## [Tools](tools.md)

Book: crafted from a book and wrench.

- **Editing tools**: [Wrench](tools.md#wrench), [Text marker](tools.md#text-marker), [Hammer](tools.md#hammer), [Debugging tool](tools.md#debugging-tool), [c0de disk](tools.md#c0de-disk)
- **Items**: [Data item](tools.md#data-item), [Type items](tools.md#type-items), [Variable item](tools.md#variable-item), [Constant item](tools.md#constant-item)
- **Help**: [Guide book](tools.md#guide-book)

## [Triggers](triggers.md)

Book: crafted from a book and trigger block.

- **Starting programs**: [Trigger block](triggers.md#trigger-block), [Timer block](triggers.md#timer-block), [Debug trigger block](triggers.md#debug-trigger-block)
- **Running programs**: [PID block](triggers.md#pid-block)

## [Devices](devices.md)

Book: crafted from a book and display block.

- **Screens**: [Display block](devices.md#display-block), [Laptop](devices.md#laptop), [Tablet](devices.md#tablet)
- **Items**: [I/O block](devices.md#io-block), [Literal item](devices.md#literal-item)

## [Functions](functions.md)

Book: crafted from a book and function block.

- **Built-in functions**: [LEN](functions.md#len), [HAS_KEY](functions.md#has_key), [REMOVE](functions.md#remove), [INSERT](functions.md#insert), [CLEAR](functions.md#clear), [SWAP](functions.md#swap), [RAISE](functions.md#raise)
- **Screen functions**: [CLS](functions.md#cls), [LOCATE](functions.md#locate), [CSRLIN](functions.md#csrlin), [POS](functions.md#pos), [COLOR](functions.md#color), [INKEY](functions.md#inkey)
- **c0de/math**: [ABS](functions.md#abs), [SGN](functions.md#sgn), [SQR](functions.md#sqr), [EXP](functions.md#exp), [LOG](functions.md#log), [SIN](functions.md#sin), [COS](functions.md#cos), [TAN](functions.md#tan), [ATN](functions.md#atn), [INT](functions.md#int), [FIX](functions.md#fix), [ROUND](functions.md#round), [TO_FLOAT](functions.md#to_float), [DIV](functions.md#div), [RANDOM](functions.md#random), [RANDOMIZE](functions.md#randomize)
- **c0de/strings**: [LEFT](functions.md#left), [RIGHT](functions.md#right), [MID](functions.md#mid), [REPLACE](functions.md#replace), [INSTR](functions.md#instr), [UCASE](functions.md#ucase), [LCASE](functions.md#lcase), [SPACE](functions.md#space), [FILL](functions.md#fill), [ASC](functions.md#asc), [CHR](functions.md#chr), [STR](functions.md#str), [FORMAT](functions.md#format), [VAL](functions.md#val), [HEX](functions.md#hex), [OCT](functions.md#oct), [TO_JSON](functions.md#to_json), [TO_YAML](functions.md#to_yaml), [FROM_JSON](functions.md#from_json), [FROM_YAML](functions.md#from_yaml)
- **c0de/time**: [NOW](functions.md#now), [MILLISECOND](functions.md#millisecond), [SECOND](functions.md#second), [MINUTE](functions.md#minute), [HOUR](functions.md#hour), [DAY](functions.md#day), [MONTH](functions.md#month), [YEAR](functions.md#year), [WEEKDAY](functions.md#weekday), [WEEK](functions.md#week), [TIMER](functions.md#timer), [SLEEP](functions.md#sleep)
- **c0de/minecraft**: [TICKS](functions.md#ticks), [BEEP](functions.md#beep), [SOUND](functions.md#sound), [SAY](functions.md#say), [GET_BLOCK](functions.md#get_block), [GET_INVENTORY](functions.md#get_inventory), [GET_LEVER](functions.md#get_lever), [SET_LEVER](functions.md#set_lever), [GET_BUTTON](functions.md#get_button), [PRESS_BUTTON](functions.md#press_button), [GET_PRESSURE_PLATE](functions.md#get_pressure_plate), [GET_OPEN](functions.md#get_open), [SET_OPEN](functions.md#set_open), [MOVE_ITEMS](functions.md#move_items), [DELETE_ITEMS](functions.md#delete_items), [PLACE_BLOCK](functions.md#place_block), [REMOVE_BLOCK](functions.md#remove_block), [SPAWN_ITEM](functions.md#spawn_item), [SPAWN_ENTITY](functions.md#spawn_entity), [FIND_ENTITIES](functions.md#find_entities), [TELEPORT](functions.md#teleport)
