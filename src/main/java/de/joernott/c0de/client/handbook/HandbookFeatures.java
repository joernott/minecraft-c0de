package de.joernott.c0de.client.handbook;

import de.joernott.c0de.client.handbook.md.Lexer;
import de.joernott.c0de.client.handbook.md.MarkdownFeature;
import de.joernott.c0de.client.handbook.md.Parser;
import de.joernott.c0de.client.handbook.md.compiler.MarkdownCompiler;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;

/**
 * The Markdown of c0de's handbooks in addition to lavender-md (docs/concept/21-handbooks.md): code, program
 * examples, recipes, items and page breaks. They only work with the {@link PageCompiler}.
 */
public final class HandbookFeatures {
    private HandbookFeatures() {}

    /** All features of c0de. */
    public static List<MarkdownFeature> all() {
        return List.of(new Code(), new Tag("recipe", PageCompiler::visitRecipe), new Tag("item", PageCompiler::visitItem),
                new PageBreak());
    }

    /** {@code `code`} (dark blue) and program examples between lines of {@code ```} (every line as written). */
    static final class Code implements MarkdownFeature {
        @Override
        public String name() {
            return "c0de code";
        }

        @Override
        public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
            return compiler instanceof PageCompiler;
        }

        @Override
        public void registerTokens(TokenRegistrar registrar) {
            registrar.registerToken((nibbler, tokens) -> {
                if (!nibbler.tryConsume("```")) {
                    return false;
                }
                // The rest of the first line names the language
                if (nibbler.consumeUntil('\n') == null) {
                    return false;
                }
                List<String> lines = new ArrayList<>();
                StringBuilder line = new StringBuilder();
                while (nibbler.hasNext()) {
                    char next = nibbler.next();
                    if (next != '\n') {
                        line.append(next);
                        if (!nibbler.hasNext() && line.toString().strip().equals("```")) {
                            tokens.add(new BlockToken(lines));
                            return true;
                        }
                        continue;
                    }
                    if (line.toString().strip().equals("```")) {
                        tokens.add(new BlockToken(lines));
                        return true;
                    }
                    lines.add(line.toString().stripTrailing());
                    line.setLength(0);
                }
                return false;
            }, '`');
            registrar.registerToken((nibbler, tokens) -> {
                nibbler.skip();
                String code = nibbler.consumeUntil('`');
                if (code == null || code.isEmpty() || code.contains("\n")) {
                    return false;
                }
                tokens.add(new InlineToken(code));
                return true;
            }, '`');
        }

        @Override
        public void registerNodes(NodeRegistrar registrar) {
            registrar.registerNode(
                    (parser, token, tokens) -> new Parser.FormattingNode(
                                    style -> style.withColor(ChatFormatting.DARK_BLUE))
                            .addChild(new Parser.TextNode(token.code)),
                    (token, tokens) -> token instanceof InlineToken code ? code : null);
            registrar.registerNode(
                    (parser, token, tokens) -> new Visit(compiler -> compiler.visitCode(token.lines)),
                    (token, tokens) -> token instanceof BlockToken block ? block : null);
        }

        private static final class InlineToken extends Lexer.Token {
            final String code;

            InlineToken(String code) {
                super("`" + code + "`");
                this.code = code;
            }
        }

        private static final class BlockToken extends Lexer.Token {
            final List<String> lines;

            BlockToken(List<String> lines) {
                super(String.join("\n", lines));
                this.lines = List.copyOf(lines);
            }
        }
    }

    /** {@code <name;id>}: a recipe or an item. */
    static final class Tag implements MarkdownFeature {
        private final String tag;
        private final java.util.function.BiConsumer<PageCompiler, String> visit;

        Tag(String tag, java.util.function.BiConsumer<PageCompiler, String> visit) {
            this.tag = tag;
            this.visit = visit;
        }

        @Override
        public String name() {
            return "c0de " + tag;
        }

        @Override
        public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
            return compiler instanceof PageCompiler;
        }

        @Override
        public void registerTokens(TokenRegistrar registrar) {
            registrar.registerToken((nibbler, tokens) -> {
                if (!nibbler.tryConsume("<" + tag + ";")) {
                    return false;
                }
                String id = nibbler.consumeUntil('>');
                if (id == null || id.isBlank()) {
                    return false;
                }
                tokens.add(new TagToken(this, id.strip()));
                return true;
            }, '<');
        }

        @Override
        public void registerNodes(NodeRegistrar registrar) {
            registrar.registerNode(
                    (parser, token, tokens) -> new Visit(compiler -> visit.accept(compiler, token.id)),
                    (token, tokens) -> token instanceof TagToken found && found.feature == this ? found : null);
        }

        private static final class TagToken extends Lexer.Token {
            final Tag feature;
            final String id;

            TagToken(Tag feature, String id) {
                super("<" + feature.tag + ";" + id + ">");
                this.feature = feature;
                this.id = id;
            }
        }
    }

    /** {@code ;;;;;}: the next element starts on a new page. */
    static final class PageBreak implements MarkdownFeature {
        @Override
        public String name() {
            return "c0de page break";
        }

        @Override
        public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
            return compiler instanceof PageCompiler;
        }

        @Override
        public void registerTokens(TokenRegistrar registrar) {
            registrar.registerToken((nibbler, tokens) -> {
                if (!nibbler.tryConsume(";;;;;")) {
                    return false;
                }
                tokens.add(new BreakToken());
                return true;
            }, ';');
        }

        @Override
        public void registerNodes(NodeRegistrar registrar) {
            registrar.registerNode(
                    (parser, token, tokens) -> new Visit(PageCompiler::visitPageBreak),
                    (token, tokens) -> token instanceof BreakToken found ? found : null);
        }

        private static final class BreakToken extends Lexer.Token {
            BreakToken() {
                super(";;;;;");
            }
        }
    }

    /** A node that calls the page compiler. */
    private static final class Visit extends Parser.Node {
        private final Consumer<PageCompiler> visit;

        Visit(Consumer<PageCompiler> visit) {
            this.visit = visit;
        }

        @Override
        protected void visitStart(MarkdownCompiler<?> compiler) {
            visit.accept((PageCompiler) compiler);
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {}
    }
}
