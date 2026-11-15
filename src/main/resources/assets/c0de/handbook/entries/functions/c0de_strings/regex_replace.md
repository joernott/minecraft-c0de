```json
{
  "title": "REGEX_REPLACE",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 16
}
```

Replaces the matches of a regular expression, like the command `s` of sed.

In `replacement`, `&` stands for the whole match and `\1` to `\9` for the groups of the pattern\; `\&` and `\\` are a `&` and a backslash, `\n` is a line break. Without `flags`, only the first match is replaced. The flags are `g` for all matches, a number N for the Nth match (with `g`, the Nth and all after it), `i` to ignore the case, `m` for `^` and \`# Functions

A function call runs a function with a CALL line. The PARAMETER lines below it give the values the function needs. The RESULT lines name the variables that receive what the function returns. Results you don't list are thrown away.

Built-in functions work everywhere. Library functions need an INCLUDE line for their library first, e.g. `INCLUDE c0de/math`. Then you call them by their full name, e.g. `c0de/math/SIN`. If a call fails and has no error label, the error is raised.
