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

import de.joernott.c0de.client.handbook.md.compiler.MarkdownCompiler;
import de.joernott.c0de.client.handbook.md.util.ListNibbler;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * A Markdown feature implements a set of tokens and AST nodes
 * for parsing & generating certain Markdown syntax
 */
public interface MarkdownFeature {

    /**
     * @return A name for this feature, to be used in logging messages
     */
    String name();

    /**
     * Return {@code true} if this feature knows how to invoke {@code compiler}.
     * <p>
     * All nodes created by this feature must assume that the compiler they are
     * invoked on has passed the check in this method. Notably - if this method does
     * a type-check, all compilers used on this feature's nodes are guaranteed
     * to be of said type
     */
    boolean supportsCompiler(MarkdownCompiler<?> compiler);

    /**
     * Add this feature's set of tokens to {@code registrar}
     */
    void registerTokens(TokenRegistrar registrar);

    /**
     * Add this feature's set of nodes to {@code registrar}
     */
    void registerNodes(NodeRegistrar registrar);

    @FunctionalInterface
    interface TokenRegistrar {
        /**
         * Register {@code lexer} to be invoked when {@code trigger}
         * is encountered while lexing the Markdown input
         */
        void registerToken(Lexer.LexFunction lexer, char trigger);

        /**
         * Register {@code lexer} to be invoked when any of
         * {@code triggers} is encountered while lexing the Markdown input
         */
        default void registerToken(Lexer.LexFunction lexer, char... triggers) {
            for (char trigger : triggers) {
                this.registerToken(lexer, trigger);
            }
        }
    }

    @FunctionalInterface
    interface NodeRegistrar {
        /**
         * Register {@code parser} to be invoked when {@code trigger} matches and
         * returns a token of type {@code T} from the incoming stream
         */
        <T extends Lexer.Token> void registerNode(Parser.ParseFunction<T> parser, BiFunction<Lexer.Token, ListNibbler<Lexer.Token>, @Nullable T> trigger);
    }

}
