```json
{
  "title": "I/O block",
  "icon": "c0de:io_block",
  "category": "items",
  "ordinal": 0
}
```

The I/O block takes values for INPUT and gives out values from WRITE.

Right-click it to open its dialog. It has 18 input slots and 18 output slots. The input takes only variable, constant and literal items. You can only take items out of the output. The Program and Function buttons and ✔ bind the block, like on a display block.

Hoppers fill the input and empty the output. A comparator reads how full the output is.

<recipe;c0de:io_block>

Craft a data item and a hopper together.

Bind an I/O block to the function `double`. Put an INTEGER variable item named `number` with the value 21 and an uninitialized variable item into its input. Start the function: the output gets an item named `output` with the value 42.

```c0de
PROGRAM doubling
NOOP
NOOP
FUNCTION double
BEGIN
  VARIABLE number INTEGER
  CALL INPUT
    PARAMETER variable number ;
  CALL WRITE
    PARAMETER value number * 2 ;
END FUNCTION
```
