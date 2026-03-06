package com.fredplugins.dynamicHighlights;

import com.fredplugins.dynamicHighlights.lang.CompileException;
import com.fredplugins.dynamicHighlights.lang.Lexer;
import com.fredplugins.dynamicHighlights.lang.Parser;
import com.fredplugins.dynamicHighlights.lang.Preprocessor;
import com.fredplugins.dynamicHighlights.lang.Sources;
import com.fredplugins.dynamicHighlights.lang.Token;
import com.fredplugins.dynamicHighlights.lang.TokenStream;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fredplugins.dynamicHighlights.LootFilterManager.toFilename;
import static com.fredplugins.dynamicHighlights.lang.Location.UNKNOWN_SOURCE_NAME;
import static com.fredplugins.dynamicHighlights.util.TextUtil.normalizeCrlf;

@Getter
@EqualsAndHashCode
@ToString
public class LootFilter {
    public static final LootFilter Nop = LootFilter.builder().build();

    private final String name;
    private final String filename;
    private final String description;
    private final List<FilterRule> rules;

    private LootFilter(Builder builder) {
        name = builder.name;
        filename = builder.filename;
        description = builder.description;
        rules = builder.rules;
    }

    public static LootFilter fromSourcesWithPreamble(Map<String, String> sources) throws CompileException {
        if (!sources.containsKey("preamble")) {
            // LinkedHashMap preserves insertion order for iteration ensuring preamble is handled first
            var withPreamble = new LinkedHashMap<String, String>();
            withPreamble.put("preamble", Sources.getPreamble());
            // If iteration order of input map is unstable this may change ordering of input scripts
            withPreamble.putAll(sources);
            return fromSources(withPreamble);
        }
        return fromSources(new LinkedHashMap<>(sources));
    }

    public static void dumpTokens(TokenStream stream) throws IOException {
        int tokenCount = stream.getTokens().size();
        String toWrite = stream.getTokens().stream().map((Token t) -> (t.getType() + "(" + t.getLocation().getLineNumber() + ":" + t.getLocation().getCharNumber() + ", \"" + t.getValue() + "\")"))
            .collect(Collectors.joining("\n", "tokens dump\n", "\n" + tokenCount + " tokens\n"));
        File desktopFile = new File(System.getProperty("user.home"), "Desktop");
        File newFile = new File(desktopFile, "tokenDump.dump");
        if (!newFile.createNewFile()) {
            throw new IOException("could not create file " + newFile.getPath());
        }

        try (var writer = new FileWriter(newFile)) {
            writer.write(toWrite);
        }
    }

    public static LootFilter fromSources(LinkedHashMap<String, String> sources) throws CompileException {
        var combinedStream = sources
            .entrySet().stream()
            .map(source -> {
                // Do this in 1 map iteration to ensure we preserve iteration order over our input
                var sourceValue = source.getValue();
                if (!sourceValue.endsWith("\n")) {
                    sourceValue += "\n";
                }
                return new Lexer(source.getKey(), normalizeCrlf(sourceValue));
            })
            .map(Lexer::tokenize)
            .flatMap(tokenStream -> tokenStream.getTokens().stream())
            .collect(Collectors.collectingAndThen(Collectors.toList(), TokenStream::new));

        var postproc = new Preprocessor(combinedStream).preprocess();
//        try {
//            dumpTokens(postproc);
//        } catch (IOException e) {}
        return new Parser(postproc).parse();
    }

    public static LootFilter fromSource(String source) throws CompileException {
        return fromSourcesWithPreamble(Map.of(UNKNOWN_SOURCE_NAME, source));
    }

    public Builder toBuilder() {
        var builder = new Builder();
        builder.name = name;
        builder.filename = filename;
        builder.description = description;
        builder.rules = new ArrayList<>(rules);
        return builder;
    }

    public @NonNull DisplayConfig findMatch(FredsLootFiltersPlugin plugin, PluginTileItem item) {
    var display = DisplayConfig.builder(DisplayConfig$.MODULE$.apply(Color.WHITE))
            .build();
        for (var rule : rules) {
            if (!rule.getCond().test(plugin, item)) {
                continue;
            }

            display = DisplayConfig.builder(DisplayConfig.merge(display, rule.getDisplay()))
                .withTrace(rule.getSourceLine()).build();
            if (rule.isTerminal()) {
                return display;
            }
        }
        return display;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String filename;
        private String description;
        private List<FilterRule> rules = new ArrayList<>();

        private Builder() {
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setRules(List<FilterRule> rules) {
            this.rules = rules;
            return this;
        }

        public Builder addRule(FilterRule rule) {
            rules.add(rule);
            return this;
        }

        public LootFilter build() {
            return new LootFilter(this);
        }
    }
}
