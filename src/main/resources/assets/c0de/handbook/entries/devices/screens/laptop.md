```json
{
  "title": "Laptop",
  "icon": "c0de:laptop",
  "category": "screens",
  "ordinal": 1
}
```

A laptop is a block with a screen and a keyboard. Players type the values for INPUT on it.

Right-click it to open its terminal. Left-click it with the wrench to turn it. The screen has 48 columns and 20 rows. Rows that scroll off the top stay in a history of 200 rows. Scroll with the mouse wheel or the scroll bar.

At the top, two buttons pick the program and the function. ✔ binds the laptop and closes the terminal. Below the screen is the input line. It only works while an INPUT waits, and it shows what INPUT asks for, like `INPUT count (INTEGER):`.

Press Enter to send the line. The line appears on the screen. Text that doesn't fit the type is refused with a line starting with `? `, and INPUT keeps waiting. Keys typed while no INPUT waits are kept for INKEY. Enter arrives there as CHR 13.

A laptop doesn't take part in WRITE. Breaking a laptop deletes its terminal.

<recipe;c0de:laptop>

<recipe;c0de:laptop_from_tablet>

Craft a display block and an I/O block together, or turn a tablet into a laptop.

Bind a laptop to the function `ask` and start it. The input line shows `INPUT count (INTEGER):`. Type 21 and press Enter. The screen shows 42.

```c0de
PROGRAM typing
NOOP
NOOP
FUNCTION ask
BEGIN
  VARIABLE count INTEGER
  CALL INPUT
    PARAMETER variable count ;
  CALL PRINT
    PARAMETER value count * 2 ;
END FUNCTION
```
