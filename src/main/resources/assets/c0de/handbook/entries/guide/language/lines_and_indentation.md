```json
{
  "title": "Lines and indentation",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 1
}
```

Global lines start at x=5: PROGRAM, [INCLUDE](^libraries/including_libraries), [STRUCT](^language/structs), [FUNCTION](^language/functions) and global [VARIABLE](^language/variables_and_constants) and CONSTANT lines. Lines inside a code block are one block further east than its BEGIN.

A code block starts with BEGIN and ends with END. END is followed by the keyword that opened the block, like `END FUNCTION` or `END WHILE`. A code block may not be empty.

Variables and constants made inside a code block only live inside it. They are forgotten when the block ends.

**Recipes**

<item;c0de:program_block>

<recipe;c0de:program_block>

<item;c0de:include_block>

<recipe;c0de:include_block>

<item;c0de:type_struct_block>

<recipe;c0de:type_struct_block>

<item;c0de:function_block>

<recipe;c0de:function_block>

<item;c0de:variable_block>

<recipe;c0de:variable_block>

<item;c0de:constant_block>

<recipe;c0de:constant_block>

<item;c0de:begin_block>

<recipe;c0de:begin_block>

<item;c0de:end_block>

<recipe;c0de:end_block>
