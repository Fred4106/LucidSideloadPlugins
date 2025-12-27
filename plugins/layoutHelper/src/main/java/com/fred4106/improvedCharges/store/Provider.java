package com.fred4106.improvedCharges.store;

import com.fred4106.improvedCharges.FredsItemChargesConfig;
import com.google.gson.Gson;
import net.runelite.api.Client;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;

public class Provider {
    public final Client client;
    public final ClientThread clientThread;
    public final PluginManager pluginManager;
    public final ConfigManager configManager;
    public final ItemManager itemManager;
    public final InfoBoxManager infoBoxManager;
    public final ChatMessageManager chatMessageManager;
    public final TooltipManager tooltipManager;
    public final Notifier notifier;
    public final FredsItemChargesPlugin plugin;
    public final FredsItemChargesConfig config;
    public final Store store;
    public final Gson gson;

    public Provider(
        final Client client,
        final ClientThread clientThread,
        final PluginManager pluginManager,
        final ConfigManager configManager,
        final ItemManager itemManager,
        final InfoBoxManager infoBoxManager,
        final ChatMessageManager chatMessageManager,
        final TooltipManager tooltipManager,
        final Notifier notifier,
        final FredsItemChargesPlugin plugin,
        final FredsItemChargesConfig config,
        final Store store,
        final Gson gson
    ) {
        this.client = client;
        this.clientThread = clientThread;
        this.pluginManager = pluginManager;
        this.configManager = configManager;
        this.itemManager = itemManager;
        this.infoBoxManager = infoBoxManager;
        this.chatMessageManager = chatMessageManager;
        this.tooltipManager = tooltipManager;
        this.notifier = notifier;
        this.plugin = plugin;
        this.config = config;
        this.store = store;
        this.gson = gson;
    }
}
