```json
{
  "title": "ASSIGN",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 10
}
```

ASSIGN stores the value of an [expression](^language/expressions) in a variable. The value must fit the type of the variable. An INTEGER may go into a FLOAT, and a CHAR into a STRING.

```c0de
ASSIGN arithmetic 1 + 2 * ( 3 + 3 ) \ 10 * 2 + 10 ;
ASSIGN list[0] 10 ;
ASSIGN list[1] 20 ;
ASSIGN list[2] list[0] + list[1] ;
```

Assigning to the index right after the end of a list adds a new element. A new key adds a new element to a hash.

**Recipes**

<item;c0de:assign_block>

<recipe;c0de:assign_block>

<item;c0de:type_integer_block>

<recipe;c0de:type_integer_block>

<item;c0de:type_float_block>

<recipe;c0de:type_float_block>

<item;c0de:type_char_block>

<recipe;c0de:type_char_block>

<item;c0de:type_string_block>

<recipe;c0de:type_string_block>
