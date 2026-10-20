package de.joernott.c0de.core.program;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Block.KeywordBlock;
import de.joernott.c0de.core.line.Block.LabelBlock;
import de.joernott.c0de.core.line.Block.LiteralBlock;
import de.joernott.c0de.core.line.Block.OperatorBlock;
import de.joernott.c0de.core.line.Block.TagBlock;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Operator;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns the lines of a sheet into a {@link Program}. It checks the syntax as described in
 * docs/concept/15-cursor.md: the NOOP and COMMENT rules, indentation, placement and the matching of BEGIN
 * and END. A statement with an error becomes a {@link Statement.Invalid}; parsing continues after it, so
 * that all errors can be reported.
 */
public final class ProgramParser {
    /** Maximum nesting depth of initializers and of arrays and hashes. */
    public static final int MAX_DEPTH = 8;

    private static final int GLOBAL_X = SheetGeometry.CODE_MIN_X;

    private final Sheet sheet;
    private final List<Row> rows = new ArrayList<>();
    private int position;

    private ProgramParser(Sheet sheet) {
        this.sheet = sheet;
    }

    /** Parses the program on a sheet. */
    public static Program parse(Sheet sheet) {
        return new ProgramParser(sheet).parseProgram();
    }

    // ---------------------------------------------------------------------------------------------- rows

    /** A line that is processed by the parser: NOOP and comment lines are left out. */
    private record Row(int z, List<Line.Cell> cells, C0deException noopError) {
        int x() {
            return cells.get(0).x();
        }

        Block first() {
            return cells.get(0).block();
        }

        boolean startsWith(Keyword keyword) {
            return first() instanceof KeywordBlock(Keyword k) && k == keyword;
        }

        /** True if the line contains only the keyword. */
        boolean isOnly(Keyword keyword) {
            return cells.size() == 1 && startsWith(keyword);
        }
    }

    private int readRows() {
        int z = 0;
        while (!sheet.line(z).isEmpty()) {
            Line line = sheet.line(z);
            List<Line.Cell> cells = line.cells();
            Block first = cells.get(0).block();
            if (first instanceof KeywordBlock(Keyword keyword) && keyword == Keyword.NOOP) {
                if (cells.size() > 1) {
                    rows.add(new Row(
                            z,
                            cells,
                            C0deException.at(
                                    z, ErrorCode.SYNTAX_ERROR_PLACEMENT, "A NOOP line must not contain other blocks")));
                }
            } else if (!(first instanceof Block.CommentBlock)) {
                List<Line.Cell> content = new ArrayList<>();
                for (Line.Cell cell : cells) {
                    if (cell.block() instanceof Block.CommentBlock) {
                        break;
                    }
                    content.add(cell);
                }
                rows.add(new Row(z, content, null));
            }
            z++;
        }
        return z;
    }

    private boolean isNoopLine(int z) {
        Line line = sheet.line(z);
        return line.cells().size() == 1
                && line.cells().get(0).block() instanceof KeywordBlock(Keyword keyword)
                && keyword == Keyword.NOOP;
    }

    private Row current() {
        return position < rows.size() ? rows.get(position) : null;
    }

    // ------------------------------------------------------------------------------------------- program

    private Program parseProgram() {
        int end = readRows();
        List<Statement> statements = new ArrayList<>();
        String name = null;
        Row header = current();
        if (header == null || header.z() != 0) {
            statements.add(
                    invalid(0, ErrorCode.SYNTAX_ERROR_PLACEMENT, "A program must start with a PROGRAM line at z=0"));
        } else {
            position++;
            try {
                name = parseProgramLine(header);
            } catch (C0deException e) {
                statements.add(new Statement.Invalid(header.z(), e));
            }
            if (end > 1 && !isNoopLine(1)) {
                statements.add(
                        invalid(1, ErrorCode.SYNTAX_ERROR_PLACEMENT, "The line after PROGRAM must be a NOOP line"));
            }
        }
        boolean declarations = false;
        while (current() != null) {
            Row row = current();
            int start = position;
            try {
                Statement statement = parseGlobal(row, declarations);
                declarations |= !(statement instanceof Statement.Include);
                statements.add(statement);
            } catch (C0deException e) {
                skipRest(start, row);
                statements.add(new Statement.Invalid(row.z(), e.inLine(row.z()), lastSkipped()));
            }
        }
        return new Program(name, statements, end);
    }

    private String parseProgramLine(Row row) {
        Cells cells = new Cells(row);
        if (row.x() != GLOBAL_X || !row.startsWith(Keyword.PROGRAM)) {
            throw C0deException.at(
                    row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "A program must start with a PROGRAM block at x=5");
        }
        cells.next();
        String name = cells.label(ErrorCode.SYNTAX_ERROR_PLACEMENT, "the program name");
        cells.end(ErrorCode.SYNTAX_ERROR_PLACEMENT);
        return name;
    }

    private Statement parseGlobal(Row row, boolean afterDeclarations) {
        if (row.noopError() != null) {
            position++;
            throw row.noopError();
        }
        if (!(row.first() instanceof KeywordBlock(Keyword keyword))) {
            throw C0deException.at(
                    row.z(), ErrorCode.SYNTAX_ERROR_UNKNOWN_STATEMENT, "A line must start with a keyword");
        }
        if (keyword == Keyword.INCLUDE) {
            if (row.x() != GLOBAL_X + 1) {
                throw indentation(row, GLOBAL_X + 1);
            }
            if (afterDeclarations) {
                throw C0deException.at(
                        row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "INCLUDE lines must come before all declarations");
            }
            position++;
            Cells cells = new Cells(row);
            cells.next();
            String library = cells.qualifiedLabel(ErrorCode.SYNTAX_ERROR_PLACEMENT, "the library");
            if (!Names.isValidLibrary(library)) {
                throw C0deException.at(row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "Not a library name: " + library);
            }
            cells.end(ErrorCode.SYNTAX_ERROR_PLACEMENT);
            return new Statement.Include(row.z(), library);
        }
        if (row.x() != GLOBAL_X) {
            throw indentation(row, GLOBAL_X);
        }
        return switch (keyword) {
            case STRUCT -> parseStruct(row);
            case VARIABLE, CONSTANT -> parseDeclaration(row);
            case FUNCTION -> parseFunction(row);
            case CALL, ASSIGN, IF, ELSE, FOR, EACH, WHILE, REPEAT, BREAK, RETURN, EXIT ->
                throw C0deException.at(
                        row.z(),
                        ErrorCode.SYNTAX_ERROR_PLACEMENT,
                        keyword + " must be placed inside the code block of a function");
            case BEGIN ->
                throw C0deException.at(
                        row.z(),
                        ErrorCode.SYNTAX_ERROR_PLACEMENT,
                        "BEGIN must be part of a statement that requires a code block");
            case END ->
                throw C0deException.at(row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "END without a matching BEGIN");
            case PARAMETER, RESULT ->
                throw C0deException.at(
                        row.z(),
                        ErrorCode.SYNTAX_ERROR_PLACEMENT,
                        keyword + " lines must follow a FUNCTION or CALL line");
            case PROGRAM ->
                throw C0deException.at(
                        row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "A program has only one PROGRAM line");
            default ->
                throw C0deException.at(
                        row.z(), ErrorCode.SYNTAX_ERROR_UNKNOWN_STATEMENT, keyword + " can't start a statement");
        };
    }

    /**
     * After an error: skips the rest of the statement that started at the given position, i.e. its
     * continuation lines and code blocks.
     */
    private void skipRest(int start, Row first) {
        position = Math.max(position, start + 1);
        int x = first.x();
        while (current() != null) {
            Row row = current();
            if (row.x() > x) {
                position++;
            } else if (row.x() == x && row.startsWith(Keyword.BEGIN)) {
                position++;
                skipToEnd(x);
            } else if (row.x() == x && (row.startsWith(Keyword.ELSE) || row.startsWith(Keyword.END))) {
                position++;
            } else {
                return;
            }
        }
    }

    /** z coordinate of the last line that was processed. */
    private int lastSkipped() {
        return rows.get(position - 1).z();
    }

    private void skipToEnd(int x) {
        while (current() != null) {
            Row row = current();
            position++;
            if (row.x() == x && row.startsWith(Keyword.END)) {
                return;
            }
        }
    }

    // ---------------------------------------------------------------------------------------- code blocks

    /** Context of a statement in a code block. */
    private record Context(int loops) {
        Context inLoop() {
            return new Context(loops + 1);
        }
    }

    /** A parsed code block and its END line. */
    private record BlockAndEnd(Statement.CodeBlock block, Row end) {}

    /**
     * Parses a code block whose BEGIN line is the current line. The statements inside are indented by one
     * block. The END line is returned to the caller, which checks the keyword following END.
     */
    private BlockAndEnd parseCodeBlock(int x, Context context, ErrorCode missingBegin, String owner) {
        Row begin = current();
        if (begin == null || !begin.startsWith(Keyword.BEGIN)) {
            int z = begin == null ? sheet.size() : begin.z();
            throw C0deException.at(z, missingBegin, owner + " must be followed by a BEGIN line");
        }
        if (begin.x() != x) {
            throw indentation(begin, x);
        }
        if (!begin.isOnly(Keyword.BEGIN)) {
            throw C0deException.at(
                    begin.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "A BEGIN line must not contain other blocks");
        }
        position++;
        List<Statement> statements = new ArrayList<>();
        while (true) {
            Row row = current();
            if (row == null || (row.x() <= x && !(row.x() == x && row.startsWith(Keyword.END)))) {
                throw C0deException.at(
                        begin.z(), ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END, "The BEGIN line has no matching END line");
            }
            if (row.x() == x) {
                // END line
                if (row.z() == begin.z() + 1) {
                    throw C0deException.at(
                            row.z(),
                            ErrorCode.SYNTAX_ERROR_PLACEMENT,
                            "A code block must contain at least one statement");
                }
                if (statements.isEmpty() && !containsNoopLine(begin.z(), row.z())) {
                    throw C0deException.at(
                            row.z(),
                            ErrorCode.SYNTAX_ERROR_PLACEMENT,
                            "A code block must contain at least one statement");
                }
                position++;
                return new BlockAndEnd(new Statement.CodeBlock(begin.z(), statements, row.z()), row);
            }
            int start = position;
            try {
                if (row.noopError() != null) {
                    position++;
                    throw row.noopError();
                }
                if (row.x() != x + 1) {
                    throw indentation(row, x + 1);
                }
                statements.add(parseStatement(row, context));
            } catch (C0deException e) {
                skipRest(start, row);
                statements.add(new Statement.Invalid(row.z(), e.inLine(row.z()), lastSkipped()));
            }
        }
    }

    private boolean containsNoopLine(int from, int to) {
        for (int z = from + 1; z < to; z++) {
            if (isNoopLine(z)) {
                return true;
            }
        }
        return false;
    }

    /** Checks the blocks after the END block of a code block: exactly the given keyword. */
    private static void expectEnd(Row end, Keyword keyword, ErrorCode error) {
        Cells cells = new Cells(end);
        cells.next();
        Block next = cells.next();
        if (!(next instanceof KeywordBlock(Keyword k)) || k != keyword) {
            throw C0deException.at(end.z(), error, "The END line must be followed by " + keyword);
        }
        cells.end(error);
    }

    private Statement parseStatement(Row row, Context context) {
        if (!(row.first() instanceof KeywordBlock(Keyword keyword))) {
            throw C0deException.at(
                    row.z(), ErrorCode.SYNTAX_ERROR_UNKNOWN_STATEMENT, "A line must start with a keyword");
        }
        return switch (keyword) {
            case VARIABLE, CONSTANT -> parseDeclaration(row);
            case ASSIGN -> parseAssign(row);
            case CALL -> parseCall(row);
            case IF -> parseIf(row, context);
            case FOR -> parseFor(row, context);
            case EACH -> parseEach(row, context);
            case WHILE -> parseWhile(row, context);
            case REPEAT -> parseRepeat(row, context);
            case BREAK -> parseBreak(row, context);
            case RETURN -> parseReturn(row);
            case EXIT -> parseExit(row);
            case ELSE ->
                throw C0deException.at(
                        row.z(),
                        ErrorCode.SYNTAX_ERROR_PLACEMENT,
                        "ELSE must follow the END line of the first code block of an IF statement");
            case STRUCT, FUNCTION, INCLUDE, PROGRAM ->
                throw C0deException.at(
                        row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, keyword + " must not be placed inside a code block");
            case PARAMETER, RESULT ->
                throw C0deException.at(
                        row.z(),
                        ErrorCode.SYNTAX_ERROR_PLACEMENT,
                        keyword + " lines must follow a FUNCTION or CALL line");
            case BEGIN ->
                throw C0deException.at(
                        row.z(),
                        ErrorCode.SYNTAX_ERROR_PLACEMENT,
                        "BEGIN must be part of a statement that requires a code block");
            case END ->
                throw C0deException.at(row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "END without a matching BEGIN");
            default ->
                throw C0deException.at(
                        row.z(), ErrorCode.SYNTAX_ERROR_UNKNOWN_STATEMENT, keyword + " can't start a statement");
        };
    }

    // ---------------------------------------------------------------------------------------- statements

    private Statement parseStruct(Row row) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_STRUCT;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        String name = cells.name(error, "the name of the struct");
        cells.end(error);
        Row begin = current();
        if (begin == null || !begin.isOnly(Keyword.BEGIN) || begin.x() != row.x()) {
            throw C0deException.at(row.z(), error, "A STRUCT line must be followed by a BEGIN line at x=" + row.x());
        }
        position++;
        List<Statement.StructDeclaration.Element> elements = new ArrayList<>();
        Set<String> names = new HashSet<>();
        while (true) {
            Row element = current();
            if (element == null
                    || element.x() < row.x()
                    || (element.x() == row.x() && !element.startsWith(Keyword.END))) {
                throw C0deException.at(
                        begin.z(), ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END, "The BEGIN line has no matching END line");
            }
            if (element.x() == row.x()) {
                expectEnd(element, Keyword.STRUCT, error);
                position++;
                break;
            }
            position++;
            if (element.noopError() != null) {
                throw element.noopError();
            }
            if (element.x() != row.x() + 1) {
                throw C0deException.at(element.z(), error, "The elements of a struct must be at x=" + (row.x() + 1));
            }
            Cells elementCells = new Cells(element);
            String elementName = elementCells.name(error, "the name of the element");
            if (!names.add(elementName)) {
                throw C0deException.at(element.z(), error, "The element " + elementName + " is declared twice");
            }
            TypeSpec type = parseType(elementCells, error, 1);
            Map<String, String> tags = parseTags(elementCells, error, false);
            elementCells.end(error);
            elements.add(new Statement.StructDeclaration.Element(element.z(), elementName, type, tags));
        }
        if (elements.isEmpty()) {
            throw C0deException.at(row.z(), error, "A struct must have at least one element");
        }
        return new Statement.StructDeclaration(
                row.z(), name, elements, rows.get(position - 1).z());
    }

    private Statement parseDeclaration(Row row) {
        boolean constant = row.startsWith(Keyword.CONSTANT);
        ErrorCode error = constant ? ErrorCode.SYNTAX_ERROR_CONSTANT : ErrorCode.SYNTAX_ERROR_VARIABLE;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        String name = cells.name(error, "the name");
        TypeSpec type = parseType(cells, error, 1);
        Value literal = null;
        if (cells.peek() instanceof LiteralBlock(Value value)) {
            cells.next();
            if (value == null) {
                throw C0deException.at(row.z(), ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, "The LITERAL has no value");
            }
            if (type.isComplex()) {
                throw C0deException.at(
                        row.z(), error, "A " + type + " is initialized with a code block, not a literal");
            }
            literal = value;
        }
        Map<String, String> tags = parseTags(cells, error, true);
        cells.end(error);
        Initializer initializer = null;
        Row next = current();
        if (type.isComplex() && next != null && next.startsWith(Keyword.BEGIN) && next.x() == row.x()) {
            initializer = parseInitializer(row.x(), 1, error, constant ? Keyword.CONSTANT : Keyword.VARIABLE);
        }
        if (constant && !type.isComplex() && literal == null) {
            throw C0deException.at(row.z(), error, "A constant must be initialized with a literal");
        }
        if (constant && type.isComplex() && initializer == null) {
            throw C0deException.at(row.z(), error, "A constant must be initialized with a code block");
        }
        return new Statement.Declaration(row.z(), constant, name, type, literal, tags, initializer);
    }

    private Initializer parseInitializer(int x, int depth, ErrorCode error, Keyword closing) {
        Row begin = current();
        if (!begin.isOnly(Keyword.BEGIN)) {
            throw C0deException.at(
                    begin.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "A BEGIN line must not contain other blocks");
        }
        position++;
        List<Initializer.Entry> entries = new ArrayList<>();
        while (true) {
            Row row = current();
            if (row == null || row.x() < x || (row.x() == x && !row.startsWith(Keyword.END))) {
                throw C0deException.at(
                        begin.z(), ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END, "The BEGIN line has no matching END line");
            }
            if (row.x() == x) {
                if (closing == null) {
                    if (!row.isOnly(Keyword.END)) {
                        throw C0deException.at(
                                row.z(), error, "The END line of a nested element must not contain other blocks");
                    }
                } else {
                    expectEnd(row, closing, ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END);
                }
                position++;
                return new Initializer(begin.z(), entries, row.z());
            }
            if (row.noopError() != null) {
                position++;
                throw row.noopError();
            }
            if (row.x() != x + 1) {
                throw indentation(row, x + 1);
            }
            entries.add(parseInitializerEntry(row, x + 1, depth, error));
        }
    }

    private Initializer.Entry parseInitializerEntry(Row row, int x, int depth, ErrorCode error) {
        Block first = row.first();
        if (row.isOnly(Keyword.BEGIN)) {
            if (depth >= MAX_DEPTH) {
                throw C0deException.at(row.z(), error, "Initializers can be nested up to a depth of " + MAX_DEPTH);
            }
            return new Initializer.Nested(row.z(), parseInitializer(x, depth + 1, error, null));
        }
        position++;
        Cells cells = new Cells(row);
        if (first instanceof LiteralBlock) {
            List<Value> values = new ArrayList<>();
            while (cells.peek() instanceof LiteralBlock(Value value)) {
                cells.next();
                if (value == null) {
                    throw C0deException.at(row.z(), ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, "A LITERAL has no value");
                }
                values.add(value);
            }
            cells.end(error);
            return new Initializer.Values(row.z(), values);
        }
        if (first instanceof LabelBlock) {
            String key = cells.label(error, "the key");
            if (cells.peek() instanceof LiteralBlock(Value value)) {
                cells.next();
                if (value == null) {
                    throw C0deException.at(row.z(), ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, "The LITERAL has no value");
                }
                cells.end(error);
                return new Initializer.Keyed(row.z(), key, value);
            }
            cells.end(error);
            Row next = current();
            if (next == null || !next.startsWith(Keyword.BEGIN) || next.x() != x) {
                throw C0deException.at(row.z(), error, "A key without a value must be followed by a BEGIN line");
            }
            if (depth >= MAX_DEPTH) {
                throw C0deException.at(row.z(), error, "Initializers can be nested up to a depth of " + MAX_DEPTH);
            }
            return new Initializer.KeyedNested(row.z(), key, parseInitializer(x, depth + 1, error, null));
        }
        throw C0deException.at(row.z(), error, "A line of an initializer must contain literals or a key");
    }

    private Statement parseFunction(Row row) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_FUNCTION;
        if (!isNoopLine(row.z() - 1)) {
            position++;
            throw C0deException.at(
                    row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "The line before a FUNCTION line must be a NOOP line");
        }
        position++;
        Cells cells = new Cells(row);
        cells.next();
        String name = cells.name(error, "the name of the function");
        cells.end(error);
        List<Statement.FunctionDeclaration.Parameter> parameters = new ArrayList<>();
        List<Statement.FunctionDeclaration.Result> results = new ArrayList<>();
        Set<String> names = new HashSet<>();
        while (current() != null
                && current().x() == row.x() + 1
                && (current().startsWith(Keyword.PARAMETER) || current().startsWith(Keyword.RESULT))) {
            Row line = current();
            position++;
            Cells lineCells = new Cells(line);
            boolean parameter = line.startsWith(Keyword.PARAMETER);
            if (parameter && !results.isEmpty()) {
                throw C0deException.at(
                        line.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "PARAMETER lines must come before RESULT lines");
            }
            lineCells.next();
            String itemName = lineCells.name(error, "the name");
            if (!names.add(itemName)) {
                throw C0deException.at(line.z(), error, "The name " + itemName + " is used twice in the function");
            }
            TypeSpec type = parseType(lineCells, error, 1);
            if (parameter) {
                Value defaultValue = null;
                if (lineCells.peek() instanceof LiteralBlock(Value value)) {
                    lineCells.next();
                    if (value == null || type.isComplex()) {
                        throw C0deException.at(line.z(), ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, "Invalid default value");
                    }
                    defaultValue = value;
                }
                lineCells.end(error);
                parameters.add(new Statement.FunctionDeclaration.Parameter(line.z(), itemName, type, defaultValue));
            } else {
                lineCells.end(error);
                results.add(new Statement.FunctionDeclaration.Result(line.z(), itemName, type));
            }
        }
        BlockAndEnd body = parseCodeBlock(row.x(), new Context(0), error, "A FUNCTION line");
        Cells end = new Cells(body.end());
        end.next();
        if (!(end.next() instanceof KeywordBlock(Keyword k)) || k != Keyword.FUNCTION) {
            throw C0deException.at(
                    body.end().z(),
                    ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END,
                    "The END line must be followed by FUNCTION");
        }
        if (end.peek() != null) {
            String repeated = end.name(error, "the function name");
            if (!repeated.equals(name)) {
                throw C0deException.at(
                        body.end().z(), error, "The END line repeats the name " + repeated + " instead of " + name);
            }
        }
        end.end(error);
        return new Statement.FunctionDeclaration(row.z(), name, parameters, results, body.block());
    }

    private Statement parseAssign(Row row) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_ASSIGN;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        LabelRef target = cells.reference(error, "the target variable");
        ExpressionReader reader = new ExpressionReader(cells, row.x() + 1);
        Expression value = reader.read(error, "the value");
        reader.end(error);
        return new Statement.Assign(row.z(), target, value, reader.lastLine());
    }

    private Statement parseCall(Row row) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_CALL;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        String function = cells.qualifiedLabel(error, "the function");
        String errorLabel = null;
        if (cells.peek() != null) {
            errorLabel = cells.name(error, "the error variable");
        }
        cells.end(error);
        List<Statement.Call.CallParameter> parameters = new ArrayList<>();
        List<Statement.Call.CallResult> results = new ArrayList<>();
        Set<String> parameterNames = new HashSet<>();
        Set<String> resultNames = new HashSet<>();
        int last = row.z();
        while (current() != null
                && current().x() == row.x() + 1
                && (current().startsWith(Keyword.PARAMETER) || current().startsWith(Keyword.RESULT))) {
            Row line = current();
            position++;
            Cells lineCells = new Cells(line);
            lineCells.next();
            if (line.startsWith(Keyword.PARAMETER)) {
                if (!results.isEmpty()) {
                    throw C0deException.at(
                            line.z(),
                            ErrorCode.SYNTAX_ERROR_PLACEMENT,
                            "PARAMETER lines must come before RESULT lines");
                }
                String name = lineCells.name(error, "the name of the parameter");
                if (!parameterNames.add(name)) {
                    throw C0deException.at(line.z(), error, "The parameter " + name + " is provided twice");
                }
                ExpressionReader reader = new ExpressionReader(lineCells, line.x() + 1);
                Expression value = reader.read(error, "the value of the parameter");
                reader.end(error);
                parameters.add(new Statement.Call.CallParameter(line.z(), name, value));
                last = reader.lastLine();
            } else {
                String name = lineCells.name(error, "the name of the result");
                if (!resultNames.add(name)) {
                    throw C0deException.at(line.z(), error, "The result " + name + " is used twice");
                }
                LabelRef target = lineCells.reference(error, "the target variable");
                lineCells.end(error);
                results.add(new Statement.Call.CallResult(line.z(), name, target));
                last = line.z();
            }
        }
        return new Statement.Call(row.z(), function, errorLabel, parameters, results, last);
    }

    private Statement parseIf(Row row, Context context) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_CONDITION;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        ExpressionReader reader = new ExpressionReader(cells, row.x() + 1);
        Expression condition = reader.read(error, "the condition");
        reader.end(error);
        BlockAndEnd then = parseCodeBlock(row.x(), context, error, "An IF line");
        if (!then.end().isOnly(Keyword.END)) {
            expectEnd(then.end(), Keyword.IF, ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END);
            return new Statement.If(row.z(), condition, then.block(), -1, null);
        }
        Row elseRow = current();
        if (elseRow == null || !elseRow.startsWith(Keyword.ELSE) || elseRow.x() != row.x()) {
            throw C0deException.at(then.end().z(), error, "A standalone END must be followed by an ELSE line");
        }
        if (!elseRow.isOnly(Keyword.ELSE)) {
            throw C0deException.at(elseRow.z(), error, "An ELSE line must not contain other blocks");
        }
        position++;
        BlockAndEnd otherwise = parseCodeBlock(row.x(), context, error, "An ELSE line");
        expectEnd(otherwise.end(), Keyword.IF, error);
        return new Statement.If(row.z(), condition, then.block(), elseRow.z(), otherwise.block());
    }

    private Statement parseFor(Row row, Context context) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_LOOP;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        String variable = cells.name(error, "the loop variable");
        Block typeBlock = cells.next();
        Type.Simple type;
        if (typeBlock instanceof KeywordBlock(Keyword keyword)
                && (keyword == Keyword.INTEGER || keyword == Keyword.FLOAT)) {
            type = keyword.simpleType().orElseThrow();
        } else {
            throw C0deException.at(row.z(), error, "The loop variable must be an INTEGER or a FLOAT");
        }
        ExpressionReader reader = new ExpressionReader(cells, row.x() + 1);
        Expression from = reader.read(error, "the start value");
        Expression to = reader.read(error, "the end value");
        Expression step = reader.hasMore() ? reader.read(error, "the increment") : null;
        reader.end(error);
        BlockAndEnd body = parseCodeBlock(row.x(), context.inLoop(), error, "A FOR line");
        expectEnd(body.end(), Keyword.FOR, ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END);
        return new Statement.For(row.z(), variable, type, from, to, step, body.block());
    }

    private Statement parseEach(Row row, Context context) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_LOOP;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        String variable = cells.name(error, "the loop variable");
        LabelRef iterable = cells.reference(error, "the array or hash");
        cells.end(error);
        BlockAndEnd body = parseCodeBlock(row.x(), context.inLoop(), error, "An EACH line");
        expectEnd(body.end(), Keyword.EACH, ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END);
        return new Statement.Each(row.z(), variable, iterable, body.block());
    }

    private Statement parseWhile(Row row, Context context) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_LOOP;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        ExpressionReader reader = new ExpressionReader(cells, row.x() + 1);
        Expression condition = reader.read(error, "the condition");
        reader.end(error);
        BlockAndEnd body = parseCodeBlock(row.x(), context.inLoop(), error, "A WHILE line");
        expectEnd(body.end(), Keyword.WHILE, ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END);
        return new Statement.While(row.z(), condition, body.block());
    }

    private Statement parseRepeat(Row row, Context context) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_LOOP;
        position++;
        if (!row.isOnly(Keyword.REPEAT)) {
            throw C0deException.at(row.z(), error, "A REPEAT line must not contain other blocks");
        }
        BlockAndEnd body = parseCodeBlock(row.x(), context.inLoop(), error, "A REPEAT line");
        Cells end = new Cells(body.end());
        end.next();
        if (!(end.next() instanceof KeywordBlock(Keyword k)) || k != Keyword.REPEAT) {
            throw C0deException.at(
                    body.end().z(),
                    ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END,
                    "The END line must be followed by REPEAT");
        }
        ExpressionReader reader = new ExpressionReader(end, row.x() + 1);
        Expression condition = reader.read(error, "the condition");
        reader.end(error);
        return new Statement.Repeat(row.z(), body.block(), condition);
    }

    private Statement parseBreak(Row row, Context context) {
        position++;
        if (context.loops() == 0) {
            throw C0deException.at(row.z(), ErrorCode.SYNTAX_ERROR_PLACEMENT, "BREAK must be placed inside a loop");
        }
        Cells cells = new Cells(row);
        cells.next();
        Expression condition = null;
        if (cells.peek() != null) {
            ExpressionReader reader = new ExpressionReader(cells, row.x() + 1);
            condition = reader.read(ErrorCode.SYNTAX_ERROR_EXPRESSION, "the condition");
            reader.end(ErrorCode.SYNTAX_ERROR_EXPRESSION);
        }
        return new Statement.Break(row.z(), condition);
    }

    private Statement parseReturn(Row row) {
        ErrorCode error = ErrorCode.SYNTAX_ERROR_FUNCTION;
        position++;
        Cells cells = new Cells(row);
        cells.next();
        LabelRef errorLabel = cells.peek() == null ? null : cells.reference(error, "the error variable");
        cells.end(error);
        return new Statement.Return(row.z(), errorLabel);
    }

    private Statement parseExit(Row row) {
        position++;
        Cells cells = new Cells(row);
        cells.next();
        if (cells.peek() == null) {
            throw C0deException.at(row.z(), ErrorCode.SYNTAX_ERROR_EXIT, "EXIT must be followed by the exit code");
        }
        ExpressionReader reader = new ExpressionReader(cells, row.x() + 1);
        Expression code = reader.read(ErrorCode.SYNTAX_ERROR_EXIT, "the exit code");
        reader.end(ErrorCode.SYNTAX_ERROR_EXIT);
        return new Statement.Exit(row.z(), code);
    }

    // ------------------------------------------------------------------------------------- types and tags

    private TypeSpec parseType(Cells cells, ErrorCode error, int dimensions) {
        Block block = cells.next();
        if (!(block instanceof KeywordBlock(Keyword keyword)) || !keyword.isType()) {
            throw C0deException.at(cells.z(), error, "A type is missing");
        }
        return switch (keyword) {
            case BOOLEAN, INTEGER, FLOAT, CHAR, TIME ->
                new TypeSpec.Simple(keyword.simpleType().orElseThrow());
            case STRING -> {
                if (cells.peek() instanceof LiteralBlock(Value value) && value instanceof IntegerValue limit) {
                    cells.next();
                    if (limit.value() < 0) {
                        throw C0deException.at(cells.z(), error, "The length limit of a STRING must not be negative");
                    }
                    yield new TypeSpec.Str(limit.value());
                }
                yield new TypeSpec.Str(null);
            }
            case ARRAY, HASH -> {
                if (dimensions > MAX_DEPTH) {
                    throw C0deException.at(
                            cells.z(), error, "Arrays and hashes can have at most " + MAX_DEPTH + " dimensions");
                }
                if (cells.peek() == null) {
                    throw C0deException.at(cells.z(), error, keyword + " must be followed by the type of its elements");
                }
                TypeSpec element = parseType(cells, error, dimensions + 1);
                yield keyword == Keyword.ARRAY ? new TypeSpec.Array(element) : new TypeSpec.Hash(element);
            }
            case STRUCT -> new TypeSpec.Struct(cells.qualifiedLabel(error, "the name of the struct"));
            case ERROR -> new TypeSpec.Error();
            default -> throw C0deException.at(cells.z(), error, keyword + " is not a type");
        };
    }

    /**
     * Reads TAG blocks. Tags of variables have paths (keys separated by dots), tags of struct elements a
     * single key name.
     */
    private static Map<String, String> parseTags(Cells cells, ErrorCode error, boolean paths) {
        Map<String, String> tags = new LinkedHashMap<>();
        while (cells.peek() instanceof TagBlock tag) {
            cells.next();
            String language = tag.language();
            String name = tag.name();
            if (language == null || !(language.equals("json") || language.equals("yaml"))) {
                throw C0deException.at(cells.z(), error, "The language of a TAG must be json or yaml");
            }
            if (name == null || name.isEmpty()) {
                throw C0deException.at(cells.z(), error, "A TAG must have a name");
            }
            if (paths ? List.of(name.split("\\.", -1)).contains("") : name.contains(".")) {
                throw C0deException.at(cells.z(), error, "Invalid name of a TAG: " + name);
            }
            if (tags.put(language, name) != null) {
                throw C0deException.at(cells.z(), error, "There is more than one TAG for " + language);
            }
        }
        return tags;
    }

    private static C0deException indentation(Row row, int expected) {
        return C0deException.at(
                row.z(),
                ErrorCode.SYNTAX_ERROR_INDENTATION,
                "The line starts at x=" + row.x() + " instead of x=" + expected);
    }

    private static Statement.Invalid invalid(int z, ErrorCode code, String message) {
        return new Statement.Invalid(z, C0deException.at(z, code, message));
    }

    // ------------------------------------------------------------------------------------ reading blocks

    /** Reads the blocks of a line one after another. Blocks must follow each other without gaps. */
    private static final class Cells {
        private final Row row;
        private int index;

        Cells(Row row) {
            this.row = row;
        }

        int z() {
            return row.z();
        }

        /** The next block, or null at the end of the line or before a gap. */
        Block peek() {
            if (index >= row.cells().size()) {
                return null;
            }
            if (index > 0
                    && row.cells().get(index).x() != row.cells().get(index - 1).x() + 1) {
                return null;
            }
            return row.cells().get(index).block();
        }

        Block next() {
            Block block = peek();
            if (block != null) {
                index++;
            }
            return block;
        }

        boolean atEnd() {
            return index >= row.cells().size();
        }

        /** Ensures that no more blocks follow. */
        void end(ErrorCode error) {
            if (!atEnd()) {
                throw C0deException.at(
                        row.z(),
                        error,
                        "Unexpected block at x=" + row.cells().get(index).x());
            }
        }

        private LabelBlock labelBlock(ErrorCode error, String what) {
            if (!(next() instanceof LabelBlock label)) {
                throw C0deException.at(row.z(), error, "A LABEL with " + what + " is missing");
            }
            if (label.name() == null) {
                throw C0deException.at(row.z(), error, "The LABEL with " + what + " has no name");
            }
            return label;
        }

        /** A label without index whose name is valid. */
        String name(ErrorCode error, String what) {
            LabelBlock label = labelBlock(error, what);
            if (!label.index().isEmpty() || !Names.isValid(label.name())) {
                throw C0deException.at(row.z(), error, "Invalid name: " + label.name());
            }
            return label.name();
        }

        /** A label without index whose name may be qualified, e.g. {@code c0de/strings/MID}. */
        String qualifiedLabel(ErrorCode error, String what) {
            LabelBlock label = labelBlock(error, what);
            if (!label.index().isEmpty() || !Names.isValidQualified(label.name())) {
                throw C0deException.at(row.z(), error, "Invalid name: " + label.name());
            }
            return label.name();
        }

        /** A label without index, any name (e.g. a hash key). */
        String label(ErrorCode error, String what) {
            LabelBlock label = labelBlock(error, what);
            if (!label.index().isEmpty()) {
                throw C0deException.at(row.z(), error, "The LABEL with " + what + " must not have an index");
            }
            return label.name();
        }

        /** A label referring to a variable or constant, optionally with an index. */
        LabelRef reference(ErrorCode error, String what) {
            LabelBlock label = labelBlock(error, what);
            if (!Names.isValidQualified(label.name())) {
                throw C0deException.at(row.z(), error, "Invalid name: " + label.name());
            }
            return new LabelRef(label.name(), label.index());
        }
    }

    /**
     * Reads expressions that end with {@code ;}. When a line ends before the {@code ;}, the expression
     * continues on the next continuation line, which starts at the given x coordinate.
     */
    private final class ExpressionReader {
        private Cells cells;
        private final int continuationX;
        private int lastLine;

        ExpressionReader(Cells cells, int continuationX) {
            this.cells = cells;
            this.continuationX = continuationX;
            this.lastLine = cells.z();
        }

        int lastLine() {
            return lastLine;
        }

        /** True if more blocks follow on the current line. */
        boolean hasMore() {
            return cells.peek() != null;
        }

        Expression read(ErrorCode missing, String what) {
            int line = cells.z();
            if (cells.peek() == null && cells.atEnd() && !continuationFollows()) {
                throw C0deException.at(line, missing, "The expression for " + what + " is missing");
            }
            List<Block> tokens = new ArrayList<>();
            while (true) {
                Block block = cells.next();
                if (block == null) {
                    if (!cells.atEnd()) {
                        throw C0deException.at(cells.z(), ErrorCode.SYNTAX_ERROR_EXPRESSION, "A gap in an expression");
                    }
                    if (!continuationFollows()) {
                        throw C0deException.at(
                                cells.z(), ErrorCode.SYNTAX_ERROR_EXPRESSION, "The expression doesn't end with ;");
                    }
                    Row next = current();
                    position++;
                    cells = new Cells(next);
                    lastLine = next.z();
                    continue;
                }
                if (block instanceof OperatorBlock(Operator operator) && operator == Operator.END_OF_EXPRESSION) {
                    break;
                }
                tokens.add(block);
            }
            return ExpressionParser.parse(tokens, line);
        }

        private boolean continuationFollows() {
            Row next = current();
            return next != null
                    && next.noopError() == null
                    && next.x() == continuationX
                    && !(next.first() instanceof KeywordBlock);
        }

        void end(ErrorCode error) {
            cells.end(error);
        }
    }
}
