package com.fredplugins.dynamicHighlights.ast.leaf;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.ast.LeafCondition;
import com.fredplugins.dynamicHighlights.model.PluginTileItem;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

import static com.fredplugins.dynamicHighlights.util.TextUtil.isInfixWildcard;

@EqualsAndHashCode(callSuper = false)
@ToString
public class ItemNameCondition extends LeafCondition {
    private final List<String> names;

    public ItemNameCondition(List<String> names) {
        this.names = names;
    }

    public ItemNameCondition(String name) {
        this.names = List.of(name);
    }

    @Override
    public boolean test(FredsLootFiltersPlugin plugin, PluginTileItem item) {
        return names.stream().anyMatch(it -> test(item.getName(), it));
    }

    private boolean test(String name, String target) {
        if (target.equals("*")) {
            return true;
        } else if (target.startsWith("*") && target.endsWith("*")) {
            return name.toLowerCase().contains(target.toLowerCase().substring(1, target.length() - 1));
        } else if (target.startsWith("*")) {
            return name.toLowerCase().endsWith(target.toLowerCase().substring(1));
        } else if (target.endsWith("*")) {
            return name.toLowerCase().startsWith(target.toLowerCase().substring(0, target.length() - 1));
        } else if (isInfixWildcard(target)) {
            var lowercase = name.toLowerCase();
            var index = target.indexOf('*');
            var before = target.substring(0, index).toLowerCase();
            var after = target.substring(index + 1).toLowerCase();
            return lowercase.startsWith(before) && lowercase.endsWith(after);
        }
        return name.equalsIgnoreCase(target);
    }
}
