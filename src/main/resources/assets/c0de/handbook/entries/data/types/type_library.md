```json
{
  "title": "TYPE_LIBRARY",
  "icon": "c0de:type_library_block",
  "category": "types",
  "ordinal": 11
}
```

TYPE\_LIBRARY stands for the keyword TYPE: a type of a library of another mod, like the canvas of c0de paint. A LABEL with the full name of the type follows it. The LABEL dialog lists the types of the libraries the program includes.

You can't look into a value of a library type\; only the functions of its library work with it. It starts with a value its library chooses, like an empty canvas. It can't be set with a literal, and there are no constants of it. `=` and `<>` compare two values\; `⊕` writes a short description, like `CANVAS 20×10×8, 1600 voxels`. TO\_JSON and TO\_YAML can't write it (TYPE\_MISMATCH).

A process keeps its values over a restart of the server. If the mod of the library is removed, the process crashes with UNKNOWN\_LIBRARY when it is loaded. A result of a library type comes out of the trigger block as an item that shows the description\; it can't go into another process.

<recipe;c0de:type_library_block>

Craft a type base and a decorated pot together.

```c0de
INCLUDE c0de_paint/cpaint
VARIABLE picture TYPE c0de_paint/cpaint/CANVAS
```

`picture` holds an empty canvas of c0de paint.
