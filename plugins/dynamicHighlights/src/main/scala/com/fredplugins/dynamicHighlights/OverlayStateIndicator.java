package com.fredplugins.dynamicHighlights;

import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

import com.google.inject.Inject;
import java.awt.Color;

public class OverlayStateIndicator extends InfoBox {
    private final FredsLootFiltersPlugin plugin;
//    private final FredsLootFiltersConfig config;

    @Inject
    public OverlayStateIndicator(FredsLootFiltersPlugin plugin) {
        super(Icons.overlay_disabled(), plugin);
        this.plugin = plugin;
//        this.config = config;
        setPriority(InfoBoxPriority.LOW);
    }

    @Override
    public boolean render() {
        return plugin.getConfig().hotkeyStateIndicator() && !plugin.isOverlayEnabled();
    }

    @Override
    public String getTooltip() {
        return "[Loot Filters]: The text overlay is currently <col=ff0000>disabled</col>.<br>" +
                "Tap <col=00ffff>" + plugin.getConfig().hotkey() + "</col> once to re-enable it.<br><br>" +
                "<col=a0a0a0>You can disable this indicator in plugin config:<br>" +
                "Loot Filters -> Hotkey -> Overlay state indicator</col>";
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public Color getTextColor() {
        return null;
    }
}
