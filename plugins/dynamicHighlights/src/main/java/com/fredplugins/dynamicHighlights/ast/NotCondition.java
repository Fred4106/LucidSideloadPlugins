package com.fredplugins.dynamicHighlights.ast;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@ToString
public class NotCondition extends Condition {
    private final Condition inner;

    public NotCondition(Condition inner) {
        this.inner = inner;
    }

    @Override
    public boolean test(FredsLootFiltersPlugin plugin, PluginTileItem item) {
        return !inner.test(plugin, item);
    }
}
