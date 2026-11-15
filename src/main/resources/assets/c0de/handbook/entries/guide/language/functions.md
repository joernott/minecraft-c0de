```json
{
  "title": "Functions",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 11
}
```

A FUNCTION line starts at x=5 with the function name. PARAMETER lines below it name the inputs, RESULT lines the outputs. Then follows the code block.

A parameter may have a default value. A caller can leave such a parameter out. Results start with the default value of their type.

```c0de
FUNCTION greet
  PARAMETER name STRING "World"
  PARAMETER mark CHAR '!'
  RESULT result STRING
BEGIN
  ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```

When the function ends, the last values of its results go back to the caller.

All parameters and results of a function need different names. A parameter `value` and a result `value` give SYNTAX\_ERROR\_FUNCTION. Call the result `result`, for example. The caller may still use one variable for both, see CALL.

**Recipes**

<item;c0de:function_block>

<recipe;c0de:function_block>

<item;c0de:parameter_block>

<recipe;c0de:parameter_block>

<item;c0de:result_block>

<recipe;c0de:result_block>

<item;c0de:call_block>

<recipe;c0de:call_block>
