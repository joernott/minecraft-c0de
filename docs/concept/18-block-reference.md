# Block reference

[c0de concept](../concept.md) › Block reference

The block IDs are the names in lowercase with the suffix `_block`, e.g. `c0de:noop_block`, `c0de:type_boolean_block` or `c0de:operator_block`.

The language blocks are as high as a slab. Most of them are slabs with their symbol on the top and plain sides; some have the shape of their symbol instead. Their color doesn't show whether a line is valid; the [line numbers](01-dimension.md#line-numbers) on the floor show it. The validated blocks have the block states `valid` and `incomplete` for the line numbers. Right-clicking a block with an item in the main hand uses the item (e.g. places a block next to it); only an empty hand opens the dialog of a block.

| Block | Color | Look | Recipe |
|---|---|---|---|
| NOOP | transparent | frame of wooden edges and metal corners, all one pixel wide | 4 iron ingots + 4 sticks → 64 |
| COMMENT | glass | yellow note with `//` on the top | 8 NOOP around glass → 8 |
| LABEL | gray #AAAAAA | name on the top | NOOP + paper |
| PROGRAM | black #000000 | shape: round in the west, square in the east | NOOP + book |
| INCLUDE | black #000000 | map | NOOP + empty map |
| EXIT | black #000000 | iron door | 8 NOOP around iron door → 8 |
| BEGIN / END | gold #FFAA00 | shape: seen from above a trapezoid with diagonal sides; BEGIN is wide towards its code block (south), END towards the code block above it (north) | 8 NOOP around yellow wool → 8 BEGIN; BEGIN ↔ END |
| BREAK | gold #FFAA00 | broken stick | NOOP + stick |
| type base (item) | – | question mark | 8 NOOP around red carpet → 8 |
| TYPE_BOOLEAN | red | redstone torch | type base + redstone torch |
| TYPE_INTEGER | red | yellow sand | type base + sand; TYPE_FLOAT + yellow dye |
| TYPE_FLOAT | red | orange sand | type base + red sand; TYPE_INTEGER + orange dye |
| TYPE_CHAR | red | oak plank | type base + oak plank |
| TYPE_STRING | red | string | type base + string |
| TYPE_ARRAY | red | barrel | type base + barrel |
| TYPE_HASH | red | chest | type base + chest or trapped chest |
| TYPE_STRUCT | red | scaffolding | type base + scaffolding |
| TYPE_TIME | red | clock | type base + clock |
| TYPE_ERROR | red | campfire | type base + campfire |
| VARIABLE | blue | shape: a blue chest, as high as a slab | type base + bone block; CONSTANT |
| CONSTANT | blue | shape: a blue barrel, as high as a slab | type base + 3 bones; VARIABLE |
| LITERAL | gray #AAAAAA | shape: octagon, value on the top | 8 NOOP around white wool → 8 |
| TAG | dark brown #553311 | price tag, language on the top | 8 NOOP around brown wool → 8 |
| OPERATOR (all operators, ( ) and ;) | dark red #AA0000 | shape: the symbol chosen in a dialog, lying on the floor and extruded 4 pixels high (a question mark until one is chosen) | 8 NOOP around red wool → 8 |
| ASSIGN | dark aqua #00AAAA | triangle pointing to positive x | 8 NOOP around cyan wool → 8 |
| FUNCTION | dark gray #555555 | ƒ | NOOP + stone |
| PARAMETER | dark purple #AA00AA | shape: hopper at half its size | 8 NOOP around hopper → 8; RESULT |
| RESULT | dark purple #AA00AA | shape: upside-down hopper at half its size | PARAMETER |
| RETURN | dark purple #AA00AA | spruce door | 8 NOOP around spruce door → 8 |
| CALL | dark gray #555555 | 📞 | NOOP + smooth stone |
| structural base (item) | – | trapezoid | 8 NOOP around green wool → 8 |
| IF | dark green #00AA00 | ? | structural base + wheat seeds; ELSE |
| ELSE | dark green #00AA00 | ¿ | structural base + wheat; IF |
| FOR | dark green #00AA00 | lime circular arrow, # | structural base + dirt; EACH |
| EACH | dark green #00AA00 | lime circular arrow, square | structural base + cobblestone; FOR |
| WHILE | dark green #00AA00 | lime circular arrow, ? | structural base + diorite; REPEAT |
| REPEAT | dark green #00AA00 | lime square, ? | structural base + granite; WHILE |
| trigger block | – | golden ball | copper, glass, gold ingot → 8 |
| timer block | – | clock | copper, glass, clock → 8 |
| debug trigger block | – | red ball | copper, glass, redstone block → 1 |
| PID block | #222222 | PID | not craftable |
