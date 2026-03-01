package com.fredplugins.dynamicHighlights;

import com.fredplugins.dynamicHighlights.model.NamedQuantity;
import com.fredplugins.dynamicHighlights.model.SoundProvider;
import com.fredplugins.dynamicHighlights.ast.AndCondition;
import com.fredplugins.dynamicHighlights.ast.leaf.ItemNameCondition;
import com.fredplugins.dynamicHighlights.ast.leaf.ItemQuantityCondition;
import com.fredplugins.dynamicHighlights.ast.OrCondition;
import com.fredplugins.dynamicHighlights.ast.Condition;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class FilterRule {
    private final Condition cond;
    private final DisplayConfigOld display;
    private final boolean isTerminal;
    private final int sourceLine;

    public static FilterRule highlight(FredsLootFiltersConfig config) {
        var rawNames = config.highlightedItems();
        var rule = new OrCondition(
                Arrays.stream(rawNames.split(","))
                        .map(NamedQuantity::fromString)
                        .map(it -> new AndCondition(new ItemNameCondition(it.getName()), new ItemQuantityCondition(it.getQuantity(), it.getComparator())))
                        .collect(Collectors.toList())
        );

        SoundProvider sound = null;
        var configSound = config.highlightSound();
        try {
            var soundId = Integer.parseInt(configSound);
            sound = new SoundProvider.SoundEffect(soundId);
        } catch (NumberFormatException e) {
            if (!configSound.isBlank()) {
                sound = new SoundProvider.File(configSound);
            }
        }
        var display = DisplayConfigOld.builder()
                .textColor(config.highlightColor())
                .showLootbeam(config.highlightLootbeam())
                .notify(config.highlightNotify())
                .backgroundColor(config.higlightBackgroundColor())
                .borderColor(config.highlightBorderColor())
                .lootbeamColor(config.highlightLootbeamColor())
                .menuTextColor(config.highlightMenuTextColor())
                .menuSort(config.highlightMenuSort())
                .sound(sound)
                .build();
        return new FilterRule(rule, display, true, -3);
    }

    public FilterRule withDisplay(Consumer<DisplayConfigOld.DisplayConfigBuilder> consumer) {
        var builder = display.toBuilder();
        consumer.accept(builder);
        return new FilterRule(cond, builder.build(), isTerminal, sourceLine);
    }

    public static FilterRule hide(String rawNames) {
        var rule = new OrCondition(
                Arrays.stream(rawNames.split(","))
                        .map(NamedQuantity::fromString)
                        .map(it -> new AndCondition(new ItemNameCondition(it.getName()), new ItemQuantityCondition(it.getQuantity(), it.getComparator())))
                        .collect(Collectors.toList())
        );
        var display = DisplayConfigOld.builder()
                .hidden(true)
                .build();
        return new FilterRule(rule, display, true, -4);
    }
}
