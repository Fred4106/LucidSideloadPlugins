package com.fredplugins.teleportMenu;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FredsBetterTeleportMenuConfig.GROUP)
public interface FredsBetterTeleportMenuConfig extends Config
{
	static final String GROUP = "fredsbetterteleportmenu";
	static final String KEYBIND_PREFIX = "keybind.";

	@ConfigItem(
		keyName = "aliasNumpad",
		name = "Alias Numpad keys",
		description = "Treat numpad keys as their number row variants"
	)
	default boolean aliasNumpad()
	{
		return true;
	}

	@ConfigItem(
		keyName = "alternateNames",
		name = "Alternate names",
		description = "Change confusing names like Carrallanger to more readable variants"
	)
	default boolean alternateNames()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideDisabled",
		name = "Hide disabled entries",
		description = "Prevent showing of disabled (strikethrough) entries in the \"scroll\"-style menu"
	)
	default boolean hideDisabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "expandScrollMenu",
		name = "Expand scroll menu",
		description = "Allow the \"scroll\"-style menu to get taller"
	)
	default boolean expandScrollMenu()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enhanceQuetzalContrast",
		name = "Enhance Quetzal contrast",
		description = "Darken non-interactive elements in the Varlamore quetzel transportation menu"
	)
	default boolean enhanceQuetzalContrast()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showLastDestination",
		name = "Show current \"Last-destination\"",
		description = "Show what the last destination is current set to in the Clue compass menu"
	)
	default boolean showLastDestination()
	{
		return true;
	}
}
