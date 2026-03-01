package com.fredplugins.dynamicHighlights.ast.leaf;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.ast.LeafCondition;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class ItemIdCondition extends LeafCondition {
    private final List<Integer> ids;

    public ItemIdCondition(List<Integer> ids) {
        this.ids = ids;
    }

    public ItemIdCondition(int id) {
        this.ids = List.of(id);
    }

    @Override
    public boolean test(FredsLootFiltersPlugin plugin, PluginTileItem item) {
        return ids.stream().anyMatch(it -> item.getId() == it);
    }
}
