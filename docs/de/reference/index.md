# Handbücher

Die Handbücher von c0de: eine Seite für jeden Block, jeden Gegenstand und jede Funktion, jeweils mit Beschreibung, Rezept und Beispiel. Die [Anleitung](../guide/01-getting-started.md) erklärt, wie du Programme schreibst und startest. Mit der Mod Patchouli ist jedes Handbuch auch ein rotes Buch im Spiel (hergestellt aus einem Buch und dem unten genannten Gegenstand).

## [Daten](data.md)

Buch: hergestellt aus einem Buch und einer Typbasis.

- **Typen**: [Typbasis](data.md#typbasis), [TYPE_BOOLEAN](data.md#type_boolean), [TYPE_INTEGER](data.md#type_integer), [TYPE_FLOAT](data.md#type_float), [TYPE_CHAR](data.md#type_char), [TYPE_STRING](data.md#type_string), [TYPE_ARRAY](data.md#type_array), [TYPE_HASH](data.md#type_hash), [TYPE_STRUCT](data.md#type_struct), [TYPE_TIME](data.md#type_time), [TYPE_ERROR](data.md#type_error)
- **Werte und Namen**: [LABEL](data.md#label), [LITERAL](data.md#literal), [VARIABLE](data.md#variable), [CONSTANT](data.md#constant), [TAG](data.md#tag)

## [Anweisungen](statements.md)

Buch: hergestellt aus einem Buch und einer Strukturbasis.

- **Programmstruktur**: [NOOP](statements.md#noop), [COMMENT](statements.md#comment), [PROGRAM](statements.md#program), [INCLUDE](statements.md#include), [EXIT](statements.md#exit)
- **Codeblöcke**: [BEGIN](statements.md#begin), [END](statements.md#end)
- **Funktionen**: [FUNCTION](statements.md#function), [PARAMETER](statements.md#parameter), [RESULT](statements.md#result), [RETURN](statements.md#return), [CALL](statements.md#call)
- **Ausdrücke**: [ASSIGN](statements.md#assign), [OPERATOR](statements.md#operator)
- **Bedingungen**: [Strukturbasis](statements.md#strukturbasis), [IF](statements.md#if), [ELSE](statements.md#else)
- **Schleifen**: [FOR](statements.md#for), [EACH](statements.md#each), [WHILE](statements.md#while), [REPEAT](statements.md#repeat), [BREAK](statements.md#break)

## [Werkzeuge](tools.md)

Buch: hergestellt aus einem Buch und einem Schraubenschlüssel.

- **Bearbeitungswerkzeuge**: [Schraubenschlüssel](tools.md#schraubenschlüssel), [Textmarker](tools.md#textmarker), [Hammer](tools.md#hammer), [Zange](tools.md#zange), [Stiefel](tools.md#stiefel), [Debugging-Werkzeug](tools.md#debugging-werkzeug), [c0de-Diskette](tools.md#c0de-diskette), [Diskettenlaufwerk](tools.md#diskettenlaufwerk)
- **Karten**: [Datenkarte](tools.md#datenkarte), [Typkarten](tools.md#typkarten), [Variablenkarte](tools.md#variablenkarte), [Konstantenkarte](tools.md#konstantenkarte)
- **Hilfe**: [Anleitungsbuch](tools.md#anleitungsbuch)

## [Auslöser](triggers.md)

Buch: hergestellt aus einem Buch und einem Auslöserblock.

- **Programme starten**: [Auslöserblock](triggers.md#auslöserblock), [Zeitgeberblock](triggers.md#zeitgeberblock), [Debug-Auslöserblock](triggers.md#debug-auslöserblock)
- **Laufende Programme**: [PID-Block](triggers.md#pid-block)

## [Geräte](devices.md)

Buch: hergestellt aus einem Buch und einem Anzeigeblock.

- **Bildschirme**: [Anzeigeblock](devices.md#anzeigeblock), [Laptop](devices.md#laptop), [Tablet](devices.md#tablet)
- **Gegenstände**: [E/A-Block](devices.md#ea-block), [Literalkarte](devices.md#literalkarte)

## [Funktionen](functions.md)

Buch: hergestellt aus einem Buch und einem FUNCTION-Block.

- **Eingebaute Funktionen**: [LEN](functions.md#len), [HAS_KEY](functions.md#has_key), [REMOVE](functions.md#remove), [INSERT](functions.md#insert), [CLEAR](functions.md#clear), [SWAP](functions.md#swap), [RAISE](functions.md#raise)
- **Eingabe und Ausgabe**: [PRINT](functions.md#print), [WRITE](functions.md#write), [INPUT](functions.md#input), [CLS](functions.md#cls), [LOCATE](functions.md#locate), [CSRLIN](functions.md#csrlin), [POS](functions.md#pos), [COLOR](functions.md#color), [INKEY](functions.md#inkey)
- **c0de/math**: [PI](functions.md#pi), [ABS](functions.md#abs), [SGN](functions.md#sgn), [SQR](functions.md#sqr), [EXP](functions.md#exp), [LOG](functions.md#log), [SIN](functions.md#sin), [COS](functions.md#cos), [TAN](functions.md#tan), [ATN](functions.md#atn), [INT](functions.md#int), [FIX](functions.md#fix), [ROUND](functions.md#round), [TO_FLOAT](functions.md#to_float), [DIV](functions.md#div), [RANDOM](functions.md#random), [RANDOMIZE](functions.md#randomize)
- **c0de/strings**: [LEFT](functions.md#left), [RIGHT](functions.md#right), [MID](functions.md#mid), [REPLACE](functions.md#replace), [INSTR](functions.md#instr), [UCASE](functions.md#ucase), [LCASE](functions.md#lcase), [SPACE](functions.md#space), [FILL](functions.md#fill), [ASC](functions.md#asc), [CHR](functions.md#chr), [STR](functions.md#str), [FORMAT](functions.md#format), [VAL](functions.md#val), [HEX](functions.md#hex), [OCT](functions.md#oct), [TO_JSON](functions.md#to_json), [TO_YAML](functions.md#to_yaml), [FROM_JSON](functions.md#from_json), [FROM_YAML](functions.md#from_yaml)
- **c0de/time**: [NOW](functions.md#now), [MILLISECOND](functions.md#millisecond), [SECOND](functions.md#second), [MINUTE](functions.md#minute), [HOUR](functions.md#hour), [DAY](functions.md#day), [MONTH](functions.md#month), [YEAR](functions.md#year), [WEEKDAY](functions.md#weekday), [WEEK](functions.md#week), [TIMER](functions.md#timer), [SLEEP](functions.md#sleep)
- **c0de/minecraft**: [POSITION, ENTITY und ITEM_STACK](functions.md#position-entity-und-item_stack), [TICKS](functions.md#ticks), [BEEP](functions.md#beep), [SOUND](functions.md#sound), [SAY](functions.md#say), [GET_BLOCK](functions.md#get_block), [GET_INVENTORY](functions.md#get_inventory), [GET_LEVER](functions.md#get_lever), [SET_LEVER](functions.md#set_lever), [GET_BUTTON](functions.md#get_button), [PRESS_BUTTON](functions.md#press_button), [GET_PRESSURE_PLATE](functions.md#get_pressure_plate), [GET_OPEN](functions.md#get_open), [SET_OPEN](functions.md#set_open), [MOVE_ITEMS](functions.md#move_items), [DELETE_ITEMS](functions.md#delete_items), [PLACE_BLOCK](functions.md#place_block), [REMOVE_BLOCK](functions.md#remove_block), [MINE_BLOCK](functions.md#mine_block), [SPAWN_ITEM](functions.md#spawn_item), [SPAWN_ENTITY](functions.md#spawn_entity), [FIND_ENTITIES](functions.md#find_entities), [TELEPORT](functions.md#teleport)
