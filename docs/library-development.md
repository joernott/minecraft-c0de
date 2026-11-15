# Writing c0de libraries in other mods

c0de programs use libraries with `INCLUDE mod/library`, e.g. `INCLUDE c0de/minecraft`. Other mods can add their own libraries: functions written in Java that c0de programs call with `CALL mymod/robots/COUNT`, constants they can read and struct types they can use. This page shows how to write such a library and register it with c0de.

## Overview

A library is a Java object that implements `de.joernott.c0de.core.library.Library`. Your mod creates it and registers it with `de.joernott.c0de.library.ModLibraries.register(...)` while it is initialized. From then on, programs can include it by its name.

- The name of a library is the ID of your mod, a slash and the name of the library, e.g. `mymod/robots`. Names start with a letter and consist of letters, digits and underscores; the names `c0de/…` belong to c0de. A name can be registered only once.
- The names of functions, constants and struct types are written in upper case by convention (`COUNT`, `MAX_ROBOTS`, `ROBOT`). Programs use them qualified with the library name: `mymod/robots/COUNT`.
- The functions run on the server thread, one step of the cursor at a time. A function must return quickly; to wait, it returns `NativeResult.waitSteps(n)` (see [Waiting](#waiting)).

## Dependency

c0de is an optional dependency of your mod: programs can only use your library if c0de is installed, and your mod must work without it.

Add the c0de jar for Minecraft 1.21.1 of your loader as a compile-only dependency. Once c0de is published on Modrinth, use the Modrinth Maven repository:

```kotlin
repositories {
    maven("https://api.modrinth.com/maven") { name = "Modrinth" }
}

dependencies {
    // Fabric (Loom)
    modCompileOnly("maven.modrinth:c0de:0.1.0+fabric")
    // NeoForge (ModDevGradle)
    compileOnly("maven.modrinth:c0de:0.1.0+neoforge")
}
```

Declare c0de as an optional dependency, so that it is loaded before your mod when it is installed: in `fabric.mod.json` with `"suggests": {"c0de": "*"}`, in `neoforge.mods.toml` with a `[[dependencies.mymod]]` entry for `c0de` with `type = "optional"` and `ordering = "AFTER"`.

## Registering

Register the library while your mod is initialized, but only if c0de is loaded. Keep the code that uses c0de classes in a class of its own, so that it is not loaded without c0de:

```java
// In the initializer of your mod
if (FabricLoader.getInstance().isModLoaded("c0de")) {   // NeoForge: ModList.get().isLoaded("c0de")
    RobotsC0de.register();
}

// RobotsC0de.java: only loaded if c0de is installed
public final class RobotsC0de {
    public static void register() {
        ModLibraries.register(new RobotsLibrary());
    }
}
```

`register` throws an `IllegalArgumentException` if the name is not valid or already registered.

## A library

```java
public final class RobotsLibrary implements Library {
    /** A struct type: programs declare variables of it with STRUCT mymod/robots/ROBOT. */
    public static final StructDefinition ROBOT = new StructDefinition(
            "mymod/robots/ROBOT",
            List.of(
                    new StructDefinition.Element("name", Type.Str.UNLIMITED, Map.of()),
                    new StructDefinition.Element("energy", Type.Simple.INTEGER, Map.of())));

    @Override
    public String name() {
        return "mymod/robots";
    }

    @Override
    public Map<String, StructDefinition> types() {
        return Map.of("ROBOT", ROBOT);
    }

    @Override
    public Map<String, Value> constants() {
        return Map.of("MAX_ROBOTS", new IntegerValue(16));
    }

    @Override
    public Map<String, NativeFunction> functions() {
        return Map.of(
                "COUNT", new NativeFunction(
                        "COUNT",
                        List.of(NativeFunction.Parameter.withDefault(
                                "radius", "INTEGER", value -> value instanceof IntegerValue, new IntegerValue(16))),
                        List.of("result"),
                        RobotsLibrary::count),
                "FIRST", new NativeFunction(
                        "FIRST",
                        List.of(),
                        List.of("result"),
                        RobotsLibrary::first));
    }

    private static NativeResult count(NativeCall call) {
        long radius = ((IntegerValue) call.argument("radius")).value();
        if (radius < 1 || radius > 64) {
            throw C0deException.of(ErrorCode.INVALID_ARGUMENT, "The radius must be 1 to 64");
        }
        ServerLevel level = host(call).level();
        BlockPos center = host(call).triggerPos();
        int robots = level.getEntitiesOfClass(Robot.class, new AABB(center).inflate(radius)).size();
        return NativeResult.of(new IntegerValue(robots));
    }

    private static NativeResult first(NativeCall call) {
        StructValue robot = new StructValue(ROBOT);
        robot.set("name", new StringValue("Robbie"));
        robot.set("energy", new IntegerValue(100));
        return NativeResult.of(robot);
    }

    /** The process in the game; only processes started by trigger blocks have one. */
    private static ProcessHost host(NativeCall call) {
        if (call.process().host() instanceof ProcessHost host) {
            return host;
        }
        throw C0deException.of(ErrorCode.INVALID_ARGUMENT, "mymod/robots only works in the game");
    }
}
```

A program uses it like this:

```c0de
PROGRAM robots
NOOP
INCLUDE mymod/robots
NOOP
FUNCTION census
    RESULT count INTEGER
BEGIN
    CALL mymod/robots/COUNT
        PARAMETER radius 32 ;
        RESULT result count
END FUNCTION
```

## Built-in constants

Constants of a library are qualified with its name and need its INCLUDE. A mod can also register **built-in constants**, which every program reads without INCLUDE, like `C0DE_VERSION` of c0de. Use them sparingly, for things like the version of your mod:

```java
ModLibraries.registerConstant("MYMOD_VERSION", new StringValue("1.2.0"));
```

The name is written in upper case and starts with the ID of your mod in upper case. Names of c0de (the error codes, the colors, SHEET, PID, CURSOR, C0DE_VERSION) and names that are already registered are refused with an `IllegalArgumentException`. Register the constants while your mod is initialized, like the libraries.

## Functions

A `NativeFunction` has a name (without library), its parameters, the names of its results and its implementation.

- **Parameters** are created with `NativeFunction.Parameter`:
  - `required(name, types, accepts)`: the CALL MUST provide it.
  - `withDefault(name, types, accepts, value)`: optional; the default value is used if the CALL leaves it out.
  - `optional(name, types, accepts)`: optional without default; `call.provided(name)` tells whether it was given.
  - `reference(name, types)`: the CALL passes a variable, which the function can change through `call.reference(name)`.

  `types` describes the accepted types for error messages, e.g. `"INTEGER|FLOAT"`. `accepts` checks a value; a value it refuses raises TYPE_MISMATCH at the CALL. `Values` has helpers, e.g. `Values::isNumber`.
- **Results** are named; a CALL assigns them with `RESULT name variable`. A single result is usually called `result`.
- **The implementation** receives a `NativeCall`: `argument(name)` is the value of a parameter (or its default), `tags(name)` the tags of the variable that provided it, `label(name)` its name (an `Optional<String>`, present if the PARAMETER expression is a single LABEL without index; WRITE names its item after it), `process()` the random generator, the clock, the time zone and the `host()` of the process.

It returns a `NativeResult`:

- `NativeResult.none()`: no results.
- `NativeResult.of(value)`: the single result `result`; `NativeResult.of(Map.of("a", …, "b", …))` for several.
- `NativeResult.error(code, message)`: returns an error to the caller, like RETURN with an error label.
- `NativeResult.waitSteps(n)`: the cursor waits `n` steps before it continues (see below).
- `NativeResult.waitForValue()`: the function can't finish yet; the cursor stays on the CALL line and calls the function again in the next step (see below).

## Values and types

The values of c0de are in `de.joernott.c0de.core.value`: `BooleanValue` (`BooleanValue.TRUE`, `FALSE`), `IntegerValue` (64 bit), `FloatValue`, `CharValue`, `StringValue`, `TimeValue`, `ArrayValue`, `HashValue` and `StructValue`. Types are `Type.Simple` (BOOLEAN, INTEGER, FLOAT, CHAR, TIME), `Type.Str` (STRING, optionally with a length limit), `Type.Array`, `Type.Hash` and `Type.Struct`.

Struct types of a library are `StructDefinition`s named with the library, e.g. `mymod/robots/ROBOT`. Programs declare variables of them with `VARIABLE robot STRUCT mymod/robots/ROBOT`.

## Errors

A function reports an error by throwing `C0deException.of(code, message)` with an `ErrorCode`, e.g. `ErrorCode.INVALID_ARGUMENT` or `ErrorCode.PERMISSION_DENIED`. The error is raised at the CALL statement: a program catches it with an error label (`CALL mymod/robots/COUNT problem`), otherwise the function that contains the CALL ends with it. Use the codes of c0de where they fit ([error codes](concept/16-error-codes.md)); for errors of your own, use codes from 1000 on.

## Waiting

A function must not block the server. If it starts something that takes time, it returns `NativeResult.waitSteps(n)`: the cursor waits `n` steps (one step per tick) and then continues with the next line. To check later whether the work is done, offer a second function that the program calls in a loop.

If the function waits for something it can't predict, it returns `NativeResult.waitForValue()`: no results are delivered, the cursor stays on the CALL line and calls the function again in the next step, with the PARAMETER expressions evaluated again, until it returns something else. INPUT uses this while it waits for a value, WRITE while no I/O block has an uninitialized variable item. Each call must still return quickly.

## The world and permissions

`call.process().host()` is a `de.joernott.c0de.library.ProcessHost` for processes that trigger blocks started: the server, the level and position of the trigger block, the UUID of its owner (the player who configured it), the program and the PID. Functions that change the world SHOULD check permissions like c0de/minecraft does: such changes are only allowed if the owner is an operator or a setting of your mod allows them, and blocks on the sheets of the c0de dimension MUST NOT be changed (PERMISSION_DENIED).

Functions can't access chunks that are not loaded; raise CHUNK_NOT_LOADED instead of loading them.

## The program runner

The [program runner](runner.md) runs c0de programs without Minecraft, e.g. to test the examples of your documentation. To make your library available there, publish a small jar with a class that implements `de.joernott.c0de.core.library.LibraryProvider` and adds your library and constants to the registry, named in `META-INF/services/de.joernott.c0de.core.library.LibraryProvider`. Such a library can only use the interpreter (`core`), not Minecraft.

## Handbooks

Since c0de 0.6.1, your mod can have handbooks in the game like those of c0de: red binders whose pages describe your library, with crafting grids, program examples in the colours of c0de, links and translations. A handbook is made of resources and data of your mod only; you register nothing and call no code of c0de. The book screen, its Markdown and the binder belong to c0de; the [concept](concept/21-handbooks.md) describes them in detail.

**The book.** `assets/mymod/handbook/books/robots.json` defines the book `mymod:robots`:

```json
{
  "name": "book.mymod.robots.name",
  "landing_text": "book.mymod.robots.landing_text",
  "color": "#F0A824"
}
```

`name` and `landing_text` are translation keys of your language files; the landing text is Markdown. `color` is the colour of the label of the binder. The name of the binder is always the translation `book.<namespace>.<book>.name`, so use that key for `name`.

**Categories and entries.** `assets/mymod/handbook/categories/robots/<category>.md` and `assets/mymod/handbook/entries/robots/<category>/<entry>.md`. Each starts with a JSON block (`title`, `icon`, `ordinal`, and for entries `category`) and continues in Markdown: bold, lists, `` `code` ``, program examples in ```` ```c0de ```` blocks, `<recipe;mymod:robot_core>` for a crafting grid, `<item;mymod:robot_core>` for an item, `;;;;;` for a page break, and links: `[text](^category/entry)` within the book, `[text](^c0de:functions/c0de_strings/len)` into a book of c0de or of another mod. Translations go into a folder named after the language, e.g. `entries/robots/de_de/<category>/<entry>.md`; the book falls back to the English files.

**Crafting grids.** The client doesn't know the recipes of the server, so the grids that your pages show are in `assets/mymod/handbook/recipes.json`, keyed by the ID you use in `<recipe;…>`:

```json
{
  "mymod:robot_core": {
    "slots": [["minecraft:iron_ingot"], ["minecraft:redstone"], ["minecraft:iron_ingot"], [], [], [], [], [], []],
    "result": "mymod:robot_core",
    "count": 1
  },
  "mymod:robots_book": {
    "slots": [["minecraft:book"], ["mymod:robot_core"], [], [], [], [], [], [], []],
    "result": "c0de:handbook",
    "book": "mymod:robots",
    "color": "#F0A824"
  }
}
```

Each of the nine slots lists the items it cycles through. c0de merges the files of all mods, so every book can show the grids of every mod. A result with `book` shows the binder of that book.

**The binder.** All books of other mods share the binder `c0de:handbook`; its custom data says which book it opens. Give your book a recipe, e.g. a book and an item of your mod. c0de is an optional dependency, so load the recipe only when c0de is there. For Minecraft 1.21.1:

```json
{
  "fabric:load_conditions": [{"condition": "fabric:all_mods_loaded", "values": ["c0de"]}],
  "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "c0de"}],
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [{"item": "minecraft:book"}, {"item": "mymod:robot_core"}],
  "result": {
    "id": "c0de:handbook",
    "count": 1,
    "components": {"minecraft:custom_data": {"book": "mymod:robots", "color": 15771684}}
  }
}
```

From Minecraft 1.21.2 on, ingredients are plain item IDs (`"ingredients": ["minecraft:book", "mymod:robot_core"]`), and the result also sets the colour for the item model: `"minecraft:custom_model_data": {"colors": [15771684]}` next to the custom data. `color` in the custom data is the colour of the label as a number (`0xF0A824` is 15771684).

The creative tab of c0de lists the binders of all books it finds. Only the guide of c0de is given to players when they join; your book is crafted.

**Generating the pages.** c0de generates its books from its Markdown documentation with `scripts/generate_book.py` (the guide in `docs/guide`, the handbooks in `docs/reference`, translations in `docs/de`). It is written for the documentation of c0de, but you can use it as a template for yours.
