package com.fredplugins.dynamicHighlights.ast;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;

public abstract class Condition {
    public abstract boolean test(FredsLootFiltersPlugin plugin, PluginTileItem item);
}
