# Block reference

[c0de concept](../concept.md) › Block reference

The block IDs are the names in lowercase with the suffix `_block`, e.g. `c0de:noop_block`, `c0de:type_boolean_block` or `c0de:operator_block`. Blocks with a valid and an invalid color have the block state `valid`. Most blocks are cubes with a symbol; PARAMETER, RESULT, BEGIN and END have the shape of their symbol instead. Right-clicking a block with an item in the main hand uses the item (e.g. places a block next to it); only an empty hand opens the dialog of a block.

| Block | Valid color | Invalid color | Symbol | Recipe |
|---|---|---|---|---|
| NOOP | transparent | – | metal corners, wooden edges | 4 iron ingots + 4 sticks → 64 |
| COMMENT | glass | – | yellow note with `//` | 8 NOOP around glass → 8 |
| LABEL | white #FFFFFF | gray #AAAAAA | name | NOOP + paper |
| PROGRAM | dark gray #555555 | black #000000 | book | NOOP + book |
| INCLUDE | dark gray #555555 | black #000000 | map | NOOP + empty map |
| EXIT | dark gray #555555 | black #000000 | iron door | 8 NOOP around iron door → 8 |
| BEGIN / END | yellow #FFFF55 | gold #FFAA00 | shape: trapezoid of four steps, BEGIN wide at the bottom, END wide at the top | 8 NOOP around yellow wool → 8 BEGIN; BEGIN ↔ END |
| BREAK | yellow #FFFF55 | gold #FFAA00 | broken stick | NOOP + stick |
| type base (item) | – | – | question mark | 8 NOOP around red carpet → 8 |
| TYPE_BOOLEAN | red | – | redstone torch | type base + redstone torch |
| TYPE_INTEGER | red | – | yellow sand | type base + sand; TYPE_FLOAT + yellow dye |
| TYPE_FLOAT | red | – | orange sand | type base + red sand; TYPE_INTEGER + orange dye |
| TYPE_CHAR | red | – | oak plank | type base + oak plank |
| TYPE_STRING | red | – | string | type base + string |
| TYPE_ARRAY | red | – | barrel | type base + barrel |
| TYPE_HASH | red | – | chest | type base + chest or trapped chest |
| TYPE_STRUCT | red | – | scaffolding | type base + scaffolding |
| TYPE_TIME | red | – | clock | type base + clock |
| TYPE_ERROR | red | – | campfire | type base + campfire |
| VARIABLE | blue #5555FF | dark blue #0000AA | square | type base + bone block; CONSTANT |
| CONSTANT | blue #5555FF | dark blue #0000AA | diamond | type base + 3 bones; VARIABLE |
| LITERAL | light gray #CCCCCC | gray #AAAAAA | value | 8 NOOP around white wool → 8 |
| TAG | brown #AA7744 | dark brown #553311 | price tag with language | 8 NOOP around brown wool → 8 |
| OPERATOR (all operators, ( ) and ;) | red #FF5555 | dark red #AA0000 | symbol chosen in a dialog | 8 NOOP around red wool → 8 |
| ASSIGN | aqua #55FFFF | dark aqua #00AAAA | triangle pointing to positive x; a dot on the east side and a cross on the west side | 8 NOOP around cyan wool → 8 |
| FUNCTION | light gray #AAAAAA | dark gray #555555 | ƒ | NOOP + stone |
| PARAMETER | light purple #FF55FF | dark purple #AA00AA | shape: hopper | 8 NOOP around hopper → 8; RESULT |
| RESULT | light purple #FF55FF | dark purple #AA00AA | shape: upside-down hopper | PARAMETER |
| RETURN | light purple #FF55FF | dark purple #AA00AA | spruce door | 8 NOOP around spruce door → 8 |
| CALL | light gray #AAAAAA | dark gray #555555 | 📞 | NOOP + smooth stone |
| structural base (item) | – | – | – | 8 NOOP around green wool → 8 |
| IF | green #55FF55 | dark green #00AA00 | ? | structural base + wheat seeds; ELSE |
| ELSE | green #55FF55 | dark green #00AA00 | ¿ | structural base + wheat; IF |
| FOR | green #55FF55 | dark green #00AA00 | circular arrow, # | structural base + dirt; EACH |
| EACH | green #55FF55 | dark green #00AA00 | circular arrow, cobblestone | structural base + cobblestone; FOR |
| WHILE | green #55FF55 | dark green #00AA00 | two circular arrows, ? | structural base + diorite; REPEAT |
| REPEAT | green #55FF55 | dark green #00AA00 | square, angled arrows, ? | structural base + granite; WHILE |
| trigger block | – | – | golden ball | copper, glass, gold ingot → 8 |
| timer block | – | – | clock | copper, glass, clock → 8 |
| debug trigger block | – | – | red ball | copper, glass, redstone block → 1 |
| PID block | #222222 | – | PID | not craftable |
