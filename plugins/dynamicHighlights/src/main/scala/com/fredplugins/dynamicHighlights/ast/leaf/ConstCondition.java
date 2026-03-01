package com.fredplugins.dynamicHighlights.ast.leaf;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.ast.LeafCondition;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class ConstCondition extends LeafCondition {
    private final boolean target;

    public ConstCondition(boolean target) {
        this.target = target;
    }

    @Override
    public boolean test(FredsLootFiltersPlugin plugin, PluginTileItem item) {
        return target;
    }
}
