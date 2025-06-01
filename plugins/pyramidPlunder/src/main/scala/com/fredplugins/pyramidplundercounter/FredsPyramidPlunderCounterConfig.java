package com.fredplugins.pyramidplundercounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(FredsPyramidPlunderCounterConfig.GroupName)
public interface FredsPyramidPlunderCounterConfig extends Config
{
	final String GroupName = "fredsPyramidPlunderCounter";
	@ConfigItem(
			position = 0,
			keyName = "showChestsLooted",
			name = "Chests Looted",
			description = "Displays the number of chests looted"
	)
	default boolean showChestsLooted() {
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "showSarcoLooted",
			name = "Sarcophagus Looted",
			description = "Displays the number of sarcophagus looted"
	)
	default boolean showSarcoLooted() {
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "showChance",
			name = "% Chance of having received at least one sceptre",
			description = "Displays the percentage chance of having received at least one sceptre."
	)
	default boolean showChance() {
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "showPetChance",
			name = "% Chance of having received pet",
			description = "Displays the percentage chance of having received at least one pet."
	)
	default boolean showPetChance() {
		return true;
	}

	@ConfigItem(
			position = 4,
			keyName = "saveData",
			name = "Save your data",
			description = "Save your data cross-sessions to keep track of it."
	)

	default boolean saveData() {
		return true;
	}


	//region Overlay
	@ConfigSection(
		name = "Overlay",
		description = "Overlay settings",
		position = 5
	)
	String OVERLAY_SECTION = "Overlay";
	@Range(
		min = 6,
		max = 32
	)
	@ConfigItem(
		keyName = "fontSize",
		name = "Font Size",
		description = "sets font size for overlay",
		position = 0,
		section = OVERLAY_SECTION
	)
	default int getFontSize()
	{
		return 14;
	}


	@ConfigItem(
		keyName = "fontBold",
		name = "Bold Font",
		description = "sets bold font for overlay",
		position = 1,
		section = OVERLAY_SECTION
	)
	default boolean getFontBold()
	{
		return true;
	}
	//endregion

	//region Debug
	@ConfigSection(
		name = "Debug",
		description = "Debug settings",
		position = 6
	)
	String DEBUG_SECTION = "Debug";
	@ConfigItem(
		keyName = "debugMenus",
		name = "Debug MenuEntries",
		description = "enables debug overlay for menu entries",
		position = 0,
		section = DEBUG_SECTION
	)
	default boolean isDebugMenu()
	{
		return true;
	}
	//endregion
}
