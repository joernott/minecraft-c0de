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

import com.google.common.collect.ImmutableList;
import de.joernott.c0de.client.handbook.md.compiler.MarkdownCompiler;
import de.joernott.c0de.client.handbook.md.compiler.TextCompiler;
import de.joernott.c0de.client.handbook.md.feature.*;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * A Markdown-processor models the pipeline required to lex, parse and compile
 * some Markdown input into a result of type {@code R}. For this purpose, it employs
 * a set of {@link MarkdownFeature}s installed into a {@link Lexer} and {@link Parser},
 * the resulting AST of which it compiles using a {@link MarkdownCompiler}.
 * <p>
 * To create a processor, either use one of the default factories and optionally customize
 * them using the provided copyWith functions, or invoke the constructor and supply
 * the desired compiler factory and feature-set
 */
public class MarkdownProcessor<R> {

    private final Supplier<MarkdownCompiler<R>> compilerFactory;

    private final List<MarkdownFeature> features;

    private final Lexer lexer;
    private final Parser parser;

    public MarkdownProcessor(Supplier<MarkdownCompiler<R>> compilerFactory, MarkdownFeature... features) {
        this(compilerFactory, Arrays.asList(features));
    }

    public MarkdownProcessor(Supplier<MarkdownCompiler<R>> compilerFactory, List<MarkdownFeature> features) {
        this.compilerFactory = compilerFactory;
        this.features = ImmutableList.copyOf(features);

        var testCompiler = this.compilerFactory.get();
        for (var feature : this.features) {
            if (!feature.supportsCompiler(testCompiler)) {
                throw new IllegalStateException("Feature '" + feature.name() + "' is incompatible with compiler '" + testCompiler.name() + "'");
            }
        }

        this.lexer = new Lexer();
        this.parser = new Parser();

        for (var extension : features) {
            extension.registerTokens(this.lexer);
            extension.registerNodes(this.parser);
        }
    }

    public Collection<MarkdownFeature> installedFeatures() {
        return this.features;
    }

    public boolean hasFeature(Class<?> featureClass) {
        for (var extension : this.features) {
            if (featureClass.isInstance(extension)) {
                return true;
            }
        }

        return false;
    }

    public R process(String markdown) {
        var compiler = this.compilerFactory.get();

        this.parser.parse(this.lexer.lex(markdown)).visit(compiler);
        return compiler.compile();
    }

    // --- copy constructors ---

    /**
     * Create a copy of this processor with {@code features} added
     * to the copy's feature-set
     */
    public MarkdownProcessor<R> copyWith(MarkdownFeature... features) {
        var newFeatures = new ArrayList<>(this.features);
        for (var feature : features) {
            if (this.hasFeature(feature.getClass())) continue;
            newFeatures.add(feature);
        }

        return new MarkdownProcessor<>(this.compilerFactory, newFeatures);
    }

    /**
     * Create a copy of this processor with its compiler factory
     * replaced by {@code compilerFactory}
     */
    public <R2> MarkdownProcessor<R2> copyWith(Supplier<MarkdownCompiler<R2>> compilerFactory) {
        return new MarkdownProcessor<>(compilerFactory, this.features);
    }

    // --- default factories ---

    /**
     * Create a new Markdown-processor with support for basic text formatting, that is:
     * <ul>
     *     <li>Bold & Italic Emphasis</li>
     *     <li>Discord-like underscore and strikethrough formatting</li>
     *     <li>Colors using <pre>{&lt;color name&gt;|#RRGGBB}content here{}</pre> syntax</li>
     * </ul>
     */
    public static MarkdownProcessor<Component> text() {
        return new MarkdownProcessor<>(TextCompiler::new, new BasicFormattingFeature(false), new ColorFeature());
    }

    /**
     * Create a new Markdown-processor with support for rich text formatting, that is:
     * <ul>
     *     <li>Bold & Italic Emphasis</li>
     *     <li>Discord-like underscore and strikethrough formatting</li>
     *     <li>Horizontal Rules</li>
     *     <li>Colors using <pre>{&lt;color name&gt;|#RRGGBB}content here{}</pre> syntax</li>
     *     <li>Hyperlinks</li>
     *     <li>Ordered & unordered lists</li>
     *     <li>Block quotes</li>
     * </ul>
     */
    public static MarkdownProcessor<Component> richText(int assumedOutputWidth) {
        return new MarkdownProcessor<>(() -> new TextCompiler(assumedOutputWidth), new BasicFormattingFeature(), new ColorFeature(), new LinkFeature(), new ListFeature(), new BlockQuoteFeature());
    }
}
