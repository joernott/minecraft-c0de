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
package de.joernott.c0de.client.handbook.md.feature;

import de.joernott.c0de.client.handbook.md.Lexer;
import de.joernott.c0de.client.handbook.md.MarkdownFeature;
import de.joernott.c0de.client.handbook.md.Parser;
import de.joernott.c0de.client.handbook.md.compiler.MarkdownCompiler;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public class BasicFormattingFeature implements MarkdownFeature {

    private final boolean enableHorizontalRule;

    public BasicFormattingFeature(boolean enableHorizontalRule) {
        this.enableHorizontalRule = enableHorizontalRule;
    }

    public BasicFormattingFeature() {
        this(true);
    }

    @Override
    public String name() {
        return "basic_formatting";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken(Lexer.Token.lexFromChar(TildeToken::new), '~');
        registrar.registerToken(Lexer.Token.lexFromChar(UnderscoreToken::new), '_');

        registrar.registerToken((nibbler, tokens) -> {
            int starCount = nibbler.consumeUntilEndOr(c -> c != '*').length();

            boolean leftAdjacent = !nibbler.expect(-starCount - 1, ' ');
            boolean rightAdjacent = !nibbler.expect(0, ' ');

            if (starCount > 3 || !(rightAdjacent || leftAdjacent)) {
                return false;
            }

            for (int i = 0; i < starCount; i++) {
                tokens.add(new StarToken("*", leftAdjacent, rightAdjacent));
            }
            return true;
        }, '*');

        if (this.enableHorizontalRule) {
            registrar.registerToken((nibbler, tokens) -> {
                if (!nibbler.expect(-1, '\n') || !nibbler.expect(-2, '\n')) return false;

                var dashes = nibbler.consumeUntilEndOr(c -> c != '-');
                if (dashes.length() != 3 || !nibbler.expect(0, '\n') || !nibbler.expect(1, '\n')) {
                    return false;
                }

                tokens.add(new HorizontalRuleToken());
                return true;
            }, '-');
        }
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        this.registerDoubleTokenFormatting(registrar, TildeToken.class, style -> style.withStrikethrough(true));
        this.registerDoubleTokenFormatting(registrar, UnderscoreToken.class, style -> style.withUnderlined(true));

        registrar.registerNode((parser, left, tokens) -> {
            int pointer = tokens.pointer();
            var content = parser.parseUntil(tokens, StarToken.class);

            if (tokens.peek() instanceof StarToken right && right.leftAdjacent) {
                tokens.nibble();

                if (content instanceof StarNode star) {
                    if (star.canIncrementStarCount()) {
                        return star.incrementStarCount();
                    } else {
                        return new Parser.TextNode("*").addChild(content).addChild(new Parser.TextNode("*"));
                    }
                } else {
                    return new StarNode().addChild(content);
                }
            } else {
                tokens.setPointer(pointer);
                return new Parser.TextNode(left.content());
            }
        }, (token, tokens) -> token instanceof StarToken star && star.rightAdjacent ? star : null);

        if (this.enableHorizontalRule) {
            registrar.registerNode(
                    (parser, rule, tokens) -> new HorizontalRuleNode(),
                    (token, tokens) -> token instanceof HorizontalRuleToken rule ? rule : null
            );
        }
    }

    private <T extends Lexer.Token> void registerDoubleTokenFormatting(NodeRegistrar registrar, Class<T> tokenClass, UnaryOperator<Style> formatting) {
        registrar.registerNode((parser, left1, tokens) -> {
            var left2 = tokens.nibble();

            int pointer = tokens.pointer();
            var content = parser.parseUntil(tokens, tokenClass);

            if (tokenClass.isInstance(tokens.peek()) && tokenClass.isInstance(tokens.peek(1))) {
                tokens.skip(2);
                return new Parser.FormattingNode(formatting).addChild(content);
            } else {
                tokens.setPointer(pointer);
                return new Parser.TextNode(left1.content() + left2.content());
            }
        }, (token, tokens) -> tokenClass.isInstance(token) && tokenClass.isInstance(tokens.peek()) ? tokenClass.cast(token) : null);
    }

    // --- tokens ---

    private static final class StarToken extends Lexer.Token {

        public final boolean leftAdjacent, rightAdjacent;

        public StarToken(String content, boolean leftAdjacent, boolean rightAdjacent) {
            super(content);
            this.leftAdjacent = leftAdjacent;
            this.rightAdjacent = rightAdjacent;
        }
    }

    private static final class TildeToken extends Lexer.Token {
        public TildeToken() {
            super("~");
        }
    }

    private static final class UnderscoreToken extends Lexer.Token {
        public UnderscoreToken() {
            super("_");
        }
    }

    private static final class HorizontalRuleToken extends Lexer.Token {
        public HorizontalRuleToken() {
            super("---");
        }
    }

    // --- nodes ---

    private static class StarNode extends Parser.FormattingNode {

        private int starCount = 1;

        public StarNode() {
            super(style -> style);
        }

        @Override
        protected Style applyStyle(Style style) {
            return style.withItalic(this.starCount % 2 == 1 ? true : null).withBold(this.starCount > 1 ? true : null);
        }

        public StarNode incrementStarCount() {
            this.starCount++;
            return this;
        }

        public boolean canIncrementStarCount() {
            return this.starCount < 3;
        }
    }

    private static class HorizontalRuleNode extends Parser.Node {
        @Override
        protected void visitStart(MarkdownCompiler<?> compiler) {
            compiler.visitHorizontalRule();
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {}
    }
}
