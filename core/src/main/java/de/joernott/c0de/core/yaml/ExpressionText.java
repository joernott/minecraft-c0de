package de.joernott.c0de.core.yaml;

import de.joernott.c0de.core.line.IndexText;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.line.Operator;
import de.joernott.c0de.core.program.Expression;
import de.joernott.c0de.core.program.LabelRef;
import java.util.ArrayList;
import java.util.List;

/**
 * Expressions as text in the YAML format (docs/concept/17-yaml-format.md): tokens separated by spaces, without
 * the final {@code ;}. Parentheses are only written where the precedence of the operators requires them.
 */
final class ExpressionText {
    private ExpressionText() {}

    /** The text of an expression. */
    static String format(Expression expression) {
        List<String> tokens = new ArrayList<>();
        tokens(expression, tokens);
        return String.join(" ", tokens);
    }

    /** The text of a label with its index, e.g. {@code values[i]}. */
    static String label(LabelRef label) {
        return label.name() + IndexText.format(label.index());
    }

    private static void tokens(Expression expression, List<String> tokens) {
        switch (expression) {
            case Expression.Literal literal -> tokens.add(Notation.formatLiteral(literal.value()));
            case Expression.Reference reference -> tokens.add(label(reference.label()));
            case Expression.Unary unary -> {
                tokens.add(unary.operator().symbol());
                operand(unary.operand(), unary.operator().precedence(), false, tokens);
            }
            case Expression.Binary binary -> {
                Operator operator = binary.operator();
                boolean rightAssociative = operator.arity() == Operator.Arity.BINARY_RIGHT;
                operand(binary.left(), operator.precedence(), rightAssociative, tokens);
                tokens.add(operator.symbol());
                operand(binary.right(), operator.precedence(), !rightAssociative, tokens);
            }
        }
    }

    /**
     * An operand, in parentheses if its operator binds less tightly than the operator it belongs to, or equally
     * tightly on the side where the evaluation order would change.
     */
    private static void operand(Expression operand, int precedence, boolean parenthesesOnEqual, List<String> tokens) {
        int own = switch (operand) {
            case Expression.Binary binary -> binary.operator().precedence();
            case Expression.Unary unary -> unary.operator().precedence();
            default -> 0;
        };
        boolean parentheses = own > precedence || (own == precedence && parenthesesOnEqual && own != 0);
        if (parentheses) {
            tokens.add("(");
        }
        tokens(operand, tokens);
        if (parentheses) {
            tokens.add(")");
        }
    }

    /** Splits the text of an expression into its tokens; quoted strings and characters are one token. */
    static List<String> split(String text) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        char quote = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quote != 0) {
                token.append(c);
                if (c == '\\' && i + 1 < text.length()) {
                    token.append(text.charAt(++i));
                } else if (c == quote) {
                    quote = 0;
                }
            } else if (c == '"' || c == '\'') {
                quote = c;
                token.append(c);
            } else if (Character.isWhitespace(c)) {
                if (!token.isEmpty()) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(c);
            }
        }
        if (quote != 0) {
            throw new IllegalArgumentException("Missing closing quote: " + text);
        }
        if (!token.isEmpty()) {
            tokens.add(token.toString());
        }
        return tokens;
    }
}
