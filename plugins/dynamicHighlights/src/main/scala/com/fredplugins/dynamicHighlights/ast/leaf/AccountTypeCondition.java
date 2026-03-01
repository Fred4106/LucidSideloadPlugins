package com.fredplugins.dynamicHighlights.ast.leaf;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.ast.LeafCondition;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;
import net.runelite.api.gameval.VarbitID;

public class AccountTypeCondition extends LeafCondition {
    private final int type;

    public AccountTypeCondition(int type) {
        this.type = type;
    }

    @Override
    public boolean test(FredsLootFiltersPlugin plugin, PluginTileItem item) {
        return plugin.getClient().getVarbitValue(VarbitID.IRONMAN) == type;
    }
}
