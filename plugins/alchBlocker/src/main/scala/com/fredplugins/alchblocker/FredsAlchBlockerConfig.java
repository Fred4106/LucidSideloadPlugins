package com.fredplugins.alchblocker;


import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FredsAlchBlockerConfig.GROUP)
public interface FredsAlchBlockerConfig extends Config
{
	static final String GROUP = "FredsAlchBlocker";

	@ConfigItem(
		keyName = "contextMenuEnabled",
		name = "Context menu add item",
		description = "Allow right clicking an item to add to the list.",
		position = 0
	)
	default boolean contextMenuEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "displayType",
		name = "Display type",
		description = "How do you want the blacklisted items shown. (explorer ring only supports transparent)",
		position = 1
	)
	default DisplayType displayType()
	{
		return DisplayType.TRANSPARENT;
	}

	@ConfigItem(
		keyName = "listType",
		name = "List type",
		description = "Blacklist will block the items in the list below from being alched. Whitelist only allows the items in the list below to be alched.",
		position = 2
	)
	default ListType listType()
	{
		return ListType.BLACKLIST;
	}

	@ConfigItem(
		keyName = "itemList",
		name = "Item list",
		description = "Configures the list of items to block or unblock from being alched. Format: (item), (item). Example: fire rune, prayer potion*",
		position = 3
	)
	default String itemList()
	{
		return "*Rune Pouch\n*(1)\n*(2)\n*(3)\n*(4)\n";
	}

	enum DisplayType {
		TRANSPARENT,
		HIDDEN
	}
	enum ListType {
		BLACKLIST,
		WHITELIST
	}
}
