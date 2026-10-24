package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.cursor.ProgramIndex.ElseNode;
import de.joernott.c0de.core.cursor.ProgramIndex.EndNode;
import de.joernott.c0de.core.cursor.ProgramIndex.Node;
import de.joernott.c0de.core.cursor.ProgramIndex.StatementNode;
import de.joernott.c0de.core.cursor.StackItem.BlockMarker;
import de.joernott.c0de.core.cursor.StackItem.FunctionFrame;
import de.joernott.c0de.core.cursor.StackItem.LoopItem;
import de.joernott.c0de.core.cursor.StackItem.VariableItem;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.eval.Declarations;
import de.joernott.c0de.core.eval.ExpressionEvaluator;
import de.joernott.c0de.core.eval.IndexedAccess;
import de.joernott.c0de.core.eval.TypeRegistry;
import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.io.Console;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.library.ProcessContext;
import de.joernott.c0de.core.program.Expression;
import de.joernott.c0de.core.program.LabelRef;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Executes a program line by line, as the cursor in the c0de dimension does (see docs/concept/15-cursor.md).
 * Every call of {@link #step()} processes one line. NOOP lines, comment lines, continuation lines and the
 * content lines of declarations don't take a step.
 */
public final class Cursor {
    /** Highest exit code; also the exit code of crashed processes. */
    public static final int CRASH_EXIT_CODE = 15;

    private final Program program;
    private final ProgramIndex index;
    private final String entryFunction;
    private final Map<String, Value> inputs;
    private final CursorSettings settings;
    private final TypeRegistry types = new TypeRegistry();
    private final Map<String, FunctionEntry> functions = new LinkedHashMap<>();
    private final Map<String, Value> constants = new HashMap<>();
    private final List<String> libraries = new ArrayList<>();
    private final CallStack stack;
    private final Process process = new Process();

    private CursorState state = CursorState.RUNNING;
    private int line = 1;
    private int waitSteps;
    private int exitCode = -1;
    private StructValue error;
    private Map<String, Value> results = Map.of();
    private C0deRandom random;

    /**
     * Creates a cursor. It starts in the line after the PROGRAM line.
     *
     * @param program the program
     * @param entryFunction the name of the function to run
     * @param inputs the values for the parameters of the entry function by name
     * @param settings the environment
     */
    public Cursor(Program program, String entryFunction, Map<String, Value> inputs, CursorSettings settings) {
        this.program = program;
        this.index = new ProgramIndex(program);
        this.entryFunction = entryFunction;
        this.inputs = Map.copyOf(inputs);
        this.settings = settings;
        this.random = new C0deRandom(settings.randomSeed());
        this.stack = new CallStack(settings.stackSize(), constants);
        constants.putAll(ErrorCode.builtinConstants());
        Library builtins = settings.libraries().builtins();
        constants.putAll(builtins.constants());
        builtins.functions().forEach((name, function) -> functions.put(name, new LibraryFunction(name, function)));
        if (program.name() == null) {
            crash(C0deException.at(0, ErrorCode.SYNTAX_ERROR_PLACEMENT, "The PROGRAM line is invalid"));
        }
    }

    // ------------------------------------------------------------------------------------------- state

    public CursorState state() {
        return state;
    }

    /** The z coordinate of the line the cursor is at (the next line it processes). */
    public int line() {
        return line;
    }

    /** True if the process has ended (finished or crashed). */
    public boolean ended() {
        return state == CursorState.FINISHED || state == CursorState.CRASHED;
    }

    /** The exit code of an ended process: 0 to 15, 15 for crashes. -1 while it runs. */
    public int exitCode() {
        return exitCode;
    }

    /** The error of a crashed process, or null. */
    public StructValue error() {
        return error;
    }

    /** The results of the entry function of a finished process. Empty for crashed processes. */
    public Map<String, Value> results() {
        return results;
    }

    /** The number of steps the cursor still waits (SLEEP). */
    public int waitSteps() {
        return waitSteps;
    }

    /** The name of the function the cursor is in, or the empty string in the global scope. */
    public String currentFunction() {
        FunctionFrame frame = stack.currentFrame();
        return frame == null ? "" : frame.function();
    }

    /** Pauses a running cursor (debug cursor). */
    public void pause() {
        if (state == CursorState.RUNNING) {
            state = CursorState.PAUSED;
        }
    }

    /** Continues a paused cursor. */
    public void resume() {
        if (state == CursorState.PAUSED) {
            state = CursorState.RUNNING;
        }
    }

    /** Processes one line if the cursor is running. */
    public void step() {
        if (state == CursorState.RUNNING) {
            execute();
        }
    }

    /** Processes one line of a paused debug cursor. */
    public void stepOnce() {
        if (state == CursorState.PAUSED) {
            execute();
        }
    }

    /** Runs until the process ends or the number of steps is reached; returns the number of steps taken. */
    public int run(int maxSteps) {
        int steps = 0;
        while (state == CursorState.RUNNING && steps < maxSteps) {
            step();
            steps++;
        }
        return steps;
    }

    // --------------------------------------------------------------------------------------- execution

    private void execute() {
        if (waitSteps > 0) {
            waitSteps--;
            return;
        }
        Integer z = index.next(line);
        if (z == null || z >= program.endLine()) {
            line = program.endLine();
            crash(C0deException.at(
                    line, ErrorCode.UNKNOWN_FUNCTION, "The entry function " + entryFunction + " was not found"));
            return;
        }
        line = z;
        try {
            execute(index.node(z));
        } catch (C0deException e) {
            handle(e.inLine(z));
        }
        if (!ended()) {
            // The cursor moves to the next line it processes, skipping NOOP, comment and continuation lines
            Integer next = index.next(line);
            line = next == null ? program.endLine() : Math.min(next, program.endLine());
        }
    }

    private void execute(Node node) {
        switch (node) {
            case StatementNode statement -> execute(statement.statement());
            case EndNode end -> end(end);
            case ElseNode elseNode ->
                throw C0deException.at(
                        line, ErrorCode.SYNTAX_ERROR_PLACEMENT, "ELSE must follow the END line of an IF statement");
        }
    }

    private void execute(Statement statement) {
        switch (statement) {
            case Statement.Invalid invalid -> throw invalid.error().asCrash();
            case Statement.Include include -> include(include);
            case Statement.StructDeclaration struct -> struct(struct);
            case Statement.Declaration declaration -> declaration(declaration);
            case Statement.FunctionDeclaration function -> function(function);
            case Statement.Assign assign -> assign(assign);
            case Statement.Call call -> call(call);
            case Statement.If condition -> condition(condition);
            case Statement.For loop -> forLoop(loop);
            case Statement.Each loop -> eachLoop(loop);
            case Statement.While loop -> whileLoop(loop);
            case Statement.Repeat loop -> enterLoop(loop, loop.body(), null, null);
            case Statement.Break breakStatement -> breakLoop(breakStatement);
            case Statement.Return returnStatement -> returnStatement(returnStatement);
            case Statement.Exit exit -> exit(exit);
        }
    }

    // ---------------------------------------------------------------------------------- input and output

    /** The devices of the process: the host of the mod if it has some, otherwise none. */
    private Console console() {
        return settings.host() instanceof Console console ? console : Console.NONE;
    }

    /** Continues after the given line. */
    private void continueAfter(int z) {
        line = z + 1;
    }

    /** Enters a code block: pushes its block marker and continues with its first line. */
    private void enter(Statement owner, Statement.CodeBlock block) {
        stack.push(new BlockMarker(owner, block));
        line = block.beginLine() + 1;
    }

    // ----------------------------------------------------------------------------------- global scope

    private void include(Statement.Include include) {
        Library library = settings.libraries()
                .find(include.library())
                .orElseThrow(
                        () -> C0deException.of(ErrorCode.UNKNOWN_LIBRARY, "There is no library " + include.library()));
        addLibrary(library);
        libraries.add(library.name());
        continueAfter(include.line());
    }

    private void addLibrary(Library library) {
        Map<String, StructDefinition> libraryTypes;
        Map<String, Value> libraryConstants;
        Map<String, NativeFunction> libraryFunctions;
        try {
            libraryTypes = library.types();
            libraryConstants = library.constants();
            libraryFunctions = library.functions();
        } catch (RuntimeException e) {
            throw C0deException.of(ErrorCode.BROKEN_LIBRARY, "The library " + library.name() + " can't be read: " + e);
        }
        if (libraryTypes == null || libraryConstants == null || libraryFunctions == null) {
            throw C0deException.of(ErrorCode.BROKEN_LIBRARY, "The library " + library.name() + " is incomplete");
        }
        String prefix = library.name() + "/";
        libraryTypes.forEach((name, definition) -> types.add(prefix + name, definition));
        libraryConstants.forEach((name, value) -> constants.put(prefix + name, value));
        libraryFunctions.forEach(
                (name, function) -> functions.put(prefix + name, new LibraryFunction(prefix + name, function)));
    }

    private void struct(Statement.StructDeclaration struct) {
        if (types.find(struct.name()).isPresent() || nameInUse(struct.name())) {
            throw C0deException.crash(ErrorCode.SYNTAX_ERROR_STRUCT, "The name " + struct.name() + " is already used");
        }
        types.add(struct.name(), Declarations.defineStruct(struct, types));
        continueAfter(struct.endLine());
    }

    private void declaration(Statement.Declaration declaration) {
        ErrorCode error = declaration.constant() ? ErrorCode.SYNTAX_ERROR_CONSTANT : ErrorCode.SYNTAX_ERROR_VARIABLE;
        requireUnique(declaration.name(), error);
        Variable variable = Declarations.declare(declaration, types).inLine(declaration.line());
        stack.push(new VariableItem(variable, stack.currentFrame() == null));
        continueAfter(declaration.endLine());
    }

    private void function(Statement.FunctionDeclaration declaration) {
        if (functions.containsKey(declaration.name()) || nameInUse(declaration.name())) {
            throw C0deException.crash(
                    ErrorCode.SYNTAX_ERROR_FUNCTION, "The name " + declaration.name() + " is already used");
        }
        ProgramFunction function = ProgramFunction.resolve(declaration, types);
        functions.put(declaration.name(), function);
        if (!declaration.name().equals(entryFunction)) {
            continueAfter(declaration.body().endLine());
            return;
        }
        // The entry function: parameters from the inputs of the trigger block
        FunctionFrame frame = new FunctionFrame(declaration.name(), true, declaration.line(), null, -1, List.of());
        stack.push(frame);
        enter(declaration, declaration.body());
        for (ProgramFunction.Parameter parameter : function.parameters()) {
            Value input = inputs.get(parameter.name());
            Value value;
            if (input != null) {
                try {
                    value = Values.convert(input, parameter.type());
                } catch (C0deException e) {
                    throw e.asCrash();
                }
            } else if (parameter.defaultValue() != null) {
                value = parameter.defaultValue().copy();
            } else {
                throw C0deException.crash(
                        ErrorCode.FUNCTION_MISSING_INPUT, "There is no input for the parameter " + parameter.name());
            }
            stack.push(new VariableItem(
                    new Variable(parameter.name(), parameter.type(), value, false, Map.of(), parameter.line(), false),
                    false));
        }
        pushResults(frame, function);
    }

    private void pushResults(FunctionFrame frame, ProgramFunction function) {
        for (ProgramFunction.Result result : function.results()) {
            Variable variable = new Variable(
                    result.name(), result.type(), result.type().defaultValue(), false, Map.of(), result.line(), false);
            frame.results().add(variable);
            stack.push(new VariableItem(variable, false));
        }
    }

    private boolean nameInUse(String name) {
        return stack.find(name).isPresent() || functions.containsKey(name);
    }

    private void requireUnique(String name, ErrorCode error) {
        if (nameInUse(name) || types.find(name).isPresent()) {
            throw C0deException.crash(error, "The name " + name + " is already used");
        }
    }

    // ------------------------------------------------------------------------------------- statements

    private void assign(Statement.Assign assign) {
        Variable target = stack.resolve(assign.target().name());
        if (target.constant() || target.loopVariable()) {
            throw C0deException.crash(
                    ErrorCode.SYNTAX_ERROR_ASSIGN,
                    "The constant or loop variable " + target.name() + " can't be changed");
        }
        Value value = ExpressionEvaluator.evaluate(assign.value(), stack);
        IndexedAccess.write(assign.target(), stack, value);
        continueAfter(assign.line());
    }

    private void condition(Statement.If condition) {
        if (ExpressionEvaluator.evaluateBoolean(condition.condition(), stack)) {
            enter(condition, condition.then());
        } else if (condition.otherwise() != null) {
            enter(condition, condition.otherwise());
        } else {
            continueAfter(condition.endLine());
        }
    }

    private void forLoop(Statement.For loop) {
        requireUnique(loop.variable(), ErrorCode.SYNTAX_ERROR_LOOP);
        Value from = loopValue(ExpressionEvaluator.evaluate(loop.from(), stack), loop.type());
        Value to = loopValue(ExpressionEvaluator.evaluate(loop.to(), stack), loop.type());
        Value step = loop.step() == null
                ? loopValue(new IntegerValue(1), loop.type())
                : loopValue(ExpressionEvaluator.evaluate(loop.step(), stack), loop.type());
        if (Values.toDouble(step) == 0.0) {
            throw C0deException.of(ErrorCode.INVALID_LOOP_STEP, "The increment of a FOR loop must not be 0");
        }
        Variable variable = new Variable(loop.variable(), loop.type(), from, false, Map.of(), loop.line(), true);
        if (forConditionHolds(from, to, step)) {
            enterLoop(loop, loop.body(), new VariableItem(variable, false), new LoopItem(loop, to, step, List.of()));
        } else {
            continueAfter(loop.body().endLine());
        }
    }

    private static Value loopValue(Value value, Type.Simple type) {
        if (!Values.isNumber(value)) {
            throw C0deException.of(ErrorCode.TYPE_MISMATCH, "The values of a FOR loop must be numbers");
        }
        return Values.convert(value, type);
    }

    private static boolean forConditionHolds(Value current, Value end, Value step) {
        int comparison = Values.compare(current, end);
        return Values.toDouble(step) > 0 ? comparison <= 0 : comparison >= 0;
    }

    private void eachLoop(Statement.Each loop) {
        requireUnique(loop.variable(), ErrorCode.SYNTAX_ERROR_LOOP);
        Value iterable = IndexedAccess.read(loop.iterable(), stack);
        List<Value> elements = new ArrayList<>();
        Type elementType;
        switch (iterable) {
            case ArrayValue array -> {
                array.elements().forEach(e -> elements.add(e.copy()));
                elementType = array.elementType();
            }
            case HashValue hash -> {
                hash.elements().keySet().forEach(key -> elements.add(new StringValue(key)));
                elementType = Type.Str.UNLIMITED;
            }
            default ->
                throw C0deException.of(
                        ErrorCode.TYPE_MISMATCH, "EACH needs an ARRAY or a HASH, not a " + iterable.type());
        }
        if (elements.isEmpty()) {
            continueAfter(loop.body().endLine());
            return;
        }
        Value first = elements.remove(0);
        Variable variable = new Variable(loop.variable(), elementType, first, false, Map.of(), loop.line(), true);
        enterLoop(loop, loop.body(), new VariableItem(variable, false), new LoopItem(loop, null, null, elements));
    }

    private void whileLoop(Statement.While loop) {
        if (ExpressionEvaluator.evaluateBoolean(loop.condition(), stack)) {
            enterLoop(loop, loop.body(), null, null);
        } else {
            continueAfter(loop.body().endLine());
        }
    }

    /** Pushes the loop item, the loop variable (if any) and the block marker, and enters the body. */
    private void enterLoop(Statement loop, Statement.CodeBlock body, VariableItem variable, LoopItem item) {
        stack.push(item == null ? new LoopItem(loop, null, null, List.of()) : item);
        if (variable != null) {
            stack.push(variable);
        }
        enter(loop, body);
    }

    private void breakLoop(Statement.Break statement) {
        if (statement.condition() != null && !ExpressionEvaluator.evaluateBoolean(statement.condition(), stack)) {
            continueAfter(statement.line());
            return;
        }
        LoopItem loop = stack.findInFunction(LoopItem.class);
        if (loop == null) {
            throw C0deException.crash(ErrorCode.SYNTAX_ERROR_PLACEMENT, "BREAK must be placed inside a loop");
        }
        stack.popTo(item -> item == loop);
        continueAfter(loopEnd(loop.loop()));
    }

    private static int loopEnd(Statement loop) {
        return switch (loop) {
            case Statement.For f -> f.body().endLine();
            case Statement.Each e -> e.body().endLine();
            case Statement.While w -> w.body().endLine();
            case Statement.Repeat r -> r.body().endLine();
            default -> throw new IllegalArgumentException("Not a loop: " + loop);
        };
    }

    private void returnStatement(Statement.Return statement) {
        StructValue returned = okError();
        if (statement.error() != null) {
            Variable variable = stack.resolve(statement.error().name());
            Value value = IndexedAccess.read(statement.error(), stack);
            if (!(value instanceof StructValue struct) || struct.definition() != StructDefinition.ERROR) {
                throw C0deException.crash(
                        ErrorCode.SYNTAX_ERROR_FUNCTION,
                        "RETURN needs a variable of type ERROR, not " + variable.name());
            }
            returned = struct.copy();
        }
        returnFromFunction(returned);
    }

    private void exit(Statement.Exit exit) {
        Value value;
        try {
            value = ExpressionEvaluator.evaluate(exit.code(), stack);
        } catch (C0deException e) {
            throw e.asCrash();
        }
        if (!(value instanceof IntegerValue code) || code.value() < 0 || code.value() > CRASH_EXIT_CODE) {
            throw C0deException.crash(
                    ErrorCode.INVALID_EXIT_CODE, "The exit code must be an INTEGER from 0 to 15: " + value.text());
        }
        finish((int) code.value());
    }

    // ------------------------------------------------------------------------------------------ END lines

    private void end(EndNode end) {
        switch (end.owner()) {
            case Statement.FunctionDeclaration function -> returnFromFunction(okError());
            case Statement.If condition -> {
                popBlock();
                continueAfter(condition.endLine());
            }
            case Statement.For loop -> {
                popBlock();
                VariableItem variable = (VariableItem) stack.top();
                LoopItem item = (LoopItem) stack.items().get(stack.size() - 2);
                Value next = add(variable.variable().value(), item.step());
                if (forConditionHolds(next, item.end(), item.step())) {
                    variable.variable().set(next);
                    enter(loop, loop.body());
                } else {
                    stack.popTo(i -> i == item);
                    continueAfter(end.block().endLine());
                }
            }
            case Statement.Each loop -> {
                popBlock();
                VariableItem variable = (VariableItem) stack.top();
                LoopItem item = (LoopItem) stack.items().get(stack.size() - 2);
                if (item.remaining().isEmpty()) {
                    stack.popTo(i -> i == item);
                    continueAfter(end.block().endLine());
                } else {
                    variable.variable().set(item.remaining().remove(0));
                    enter(loop, loop.body());
                }
            }
            case Statement.While loop -> {
                popBlock();
                stack.popTo(i -> i instanceof LoopItem);
                line = loop.line();
            }
            case Statement.Repeat loop -> {
                popBlock();
                if (ExpressionEvaluator.evaluateBoolean(loop.condition(), stack)) {
                    enter(loop, loop.body());
                } else {
                    stack.popTo(i -> i instanceof LoopItem);
                    continueAfter(end.block().endLine());
                }
            }
            default -> throw new IllegalStateException("Unexpected END owner " + end.owner());
        }
    }

    private void popBlock() {
        stack.popTo(item -> item instanceof BlockMarker);
    }

    private static Value add(Value value, Value step) {
        if (value instanceof IntegerValue a && step instanceof IntegerValue b) {
            try {
                return new IntegerValue(Math.addExact(a.value(), b.value()));
            } catch (ArithmeticException e) {
                throw C0deException.of(ErrorCode.NUMERIC_OVERFLOW, "The loop variable doesn't fit into an INTEGER");
            }
        }
        return new FloatValue(Values.toDouble(value) + Values.toDouble(step));
    }

    // ------------------------------------------------------------------------------------------- calls

    private void call(Statement.Call call) {
        FunctionEntry entry = functions.get(call.function());
        if (entry == null) {
            throw C0deException.crash(ErrorCode.UNKNOWN_FUNCTION, "The function " + call.function() + " is not known");
        }
        if (entry instanceof ProgramFunction function
                && function.declaration().line() > call.line()
                && !function.name().equals(currentFunction())) {
            throw C0deException.crash(
                    ErrorCode.UNKNOWN_FUNCTION,
                    "The function " + call.function() + " is declared after the CALL statement");
        }
        if (call.errorLabel() != null) {
            Variable variable = stack.resolve(call.errorLabel());
            if (variable.constant() || variable.loopVariable() || variable.type() != Type.Struct.ERROR) {
                throw C0deException.crash(
                        ErrorCode.SYNTAX_ERROR_CALL, call.errorLabel() + " is not a variable of type ERROR");
            }
        }
        for (Statement.Call.CallResult result : call.results()) {
            if (!entry.resultNames().contains(result.name())) {
                throw C0deException.crash(
                        ErrorCode.SYNTAX_ERROR_CALL, call.function() + " has no result " + result.name());
            }
            Variable target = stack.resolve(result.target().name());
            if (target.constant() || target.loopVariable()) {
                throw C0deException.crash(
                        ErrorCode.SYNTAX_ERROR_CALL,
                        "The constant or loop variable " + target.name() + " can't receive a result");
            }
        }
        switch (entry) {
            case ProgramFunction function -> callProgramFunction(call, function);
            case LibraryFunction function -> callNativeFunction(call, function);
        }
    }

    private void callProgramFunction(Statement.Call call, ProgramFunction function) {
        Map<String, Value> arguments = new LinkedHashMap<>();
        for (Statement.Call.CallParameter parameter : call.parameters()) {
            ProgramFunction.Parameter declared = function.parameter(parameter.name())
                    .orElseThrow(() -> C0deException.crash(
                            ErrorCode.SYNTAX_ERROR_CALL, function.name() + " has no parameter " + parameter.name()));
            arguments.put(declared.name(), ExpressionEvaluator.evaluate(parameter.value(), stack));
        }
        List<Variable> parameters = new ArrayList<>();
        for (ProgramFunction.Parameter declared : function.parameters()) {
            Value argument = arguments.get(declared.name());
            Value value;
            if (argument != null) {
                value = Values.convert(argument, declared.type());
            } else if (declared.defaultValue() != null) {
                value = declared.defaultValue().copy();
            } else {
                throw C0deException.crash(
                        ErrorCode.FUNCTION_MISSING_INPUT,
                        "The parameter " + declared.name() + " of " + function.name() + " is missing");
            }
            parameters.add(
                    new Variable(declared.name(), declared.type(), value, false, Map.of(), declared.line(), false));
        }
        Statement.FunctionDeclaration declaration = function.declaration();
        Integer returnLine = index.next(call.endLine() + 1);
        FunctionFrame frame = new FunctionFrame(
                function.name(),
                false,
                declaration.line(),
                call,
                returnLine == null ? program.endLine() : returnLine,
                List.of());
        stack.push(frame);
        enter(declaration, declaration.body());
        for (Variable parameter : parameters) {
            stack.push(new VariableItem(parameter, false));
        }
        pushResults(frame, function);
    }

    private void callNativeFunction(Statement.Call call, LibraryFunction entry) {
        NativeFunction function = entry.function();
        Map<String, Value> arguments = new HashMap<>();
        Map<String, Map<String, String>> tags = new HashMap<>();
        Map<String, String> labels = new HashMap<>();
        Map<String, Variable> references = new HashMap<>();
        for (Statement.Call.CallParameter parameter : call.parameters()) {
            NativeFunction.Parameter declared = function.parameter(parameter.name())
                    .orElseThrow(() -> C0deException.crash(
                            ErrorCode.SYNTAX_ERROR_CALL, entry.name() + " has no parameter " + parameter.name()));
            Optional<LabelRef> label = singleLabel(parameter.value());
            label.filter(l -> l.index().isEmpty()).ifPresent(l -> labels.put(declared.name(), l.name()));
            if (declared.byReference()) {
                Variable variable = label.filter(l -> l.index().isEmpty())
                        .map(l -> stack.resolve(l.name()))
                        .filter(v -> !v.constant() && !v.loopVariable())
                        .orElseThrow(() -> C0deException.crash(
                                ErrorCode.SYNTAX_ERROR_CALL,
                                "The parameter " + parameter.name() + " must be a variable"));
                references.put(declared.name(), variable);
                tags.put(declared.name(), variable.tags());
                arguments.put(declared.name(), variable.value());
                continue;
            }
            Value value = ExpressionEvaluator.evaluate(parameter.value(), stack);
            if (!declared.accepts().test(value)) {
                throw C0deException.of(
                        ErrorCode.TYPE_MISMATCH,
                        "The parameter " + declared.name() + " of " + entry.name() + " must be " + declared.types()
                                + ", not " + value.type());
            }
            arguments.put(declared.name(), value);
            label.filter(l -> l.index().isEmpty())
                    .flatMap(l -> stack.find(l.name()))
                    .ifPresent(variable -> tags.put(declared.name(), variable.tags()));
        }
        for (NativeFunction.Parameter declared : function.parameters()) {
            if (!arguments.containsKey(declared.name()) && !declared.optional()) {
                throw C0deException.crash(
                        ErrorCode.FUNCTION_MISSING_INPUT,
                        "The parameter " + declared.name() + " of " + entry.name() + " is missing");
            }
        }
        NativeResult result;
        try {
            result = function.implementation().call(new Call(function, arguments, tags, labels, references));
        } catch (C0deException e) {
            if (e.crash()) {
                throw e;
            }
            result = NativeResult.error(e.code().code(), e.getMessage());
        }
        if (result.retry()) {
            line = call.line();
            return;
        }
        Integer next = index.next(call.endLine() + 1);
        line = next == null ? program.endLine() : next;
        StructValue returned = okError();
        if (result.errorCode() != 0) {
            String context = result.callerContext() ? currentFunction() : entry.name();
            returned = errorValue(result.errorCode(), result.errorMessage(), context, call.line());
        }
        deliver(call, result.results(), returned);
        waitSteps = result.waitSteps();
    }

    private static Optional<LabelRef> singleLabel(Expression expression) {
        return expression instanceof Expression.Reference reference ? Optional.of(reference.label()) : Optional.empty();
    }

    /**
     * Returns from the current function with an error (code 0 if there is none): assigns the results in the
     * caller and passes the error on.
     */
    private void returnFromFunction(StructValue returned) {
        FunctionFrame frame = stack.currentFrame();
        Map<String, Value> values = new LinkedHashMap<>();
        for (Variable result : frame.results()) {
            values.put(result.name(), result.value());
        }
        stack.popTo(item -> item == frame);
        if (frame.entry()) {
            if (code(returned) != 0) {
                crashWith(returned);
            } else {
                results = values;
                finish(0);
            }
            return;
        }
        line = frame.returnLine();
        deliver(frame.call(), values, returned);
    }

    /** Assigns the results of a call in the caller and handles the returned error. */
    private void deliver(Statement.Call call, Map<String, Value> values, StructValue returned) {
        try {
            for (Statement.Call.CallResult result : call.results()) {
                Value value = values.get(result.name());
                if (value != null) {
                    IndexedAccess.write(result.target(), stack, value);
                }
            }
        } catch (C0deException e) {
            if (e.crash()) {
                throw e.inLine(call.line());
            }
            raise(errorValue(e.code().code(), e.getMessage(), currentFunction(), call.line()));
            return;
        }
        if (call.errorLabel() != null) {
            stack.resolve(call.errorLabel()).set(returned);
        } else if (code(returned) != 0) {
            raise(returned);
        }
    }

    // -------------------------------------------------------------------------------------------- errors

    private void handle(C0deException e) {
        if (e.crash() || stack.currentFrame() == null) {
            crash(e);
        } else {
            raise(errorValue(e.code().code(), e.getMessage(), currentFunction(), e.line()));
        }
    }

    /** Raises an error in the current function: it ends as if RETURN had been executed with the error. */
    private void raise(StructValue raised) {
        try {
            returnFromFunction(raised);
        } catch (C0deException e) {
            handle(e);
        }
    }

    /**
     * Ends a running cursor from outside, e.g. because a player aborted the process: it crashes with
     * PROCESS_ABORTED in its current line.
     */
    public void abort(String message) {
        if (!ended()) {
            crash(C0deException.crash(ErrorCode.PROCESS_ABORTED, message));
        }
    }

    private void crash(C0deException e) {
        crashWith(errorValue(
                e.code().code(),
                e.getMessage(),
                currentFunction(),
                e.line() == C0deException.NO_LINE ? line : e.line()));
    }

    private void crashWith(StructValue crashError) {
        state = CursorState.CRASHED;
        exitCode = CRASH_EXIT_CODE;
        error = crashError;
        results = Map.of();
    }

    private void finish(int code) {
        if (results.isEmpty()) {
            FunctionFrame entry = stack.entryFrame();
            if (entry != null) {
                Map<String, Value> values = new LinkedHashMap<>();
                for (Variable result : entry.results()) {
                    values.put(result.name(), result.value());
                }
                results = values;
            }
        }
        state = CursorState.FINISHED;
        exitCode = code;
    }

    private static StructValue okError() {
        return new StructValue(StructDefinition.ERROR);
    }

    /** An ERROR value. */
    public static StructValue errorValue(long code, String message, String context, int line) {
        StructValue value = new StructValue(StructDefinition.ERROR);
        value.set("code", new IntegerValue(code));
        value.set("message", new StringValue(message == null ? "" : message));
        value.set("context", new StringValue(context));
        value.set("line", new IntegerValue(line));
        return value;
    }

    private static long code(StructValue error) {
        return ((IntegerValue) error.get("code")).value();
    }

    // -------------------------------------------------------------------------------- native functions

    private final class Call implements NativeCall {
        private final NativeFunction function;
        private final Map<String, Value> arguments;
        private final Map<String, Map<String, String>> tags;
        private final Map<String, String> labels;
        private final Map<String, Variable> references;

        Call(
                NativeFunction function,
                Map<String, Value> arguments,
                Map<String, Map<String, String>> tags,
                Map<String, String> labels,
                Map<String, Variable> references) {
            this.function = function;
            this.arguments = arguments;
            this.tags = tags;
            this.labels = labels;
            this.references = references;
        }

        @Override
        public Value argument(String name) {
            Value value = arguments.get(name);
            if (value != null) {
                return value;
            }
            return function.parameter(name)
                    .map(NativeFunction.Parameter::defaultValue)
                    .orElse(null);
        }

        @Override
        public boolean provided(String name) {
            return arguments.containsKey(name);
        }

        @Override
        public Map<String, String> tags(String name) {
            return tags.getOrDefault(name, Map.of());
        }

        @Override
        public Optional<String> label(String name) {
            return Optional.ofNullable(labels.get(name));
        }

        @Override
        public Optional<Variable> reference(String name) {
            return Optional.ofNullable(references.get(name));
        }

        @Override
        public ProcessContext process() {
            return process;
        }
    }

    private final class Process implements ProcessContext {
        @Override
        public RandomGenerator random() {
            return random;
        }

        @Override
        public void randomize(long seed) {
            random = new C0deRandom(seed);
        }

        @Override
        public Clock clock() {
            return settings.clock();
        }

        @Override
        public ZoneId zone() {
            return settings.zone();
        }

        @Override
        public Object host() {
            return settings.host();
        }

        @Override
        public Console console() {
            return Cursor.this.console();
        }
    }

    // ------------------------------------------------------------------------------- function registry

    /** A function known to the cursor. */
    sealed interface FunctionEntry permits ProgramFunction, LibraryFunction {
        String name();

        List<String> resultNames();
    }

    /** A function of a library (or a built-in function) with its qualified name. */
    record LibraryFunction(String name, NativeFunction function) implements FunctionEntry {
        @Override
        public List<String> resultNames() {
            return function.results();
        }
    }

    // ------------------------------------------------------------------------------ access for snapshots

    Program program() {
        return program;
    }

    ProgramIndex index() {
        return index;
    }

    String entryFunction() {
        return entryFunction;
    }

    Map<String, Value> inputs() {
        return inputs;
    }

    TypeRegistry types() {
        return types;
    }

    Map<String, FunctionEntry> functions() {
        return functions;
    }

    List<String> libraries() {
        return libraries;
    }

    CallStack stack() {
        return stack;
    }

    C0deRandom random() {
        return random;
    }

    /** Restores the state of a stored cursor (see {@link CursorSnapshot}). */
    void restore(
            CursorState restoredState,
            int restoredLine,
            int restoredWait,
            int restoredExitCode,
            StructValue restoredError,
            Map<String, Value> restoredResults,
            long randomState) {
        this.state = restoredState;
        this.line = restoredLine;
        this.waitSteps = restoredWait;
        this.exitCode = restoredExitCode;
        this.error = restoredError;
        this.results = restoredResults;
        this.random = new C0deRandom(randomState);
    }

    /** Re-adds an included library when a cursor is restored. */
    void restoreLibrary(String name) {
        Library library = settings.libraries()
                .find(name)
                .orElseThrow(() -> C0deException.crash(ErrorCode.UNKNOWN_LIBRARY, "There is no library " + name));
        // Types are restored from the snapshot; constants and functions from the library
        String prefix = library.name() + "/";
        library.constants().forEach((constant, value) -> constants.put(prefix + constant, value));
        library.functions()
                .forEach((function, implementation) ->
                        functions.put(prefix + function, new LibraryFunction(prefix + function, implementation)));
        libraries.add(name);
    }

    /** Re-registers a program function when a cursor is restored. */
    void restoreFunction(Statement.FunctionDeclaration declaration) {
        functions.put(declaration.name(), ProgramFunction.resolve(declaration, types));
    }
}
