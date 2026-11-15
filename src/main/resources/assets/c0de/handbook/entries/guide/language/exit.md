```json
{
  "title": "EXIT",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 19
}
```

EXIT ends the whole process at once, no matter how deep the cursor is. It needs a number from 0 to 15: the **exit code**. A comparator next to the trigger block can read it.

```c0de
EXIT 3 ;
```

Exit code 15 means the program crashed. You may use `EXIT 15 ;` for a fatal problem too.

EXIT is not a crash: the results of the entry function still come out, with the values they have at that moment. Only a real crash gives no results.
