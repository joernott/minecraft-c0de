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
package de.joernott.c0de.client.handbook.md.compiler;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalInt;
import java.util.function.UnaryOperator;

/**
 * All functions defined on this interface are required for supporting
 * every default Markdown feature implemented in lavender-md. A compiler
 * accepts these commands while visiting an AST and compiles into a result
 * type specific to each compiler implementation.
 * <p>
 * Compilers are stateful objects which build their result object internally
 * while visiting the AST and must be re-created for each new result to be
 * generated
 *
 * @param <R> The result type of this specific compiler implementation
 * @see de.joernott.c0de.client.handbook.md.util.TextBuilder
 */
public interface MarkdownCompiler<R> {

    /**
     * Append {@code text} to this compiler's result, using
     * the current style as dictated by {@link #visitStyle(UnaryOperator)}
     * and {@link #visitStyleEnd()}
     */
    void visitText(String text);

    /**
     * Push {@code style} onto this compiler's stack
     */
    void visitStyle(UnaryOperator<Style> style);

    /**
     * Pop the current style from this compiler's stack
     */
    void visitStyleEnd();

    /**
     * Begin a new level of block quote
     */
    void visitBlockQuote();

    /**
     * End the current level of block quote
     */
    void visitBlockQuoteEnd();

    /**
     * Append a horizontal rule to this compiler's result
     */
    void visitHorizontalRule();

    /**
     * Append {@code image} into this compiler's result, using
     * {@code description} as alt-text, stretching the image to the
     * width of the compiler's result if {@code fit} is {@code true}
     */
    void visitImage(ResourceLocation image, String description, boolean fit);

    /**
     * Begin a new list item, potentially nesting inside a
     * previous one
     */
    void visitListItem(OptionalInt ordinal);

    /**
     * End the current list item
     */
    void visitListItemEnd();

    /**
     * @return This compiler's result - the state of this compiler
     * and the result of calling this function again is not defined
     */
    R compile();

    /**
     * @return A name for this compiler, to be used in logging messages
     */
    String name();
}
