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

## Functions

A `NativeFunction` has a name (without library), its parameters, the names of its results and its implementation.

- **Parameters** are created with `NativeFunction.Parameter`:
  - `required(name, types, accepts)`: the CALL MUST provide it.
  - `withDefault(name, types, accepts, value)`: optional; the default value is used if the CALL leaves it out.
  - `optional(name, types, accepts)`: optional without default; `call.provided(name)` tells whether it was given.
  - `reference(name, types)`: the CALL passes a variable, which the function can change through `call.reference(name)`.

  `types` describes the accepted types for error messages, e.g. `"INTEGER|FLOAT"`. `accepts` checks a value; a value it refuses raises TYPE_MISMATCH at the CALL. `Values` has helpers, e.g. `Values::isNumber`.
- **Results** are named; a CALL assigns them with `RESULT name variable`. A single result is usually called `result`.
- **The implementation** receives a `NativeCall`: `argument(name)` is the value of a parameter (or its default), `tags(name)` the tags of the variable that provided it, `process()` the random generator, the clock, the time zone and the `host()` of the process.

It returns a `NativeResult`:

- `NativeResult.none()`: no results.
- `NativeResult.of(value)`: the single result `result`; `NativeResult.of(Map.of("a", …, "b", …))` for several.
- `NativeResult.error(code, message)`: returns an error to the caller, like RETURN with an error label.
- `NativeResult.waitSteps(n)`: the cursor waits `n` steps before it continues (see below).

## Values and types

The values of c0de are in `de.joernott.c0de.core.value`: `BooleanValue` (`BooleanValue.TRUE`, `FALSE`), `IntegerValue` (64 bit), `FloatValue`, `CharValue`, `StringValue`, `TimeValue`, `ArrayValue`, `HashValue` and `StructValue`. Types are `Type.Simple` (BOOLEAN, INTEGER, FLOAT, CHAR, TIME), `Type.Str` (STRING, optionally with a length limit), `Type.Array`, `Type.Hash` and `Type.Struct`.

Struct types of a library are `StructDefinition`s named with the library, e.g. `mymod/robots/ROBOT`. Programs declare variables of them with `VARIABLE robot STRUCT mymod/robots/ROBOT`.

## Errors

A function reports an error by throwing `C0deException.of(code, message)` with an `ErrorCode`, e.g. `ErrorCode.INVALID_ARGUMENT` or `ErrorCode.PERMISSION_DENIED`. The error is raised at the CALL statement: a program catches it with an error label (`CALL mymod/robots/COUNT problem`), otherwise the function that contains the CALL ends with it. Use the codes of c0de where they fit ([error codes](concept/16-error-codes.md)); for errors of your own, use codes from 1000 on.

## Waiting

A function must not block the server. If it starts something that takes time, it returns `NativeResult.waitSteps(n)`: the cursor waits `n` steps (one step per tick) and then continues with the next line. To check later whether the work is done, offer a second function that the program calls in a loop.

## The world and permissions

`call.process().host()` is a `de.joernott.c0de.library.ProcessHost` for processes that trigger blocks started: the server, the level and position of the trigger block, the UUID of its owner (the player who configured it), the program and the PID. Functions that change the world SHOULD check permissions like c0de/minecraft does: such changes are only allowed if the owner is an operator or a setting of your mod allows them, and blocks on the sheets of the c0de dimension MUST NOT be changed (PERMISSION_DENIED).

Functions can't access chunks that are not loaded; raise CHUNK_NOT_LOADED instead of loading them.
