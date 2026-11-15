```json
{
  "title": "ASSIGN",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 10
}
```

ASSIGN stores the value of an expression in a variable. The value must fit the type of the variable. An INTEGER may go into a FLOAT, and a CHAR into a STRING.

```c0de
ASSIGN arithmetic 1 + 2 * ( 3 + 3 ) \ 10 * 2 + 10 ;
ASSIGN list[0] 10 ;
ASSIGN list[1] 20 ;
ASSIGN list[2] list[0] + list[1] ;
```

Assigning to the index right after the end of a list adds a new element. A new key adds a new element to a hash.
