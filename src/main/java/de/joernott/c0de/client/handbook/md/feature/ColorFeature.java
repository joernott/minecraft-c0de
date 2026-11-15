/*
 * Taken from lavender-md (https://github.com/wisp-forest/lavender-md, branch 26.1) for the handbooks of c0de
 * (docs/concept/21-handbooks.md). Changed for c0de: the colour names come from the names of the constants of
 * ChatFormatting, whose methods for them don't exist since Minecraft 26.2.
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

import com.google.common.collect.ImmutableMap;
import de.joernott.c0de.client.handbook.md.Lexer;
import de.joernott.c0de.client.handbook.md.MarkdownFeature;
import de.joernott.c0de.client.handbook.md.Parser;
import de.joernott.c0de.client.handbook.md.compiler.MarkdownCompiler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ColorFeature implements MarkdownFeature {

    private static final Map<String, ChatFormatting> FORMATTING_COLORS = Stream.of(ChatFormatting.values())
            // The 16 colours are the first constants; isColor() and getName() don't exist since Minecraft 26.2
            .filter(formatting -> formatting.ordinal() < 16)
            .collect(ImmutableMap.toImmutableMap(formatting -> formatting.name().toLowerCase(Locale.ROOT), Function.identity()));

    @Override
    public String name() {
        return "colors";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken((nibbler, tokens) -> {
            nibbler.skip();
            if (!nibbler.hasNext()) return false;

            if (nibbler.peek() == '}') {
                nibbler.skip();
                tokens.add(new CloseColorToken());
            } else {
                if (nibbler.peek() == '#') {
                    nibbler.skip();

                    var color = nibbler.consumeUntil('}');
                    if (color == null) return false;

                    if (!color.matches("[0-9a-fA-F]{6}")) return false;
                    tokens.add(new OpenColorToken("{#" + color + "}", style -> style.withColor(Integer.parseInt(color, 16))));
                } else {
                    var color = nibbler.consumeUntil('}');
                    if (color == null) return false;

                    if (!FORMATTING_COLORS.containsKey(color)) return false;
                    tokens.add(new OpenColorToken("{" + color + "}", style -> style.withColor(FORMATTING_COLORS.get(color))));
                }
            }

            return true;
        }, '{');

    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode((parser, left, tokens) -> {
            int pointer = tokens.pointer();
            var content = parser.parseUntil(tokens, CloseColorToken.class);

            if (tokens.peek() instanceof CloseColorToken) {
                tokens.nibble();
                return new Parser.FormattingNode(left.style).addChild(content);
            } else {
                tokens.setPointer(pointer);
                return new Parser.TextNode(left.content());
            }
        }, (token, tokens) -> token instanceof OpenColorToken color ? color : null);
    }

    // --- tokens ---

    private static final class OpenColorToken extends Lexer.Token {

        public final @NotNull UnaryOperator<Style> style;

        public OpenColorToken(String content, @NotNull UnaryOperator<Style> style) {
            super(content);
            this.style = style;
        }
    }

    private static final class CloseColorToken extends Lexer.Token {
        private CloseColorToken() {
            super("{}");
        }
    }
}
