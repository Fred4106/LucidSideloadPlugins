package com.fredplugins.dynamicHighlights.ast.leaf;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.ast.LeafCondition;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@ToString
public class ItemTradeableCondition extends LeafCondition {
    private final boolean target;

    public ItemTradeableCondition(boolean target) {
        this.target = target;
    }

    @Override
    public boolean test(FredsLootFiltersPlugin plugin, PluginTileItem item) {
        return item.isMoney() ? target : item.isTradeable() == target;
    }
}
