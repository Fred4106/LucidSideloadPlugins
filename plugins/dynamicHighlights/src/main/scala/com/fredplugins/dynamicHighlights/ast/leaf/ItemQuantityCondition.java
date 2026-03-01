package com.fredplugins.dynamicHighlights.ast.leaf;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.model.Comparator;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ItemQuantityCondition extends ComparatorCondition {
    public ItemQuantityCondition(int value, Comparator cmp) {
        super(value, cmp);
    }

    @Override
    public int getLhs(FredsLootFiltersPlugin plugin, PluginTileItem item) {
        return item.getQuantity();
    }
}
