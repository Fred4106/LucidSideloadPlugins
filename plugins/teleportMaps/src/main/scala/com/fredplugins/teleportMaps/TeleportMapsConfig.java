package com.fredplugins.teleportMaps;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(TeleportMapsConfig.GroupName)
public interface TeleportMapsConfig extends Config
{
	final String GroupName = "fredsClueCompassMap";
	String KEY_DISPLAY_HOTKEYS = "displayHotkeys";

//	@ConfigItem(
//		keyName = KEY_SHOW_CLUKEY_DISPLAY_HOTKEYSKEY_DISPLAY_HOTKEYSECOMPASS_MAP,
//		name = "Clue Compass Map",
//		description = "Replace the clue compass teleport menu with an interactive map",
//		section = teleportMaps
//	)
//	default boolean showClueCompassMap()
//	{
//		return true;
//	}

	@ConfigItem(
		keyName = KEY_DISPLAY_HOTKEYS,
		name = "Display hotkeys",
		description = "Display the travel keyboard hotkey for each map location"
	)
	default boolean displayHotkeys()
	{
		return true;
	}
}
