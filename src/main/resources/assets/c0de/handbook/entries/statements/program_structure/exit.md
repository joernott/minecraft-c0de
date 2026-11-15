```json
{
  "title": "EXIT",
  "icon": "c0de:exit_block",
  "category": "program_structure",
  "ordinal": 4
}
```

EXIT ends the whole process at once.

It works no matter how deep the cursor is in functions and code blocks. An expression with a number from 0 to 15 follows: the exit code. A comparator next to the trigger block can read it. EXIT only works inside functions.

Exit code 15 means the program crashed. You may use `EXIT 15 ;` for a fatal problem too.

EXIT is not a crash: the results of the entry function come out with the values they have at that moment.

<recipe;c0de:exit_block>

Put an iron door in the middle and eight NOOP blocks around it. You get eight EXIT blocks.

```c0de
EXIT 3 ;
ASSIGN value 0 ;
```

The ASSIGN line never runs, because EXIT ends the process first.
