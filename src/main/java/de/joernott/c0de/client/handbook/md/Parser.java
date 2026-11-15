/*
 * Taken from lavender-md (https://github.com/wisp-forest/lavender-md, branch 26.1) for the handbooks of c0de
 * (docs/concept/21-handbooks.md).
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2023
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.joernott.c0de.client.handbook.md;

import de.joernott.c0de.client.handbook.md.Lexer.NewlineToken;
import de.joernott.c0de.client.handbook.md.Lexer.TextToken;
import de.joernott.c0de.client.handbook.md.Lexer.Token;
import de.joernott.c0de.client.handbook.md.compiler.MarkdownCompiler;
import de.joernott.c0de.client.handbook.md.util.ListNibbler;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Parser implements MarkdownFeature.NodeRegistrar {

    private final Map<BiFunction<Token, ListNibbler<Token>, ?>, ParseFunction<?>> parseFunctions = new HashMap<>();

    public Parser() {
        this.registerNode((parser, text, tokens) -> {
            var content = text.content();
            if (tokens.peek(-2) == null || tokens.peek(-2) instanceof NewlineToken) {
                content = content.stripLeading();
            }

            if (tokens.peek() instanceof NewlineToken newline && !newline.isBoundary()) {
                content = content.stripTrailing();
            }

            return new TextNode(content);
        }, (token, tokens) -> token instanceof TextToken text ? text : null);
    }

    @Override
    public <T extends Token> void registerNode(ParseFunction<T> parser, BiFunction<Token, ListNibbler<Token>, @Nullable T> trigger) {
        this.parseFunctions.put(trigger, parser);
    }

    /**
     * A parse-function is responsible for consuming a sequence of tokens
     * from {@code tokens} and parsing child nodes using {@code parser}
     * to generate the node indicated by the matched {@code trigger} token
     *
     * @param <T> The type of token this parse-function consumes
     */
    @FunctionalInterface
    public interface ParseFunction<T extends Token> {
        Node parse(Parser parser, T trigger, ListNibbler<Token> tokens);
    }

    public Node parse(List<Token> tokens) {
        var tokenNibbler = new ListNibbler<>(tokens);

        var node = Node.empty();
        while (tokenNibbler.hasElements()) {
            node.addChild(parseNode(tokenNibbler));
        }

        return node;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private @NotNull Node parseNode(ListNibbler<Token> tokens) {
        var token = tokens.nibble();

        for (var function : this.parseFunctions.entrySet()) {
            var first = function.getKey().apply(token, tokens);
            if (first == null) continue;

            return ((ParseFunction) function.getValue()).parse(this, token, tokens);
        }

        if (token != null) {
            return new TextNode(token.content());
        }

        return Node.empty();
    }

    public Node parseUntil(ListNibbler<Token> tokens, Class<? extends Token> until) {
        return this.parseUntil(tokens, token -> token.isBoundary() || until.isInstance(token), token -> false);
    }

    public Node parseUntil(ListNibbler<Token> tokens, Predicate<Token> until, Predicate<Token> skip) {
        var node = this.parseNode(tokens);
        var sequence = false;

        while (tokens.hasElements()) {
            var next = tokens.peek();

            if (skip.test(next)) {
                tokens.nibble();
                continue;
            }

            if (until.test(next)) break;

            if (!sequence) {
                node = new SequenceNode().addChild(node);
                sequence = true;
            }
            node.addChild(this.parseNode(tokens));
        }

        return node;
    }

    public abstract static class Node {

        protected final List<Node> children = new ArrayList<>();

        public Node addChild(Node child) {
            this.children.add(child);
            return this;
        }

        public void visit(MarkdownCompiler<?> compiler) {
            this.visitStart(compiler);
            for (var child : this.children) {
                child.visit(compiler);
            }
            this.visitEnd(compiler);
        }

        protected abstract void visitStart(MarkdownCompiler<?> compiler);

        protected abstract void visitEnd(MarkdownCompiler<?> compiler);

        public static Node empty() {
            return new Node() {
                @Override
                protected void visitStart(MarkdownCompiler<?> compiler) {
                }

                @Override
                protected void visitEnd(MarkdownCompiler<?> compiler) {
                }
            };
        }
    }

    public static final class SequenceNode extends Node {
        @Override
        protected void visitStart(MarkdownCompiler<?> compiler) {}
        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {}
    }

    public static final class TextNode extends Node {
        private final String content;

        public TextNode(String content) {
            this.content = content;
        }

        @Override
        public void visitStart(MarkdownCompiler<?> compiler) {
            compiler.visitText(this.content);
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {}
    }

    public static class FormattingNode extends Node {
        private final UnaryOperator<Style> formatting;

        public FormattingNode(UnaryOperator<Style> formatting) {
            this.formatting = formatting;
        }

        @Override
        public void visitStart(MarkdownCompiler<?> compiler) {
            compiler.visitStyle(this::applyStyle);
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {
            compiler.visitStyleEnd();
        }

        protected Style applyStyle(Style style) {
            return this.formatting.apply(style);
        }
    }

}
