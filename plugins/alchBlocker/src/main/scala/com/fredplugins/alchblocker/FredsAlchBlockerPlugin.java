package com.fredplugins.alchblocker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ch.qos.logback.classic.Level;
import com.fredplugins.alchblocker.FredsAlchBlockerConfig.DisplayType;
import com.fredplugins.alchblocker.FredsAlchBlockerConfig.ListType;
import com.google.inject.Provides;

import ethanApiPlugin.EthanApiPlugin;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.ScriptID;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.matching.Regex;

import static net.runelite.api.gameval.InterfaceID.LUMBRIDGE_ALCHEMY;
import static net.runelite.api.gameval.InterfaceID.MagicSpellbook;
import static net.runelite.api.gameval.InterfaceID.LumbridgeAlchemy;
import static net.runelite.api.gameval.InterfaceID.Inventory;

@PluginDependency(EthanApiPlugin.class)
@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Alch Blocker</html>",
	enabledByDefault = false,
	description = "Allows you to block items from being alched",
	tags = {"inventory", "alch", "block", "blocker", "hide", "high alch", "low alch", "alchemy", "inv"}
)
public class FredsAlchBlockerPlugin extends Plugin {
	private final static Logger log;

	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FredsAlchBlockerPlugin.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(FredsAlchBlockerPlugin.class);
	}

	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ConfigManager configManager;
	@Inject
	FredsAlchBlockerConfig config;

	Set<String> exactMatches = new HashSet<>();
	List<String> wildcardPatterns = new ArrayList<>();
	List<Pattern> regexPatterns = new ArrayList<>();
	Map<Integer, Boolean> blockedItemCache = new HashMap<>();
	Set<Integer> hiddenItems = new HashSet<>();

	private static final int HIGH_ALCHEMY_WIDGET_ID = MagicSpellbook.HIGH_ALCHEMY;
	private static final int LOW_ALCHEMY_WIDGET_ID  = MagicSpellbook.LOW_ALCHEMY;

	@Override
	protected void startUp() throws Exception {
		parseItemList();
	}

	@Override
	protected void shutDown() throws Exception {
		clientThread.invoke(this::showBlockedItems);
	}

	@Provides
	FredsAlchBlockerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(FredsAlchBlockerConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!FredsAlchBlockerConfig.GROUP.equals(event.getGroup())) return;
		parseItemList();
		blockedItemCache.clear();
		clientThread.invokeAtTickEnd(this::updateItemVisibility);
	}


	@Subscribe()
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (tryingToAlch(event.getMenuEntry()) && hiddenItems.contains(event.getItemId())) {
			log.debug("Should never see this as the entry should already have been removed during sort.");
			event.consume();
		}
		// Check spell state after any click (handles clicking blank spot to cancel)
		clientThread.invokeAtTickEnd(this::updateItemVisibility);
	}
	
	public boolean tryingToAlch(MenuEntry entry) {
		String menuOption = Text.removeTags(entry.getOption()).replace('\u00A0', ' ').trim();
		String menuTarget = Text.removeTags(entry.getTarget()).replace('\u00A0', ' ').trim();
		// did you just click an item to try to alch it ("High-Alchemy <item>" from explorer's ring, "Cast High Level Alchemy -> <item>" from spell)
		return menuOption.contains("-Alchemy") || (menuOption.equals("Cast") && menuTarget.contains("Alchemy ->"));
	}

	@Subscribe
	private void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() == ScriptID.INVENTORY_DRAWITEM) {
			// Use invokeAtTickEnd to check spell selection after the client state is updated
			clientThread.invokeAtTickEnd(this::updateItemVisibility);
		}
	}

	private void updateItemVisibility() {
		if (isAlchSpellSelected() || isExplorerRingOpen()) {
			hideBlockedItems();
		} else {
			showBlockedItems();
		}
	}

	private boolean isAlchSpellSelected() {
		Widget selectedWidget = client.getSelectedWidget();
		if (selectedWidget == null) {
			return false;
		}
		int widgetId = selectedWidget.getId();
		return widgetId == HIGH_ALCHEMY_WIDGET_ID || widgetId == LOW_ALCHEMY_WIDGET_ID;
	}

	private boolean isExplorerRingOpen() {
		return client.getWidget(LumbridgeAlchemy.ITEMS) != null;
	}

	@Subscribe
	private void onWidgetLoaded(WidgetLoaded event) {
		if (event.getGroupId() == LUMBRIDGE_ALCHEMY) {
			clientThread.invokeAtTickEnd(this::hideBlockedItems);
		}
	}

	@Subscribe
	private void onWidgetClosed(WidgetClosed event) {
		if (event.getGroupId() == LUMBRIDGE_ALCHEMY) {
			showBlockedItems();
		}
	}

	@Subscribe
	public void onMenuOpened(final MenuOpened event) {
		// If the user has decided to disable the context menu, no need to process further
		if (!config.contextMenuEnabled()) {
			return;
		}

		final MenuEntry[] entries = event.getMenuEntries();
		for (int idx = entries.length - 1; idx >= 0; --idx)
		{
			final MenuEntry entry = entries[idx];
			final Widget w = entry.getWidget();

			if (w != null && w.getItemId() > -1) {
				String menuTarget = Text.removeTags(entry.getTarget()).replace('\u00A0', ' ').trim();
				String menuOption = Text.removeTags(entry.getOption()).replace('\u00A0', ' ').trim();
				if (menuOption.contains("-Alchemy") || (menuOption.equals("Cast") && menuTarget.contains("Level Alchemy"))) {
					// Item already in block list, no need to add menu item
					if (
						(hiddenItems.contains(w.getItemId()) && config.listType() == ListType.BLACKLIST) ||
							(!hiddenItems.contains(w.getItemId()) && config.listType() == ListType.WHITELIST)
					) {
						return;
					}

					final String itemName = w.getName();

					client.createMenuEntry(idx)
						.setOption(config.listType() == ListType.BLACKLIST ? "Blacklist Alchemy" : "Whitelist Alchemy")
						.setTarget(itemName)
						.setType(MenuAction.RUNELITE)
						.onClick(e ->
						{
							String x = config.itemList().concat("\n" + Text.removeTags(itemName));
							String newValue = x.lines().distinct().sorted().collect(Collectors.joining("\n"));
							configManager.setConfiguration(FredsAlchBlockerConfig.GROUP, "itemList", newValue);
							showBlockedItems();
						});
				}
			}
		}
	}

	private SAlchUtils alchUtils = null;

	@Subscribe(priority = -10.0f)
	public void onPostMenuSort(PostMenuSort event) {
		if (alchUtils == null) {
			alchUtils = new SAlchUtils(client, clientThread, this);
		}
		if (!client.isMenuOpen()) {
			alchUtils.postMenuSortJava(this.hiddenItems);
		}
	}

	private void hideBlockedItems() {
		Widget inventory = client.getWidget(LumbridgeAlchemy.ITEMS);
		if (inventory == null) {
			inventory = client.getWidget(Inventory.ITEMS);
			if (inventory == null) {
				return;
			}
		}

		for (Widget inventoryItem : Objects.requireNonNull(inventory.getChildren())) {
			int itemId = inventoryItem.getItemId();
			boolean shouldBlock;

			// Check cache first for O(1) lookup
			if (blockedItemCache.containsKey(itemId)) {
				shouldBlock = blockedItemCache.get(itemId);
			} else {
				String itemName = Text.removeTags(inventoryItem.getName()).toLowerCase();
				boolean matchesPattern = isItemInBlockList(itemName);
				shouldBlock = (config.listType() == ListType.BLACKLIST) == matchesPattern;
				blockedItemCache.put(itemId, shouldBlock);
			}

			if (shouldBlock) {
				if (config.displayType() == DisplayType.TRANSPARENT || LumbridgeAlchemy.ITEMS == inventory.getId()) {
					inventoryItem.setOpacity(200);
				} else {
					inventoryItem.setHidden(true);
				}
				hiddenItems.add(itemId);
			}
		}
	}

	private boolean isItemInBlockList(String itemName) {
		// O(1) lookup for exact matches
		if (exactMatches.contains(itemName)) {
			return true;
		}
		// Only iterate wildcard patterns (typically much smaller)
		for (String pattern : wildcardPatterns) {
			if (WildcardMatcher.matches(pattern, itemName)) {
				return true;
			}
		}
		for (Pattern pattern : regexPatterns) {
			boolean result = pattern.asPredicate().test(itemName);
			log.debug("testing pattern \"{}\" against \"{}\" with result {}", pattern.pattern(), itemName, result);
			if(!result) continue;
			return result;
		}
		return false;
	}

	private void showBlockedItems() {
		if(hiddenItems.isEmpty()) {
			return;
		}

		Widget inventory = client.getWidget(LumbridgeAlchemy.ITEMS);
		inventory = (inventory == null) ? client.getWidget(Inventory.ITEMS) : inventory;

		if (inventory != null) {
			for (Widget inventoryItem : Objects.requireNonNull(inventory.getChildren())) {
				if(hiddenItems.contains(inventoryItem.getItemId())) {
					if (config.displayType() == DisplayType.TRANSPARENT || LumbridgeAlchemy.ITEMS == inventory.getId()) {
						inventoryItem.setOpacity(0);
					} else {
						inventoryItem.setHidden(false);
					}
				}
			}
		}

		hiddenItems.clear();
	}


	private void parseItemList() {
		exactMatches.clear();
		wildcardPatterns.clear();
		regexPatterns.clear();

		List<String> exactItemList = Arrays.stream(config.itemList().split("\n")).map(String::trim)
			.filter(s -> !(s.isEmpty() || s.startsWith("//")))
			.collect(Collectors.toList());
		List<String> wildcardItemList = Arrays.stream(config.wildcardItemList().split("\n")).map(String::trim)
			.filter(s -> !(s.isEmpty() || s.startsWith("//")))
			.collect(Collectors.toList());
		List<String> regexItemList = Arrays.stream(config.regexItemList().split("\n")).map(String::trim)
			.filter(s -> !(s.isEmpty() || s.startsWith("//")))
			.collect(Collectors.toList());


		for (String listItem : exactItemList) {
			if (listItem.contains("*")) {
				log.warn("exact item list contained a wildcard token\"{}\"", listItem);
				continue;
			}
			exactMatches.add(listItem.toLowerCase().trim());
		}

		for (String listItem : wildcardItemList) {
			if (!listItem.contains("*")) {
				log.warn("wildcard item list contained a non-wildcard match \"{}\"", listItem);
				continue;
			}
			wildcardPatterns.add(listItem.toLowerCase().trim());
		}

		for (String listItem : regexItemList) {
			try{
			 Pattern p = Pattern.compile("(?i)" + listItem);
			 regexPatterns.add(p);
			} catch (PatternSyntaxException e) {
				log.warn("regex item list contained an invalid regex pattern \"{}\"", listItem, e);
			}
		}
	}
}