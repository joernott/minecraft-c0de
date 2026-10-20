package de.joernott.c0de.core.validation;

import de.joernott.c0de.core.cursor.ProgramFunction;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.eval.Declarations;
import de.joernott.c0de.core.eval.TypeRegistry;
import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.line.IndexEntry;
import de.joernott.c0de.core.program.Expression;
import de.joernott.c0de.core.program.LabelRef;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Walks a parsed program like a cursor would, without running it, and collects the problems the cursor would
 * crash with or raise because of names: unknown libraries, functions, variables and struct types, names that
 * are used twice, constants that are changed and calls with unknown or missing parameters and results.
 *
 * <p>Declarations are checked in the order of their lines, like the cursor executes them. The code of a function
 * may only use what is declared before the function (docs/concept/03-programs.md), so it is checked at the
 * position of the function.
 */
final class ProgramAnalyzer {
    /**
     * A function that can be called.
     *
     * @param line z coordinate of the FUNCTION line, or -1 for functions of libraries
     * @param byReference the parameters that must be variables
     */
    record Signature(
            String name,
            int line,
            List<String> parameters,
            List<String> mandatory,
            List<String> results,
            Set<String> byReference) {}

    /**
     * The names that are visible in a line.
     *
     * @param inFunction true if the line is in the code of a function that could be parsed
     */
    record Context(
            List<Symbol> symbols, Map<String, Signature> functions, List<String> structTypes, boolean inFunction) {}

    private final Program program;
    private final LibraryRegistry libraries;
    private final int probe;
    private final Map<Integer, Problem> problems = new TreeMap<>();
    private final TypeRegistry types = new TypeRegistry();
    private final Map<String, Signature> functions = new LinkedHashMap<>();
    private final Map<String, Symbol> globals = new LinkedHashMap<>();
    private final Deque<Map<String, Symbol>> locals = new ArrayDeque<>();
    private String currentFunction;
    private Context captured;

    /**
     * @param probe z coordinate of a line whose visible names are captured, or -1
     */
    ProgramAnalyzer(Program program, LibraryRegistry libraries, int probe) {
        this.program = program;
        this.libraries = libraries;
        this.probe = probe;
    }

    Map<Integer, Problem> problems() {
        return problems;
    }

    /** The names visible in the probed line, or the global names if the line is not part of a statement. */
    Context context() {
        return captured != null ? captured : snapshot();
    }

    void analyze() {
        Library builtins = libraries.builtins();
        addFunctions("", builtins);
        ErrorCode.builtinConstants().forEach((name, value) -> addConstant(name, value));
        builtins.constants().forEach(this::addConstant);
        for (Statement statement : program.statements()) {
            capture(statement);
            switch (statement) {
                case Statement.Invalid invalid -> invalid(invalid);
                case Statement.Include include -> include(include);
                case Statement.StructDeclaration struct -> struct(struct);
                // A name that is used twice keeps its first declaration
                case Statement.Declaration declaration -> globals.putIfAbsent(declaration.name(), declare(declaration));
                case Statement.FunctionDeclaration function -> {
                    if (function(function)) {
                        body(function);
                    }
                }
                default -> {
                    // The parser only allows the statements above in the global scope
                }
            }
        }
    }

    // ----------------------------------------------------------------------------------------- global scope

    private void include(Statement.Include include) {
        Library library = libraries.find(include.library()).orElse(null);
        if (library == null) {
            problem(include.line(), ErrorCode.UNKNOWN_LIBRARY, "There is no library " + include.library());
            return;
        }
        String prefix = library.name() + "/";
        addFunctions(prefix, library);
        library.types().forEach((name, definition) -> types.add(prefix + name, definition));
        library.constants().forEach((name, value) -> addConstant(prefix + name, value));
    }

    private void addFunctions(String prefix, Library library) {
        for (NativeFunction function : library.functions().values()) {
            List<String> parameters = new ArrayList<>();
            List<String> mandatory = new ArrayList<>();
            List<String> byReference = new ArrayList<>();
            for (NativeFunction.Parameter parameter : function.parameters()) {
                parameters.add(parameter.name());
                if (!parameter.optional()) {
                    mandatory.add(parameter.name());
                }
                if (parameter.byReference()) {
                    byReference.add(parameter.name());
                }
            }
            String name = prefix + function.name();
            functions.put(
                    name, new Signature(name, -1, parameters, mandatory, function.results(), Set.copyOf(byReference)));
        }
    }

    private void addConstant(String name, Value value) {
        globals.put(name, new Symbol(name, Symbol.Kind.CONSTANT, value.type()));
    }

    private void struct(Statement.StructDeclaration struct) {
        if (types.find(struct.name()).isPresent() || inUse(struct.name())) {
            problem(struct.line(), ErrorCode.SYNTAX_ERROR_STRUCT, "The name " + struct.name() + " is already used");
            return;
        }
        try {
            types.add(struct.name(), Declarations.defineStruct(struct, types));
        } catch (C0deException e) {
            problem(e, struct.line());
        }
    }

    /** Checks a VARIABLE or CONSTANT declaration and returns its symbol. */
    private Symbol declare(Statement.Declaration declaration) {
        Symbol.Kind kind = declaration.constant() ? Symbol.Kind.CONSTANT : Symbol.Kind.VARIABLE;
        unique(
                declaration.name(),
                declaration.line(),
                declaration.constant() ? ErrorCode.SYNTAX_ERROR_CONSTANT : ErrorCode.SYNTAX_ERROR_VARIABLE);
        Type type = null;
        try {
            Variable variable = Declarations.declare(declaration, types);
            type = variable.type();
        } catch (C0deException e) {
            problem(e, declaration.line());
        }
        return new Symbol(declaration.name(), kind, type);
    }

    /** Checks the declaration of a function; returns true if its code can be checked. */
    private boolean function(Statement.FunctionDeclaration declaration) {
        if (functions.containsKey(declaration.name()) || inUse(declaration.name())) {
            problem(
                    declaration.line(),
                    ErrorCode.SYNTAX_ERROR_FUNCTION,
                    "The name " + declaration.name() + " is already used");
            return false;
        }
        try {
            ProgramFunction function = ProgramFunction.resolve(declaration, types);
            List<String> parameters = new ArrayList<>();
            List<String> mandatory = new ArrayList<>();
            for (ProgramFunction.Parameter parameter : function.parameters()) {
                parameters.add(parameter.name());
                if (parameter.defaultValue() == null) {
                    mandatory.add(parameter.name());
                }
            }
            functions.put(
                    declaration.name(),
                    new Signature(
                            declaration.name(),
                            declaration.line(),
                            parameters,
                            mandatory,
                            function.resultNames(),
                            Set.of()));
            return true;
        } catch (C0deException e) {
            problem(e, declaration.line());
            return false;
        }
    }

    private void body(Statement.FunctionDeclaration declaration) {
        currentFunction = declaration.name();
        ProgramFunction function = ProgramFunction.resolve(declaration, types);
        Map<String, Symbol> frame = new LinkedHashMap<>();
        function.parameters()
                .forEach(parameter -> frame.put(
                        parameter.name(), new Symbol(parameter.name(), Symbol.Kind.PARAMETER, parameter.type())));
        function.results()
                .forEach(result ->
                        frame.put(result.name(), new Symbol(result.name(), Symbol.Kind.RESULT, result.type())));
        locals.push(frame);
        if (probe >= declaration.line() && probe <= declaration.body().endLine()) {
            captured = snapshot();
        }
        block(declaration.body());
        locals.pop();
        currentFunction = null;
    }

    // ----------------------------------------------------------------------------------------- statements

    private void block(Statement.CodeBlock block) {
        locals.push(new LinkedHashMap<>());
        for (Statement statement : block.statements()) {
            statement(statement);
        }
        locals.pop();
    }

    private void statement(Statement statement) {
        capture(statement);
        switch (statement) {
            case Statement.Invalid invalid -> invalid(invalid);
            case Statement.Declaration declaration -> {
                Symbol symbol = declare(declaration);
                locals.peek().putIfAbsent(symbol.name(), symbol);
            }
            case Statement.Assign assign -> {
                writable(assign.target(), assign.line(), ErrorCode.SYNTAX_ERROR_ASSIGN);
                expression(assign.value(), assign.line());
            }
            case Statement.Call call -> call(call);
            case Statement.If condition -> {
                expression(condition.condition(), condition.line());
                block(condition.then());
                if (condition.otherwise() != null) {
                    block(condition.otherwise());
                }
            }
            case Statement.For loop -> {
                unique(loop.variable(), loop.line(), ErrorCode.SYNTAX_ERROR_LOOP);
                expression(loop.from(), loop.line());
                expression(loop.to(), loop.line());
                if (loop.step() != null) {
                    expression(loop.step(), loop.line());
                }
                loopBody(new Symbol(loop.variable(), Symbol.Kind.LOOP_VARIABLE, loop.type()), loop.body());
            }
            case Statement.Each loop -> {
                unique(loop.variable(), loop.line(), ErrorCode.SYNTAX_ERROR_LOOP);
                reference(loop.iterable(), loop.line());
                loopBody(new Symbol(loop.variable(), Symbol.Kind.LOOP_VARIABLE, null), loop.body());
            }
            case Statement.While loop -> {
                expression(loop.condition(), loop.line());
                block(loop.body());
            }
            case Statement.Repeat loop -> {
                block(loop.body());
                expression(loop.condition(), loop.body().endLine());
            }
            case Statement.Break breakStatement -> {
                if (breakStatement.condition() != null) {
                    expression(breakStatement.condition(), breakStatement.line());
                }
            }
            case Statement.Return returnStatement -> {
                if (returnStatement.error() != null) {
                    reference(returnStatement.error(), returnStatement.line());
                }
            }
            case Statement.Exit exit -> {
                if (exit.code() != null) {
                    expression(exit.code(), exit.line());
                }
            }
            default -> {
                // INCLUDE, STRUCT and FUNCTION are only parsed in the global scope
            }
        }
    }

    private void loopBody(Symbol variable, Statement.CodeBlock body) {
        Map<String, Symbol> frame = new LinkedHashMap<>();
        frame.put(variable.name(), variable);
        locals.push(frame);
        if (probe > body.beginLine() && probe <= body.endLine()) {
            captured = snapshot();
        }
        block(body);
        locals.pop();
    }

    private void call(Statement.Call call) {
        Signature function = functions.get(call.function());
        if (function == null) {
            problem(
                    call.line(),
                    ErrorCode.UNKNOWN_FUNCTION,
                    call.function().contains("/")
                            ? "The function " + call.function() + " is not known; is its library included?"
                            : "The function " + call.function() + " is not known");
        } else if (function.line() > call.line() && !function.name().equals(currentFunction)) {
            problem(
                    call.line(),
                    ErrorCode.UNKNOWN_FUNCTION,
                    "The function " + call.function() + " is declared after the CALL statement");
        }
        if (call.errorLabel() != null) {
            Symbol error = resolve(call.errorLabel());
            if (error == null) {
                problem(call.line(), ErrorCode.UNKNOWN_NAME, "The name " + call.errorLabel() + " is not declared");
            } else if (!error.isWritable() || (error.type() != null && error.type() != Type.Struct.ERROR)) {
                problem(
                        call.line(),
                        ErrorCode.SYNTAX_ERROR_CALL,
                        call.errorLabel() + " is not a variable of type ERROR");
            }
        }
        List<String> provided = new ArrayList<>();
        for (Statement.Call.CallParameter parameter : call.parameters()) {
            provided.add(parameter.name());
            if (function != null && !function.parameters().contains(parameter.name())) {
                problem(
                        parameter.line(),
                        ErrorCode.SYNTAX_ERROR_CALL,
                        call.function() + " has no parameter " + parameter.name());
            }
            if (function != null
                    && function.byReference().contains(parameter.name())
                    && !(parameter.value() instanceof Expression.Reference)) {
                problem(parameter.line(), ErrorCode.SYNTAX_ERROR_CALL, parameter.name() + " must be a variable");
            }
            expression(parameter.value(), parameter.line());
        }
        if (function != null) {
            for (String mandatory : function.mandatory()) {
                if (!provided.contains(mandatory)) {
                    problem(
                            call.line(),
                            ErrorCode.FUNCTION_MISSING_INPUT,
                            "The parameter " + mandatory + " of " + call.function() + " is missing");
                }
            }
        }
        for (Statement.Call.CallResult result : call.results()) {
            if (function != null && !function.results().contains(result.name())) {
                problem(
                        result.line(),
                        ErrorCode.SYNTAX_ERROR_CALL,
                        call.function() + " has no result " + result.name());
            }
            writable(result.target(), result.line(), ErrorCode.SYNTAX_ERROR_CALL);
        }
    }

    // ----------------------------------------------------------------------------------------------- names

    private void expression(Expression expression, int line) {
        switch (expression) {
            case Expression.Literal literal -> {
                // Literals need no names
            }
            case Expression.Reference reference -> reference(reference.label(), line);
            case Expression.Unary unary -> expression(unary.operand(), line);
            case Expression.Binary binary -> {
                expression(binary.left(), line);
                expression(binary.right(), line);
            }
        }
    }

    /** Checks that the name and the variables of the index of a label are declared. */
    private Symbol reference(LabelRef label, int line) {
        Symbol symbol = resolve(label.name());
        if (symbol == null) {
            problem(line, ErrorCode.UNKNOWN_NAME, "The name " + label.name() + " is not declared");
        }
        for (IndexEntry entry : label.index()) {
            if (entry instanceof IndexEntry.Variable variable && resolve(variable.name()) == null) {
                problem(line, ErrorCode.UNKNOWN_NAME, "The name " + variable.name() + " is not declared");
            }
        }
        return symbol;
    }

    private void writable(LabelRef label, int line, ErrorCode code) {
        Symbol symbol = reference(label, line);
        if (symbol != null && !symbol.isWritable()) {
            problem(line, code, "The constant or loop variable " + symbol.name() + " can't be changed");
        }
    }

    private Symbol resolve(String name) {
        for (Map<String, Symbol> frame : locals) {
            Symbol symbol = frame.get(name);
            if (symbol != null) {
                return symbol;
            }
        }
        return globals.get(name);
    }

    private boolean inUse(String name) {
        return resolve(name) != null || functions.containsKey(name);
    }

    private void unique(String name, int line, ErrorCode code) {
        if (inUse(name) || types.find(name).isPresent()) {
            problem(line, code, "The name " + name + " is already used");
        }
    }

    // ---------------------------------------------------------------------------------------------- results

    private void invalid(Statement.Invalid invalid) {
        C0deException error = invalid.error();
        int errorLine = error.line() == C0deException.NO_LINE ? invalid.line() : error.line();
        Problem problem = new Problem(errorLine, error.code(), error.getMessage());
        problems.putIfAbsent(errorLine, problem);
        for (int z = invalid.line(); z <= invalid.endLine(); z++) {
            problems.putIfAbsent(z, problem);
        }
    }

    private void problem(int line, ErrorCode code, String message) {
        problems.putIfAbsent(line, new Problem(line, code, message));
    }

    private void problem(C0deException e, int line) {
        int errorLine = e.line() == C0deException.NO_LINE ? line : e.line();
        problem(errorLine, e.code(), e.getMessage());
    }

    private void capture(Statement statement) {
        if (probe >= statement.line() && probe <= endLine(statement)) {
            captured = snapshot();
        }
    }

    private Context snapshot() {
        Map<String, Symbol> visible = new LinkedHashMap<>(globals);
        List<Map<String, Symbol>> frames = new ArrayList<>(locals);
        // Inner frames hide outer ones, so they are added last
        for (int i = frames.size() - 1; i >= 0; i--) {
            visible.putAll(frames.get(i));
        }
        Map<String, Signature> callable = new LinkedHashMap<>();
        functions.forEach((name, signature) -> {
            if (signature.line() < 0 || signature.line() < probe || name.equals(currentFunction)) {
                callable.put(name, signature);
            }
        });
        return new Context(
                List.copyOf(visible.values()), callable, List.copyOf(types.all().keySet()), currentFunction != null);
    }

    static int endLine(Statement statement) {
        return switch (statement) {
            case Statement.Invalid invalid -> invalid.endLine();
            case Statement.Include include -> include.line();
            case Statement.StructDeclaration struct -> struct.endLine();
            case Statement.Declaration declaration -> declaration.endLine();
            case Statement.FunctionDeclaration function -> function.body().endLine();
            case Statement.Assign assign -> assign.endLine();
            case Statement.Call call -> call.endLine();
            case Statement.If condition -> condition.endLine();
            case Statement.For loop -> loop.body().endLine();
            case Statement.Each loop -> loop.body().endLine();
            case Statement.While loop -> loop.body().endLine();
            case Statement.Repeat loop -> loop.body().endLine();
            case Statement.Break breakStatement -> breakStatement.line();
            case Statement.Return returnStatement -> returnStatement.line();
            case Statement.Exit exit -> exit.line();
        };
    }
}
