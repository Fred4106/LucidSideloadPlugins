package com.fredplugins.pyramidplundercounter;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

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


	//region Highlight
	@ConfigSection(
		name = "Highlight",
		description = "Highlight settings",
		position = 4
	)
	String HIGHLIGHT_SECTION = "Highlight";
	@Alpha
	@ConfigItem(
		keyName = "closedChestColor",
		name = "Highlight Chest[Closed]",
		description = "Color to highlight closed grand chest with.",
		position = 0,
		section = HIGHLIGHT_SECTION
	)
	default Color closedChestColor()
	{
		return Color.YELLOW;
	}

	@Alpha
	@ConfigItem(
		keyName = "openedChestColor",
		name = "Highlight Chest[Opened]",
		description = "Color to highlight opened grand chest with.",
		position = 1,
		section = HIGHLIGHT_SECTION
	)
	default Color openedChestColor()
	{
		return new Color(255, 255, 255, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "closedSarcColor",
		name = "Highlight Sarcophogaus[Closed]",
		description = "Color to highlight closed sarcophogaus with.",
		position = 2,
		section = HIGHLIGHT_SECTION
	)
	default Color closedSarcColor()
	{
		return Color.YELLOW;
	}

	@Alpha
	@ConfigItem(
		keyName = "openingSarcColor",
		name = "Highlight Sarcophogaus[Opening]",
		description = "Color to highlight opening sarcophogaus with.",
		position = 3,
		section = HIGHLIGHT_SECTION
	)
	default Color openingSarcColor()
	{
		return ColorUtil.colorWithAlpha(Color.ORANGE, 128);
	}
	@Alpha
	@ConfigItem(
		keyName = "openedSarcColor",
		name = "Highlight Sarcophogaus[Opened]",
		description = "Color to highlight opened sarcophogaus with.",
		position = 4,
		section = HIGHLIGHT_SECTION
	)
	default Color openedSarcColor()
	{
		return new Color(255, 255, 255, 0);
	}
	@Alpha
	@ConfigItem(
		keyName = "lockedDoorColor",
		name = "Highlight Door[Locked]",
		description = "Color to highlight locked tomb doors with.",
		position = 5,
		section = HIGHLIGHT_SECTION
	)
	default Color lockedDoorColor()
	{
		return Color.YELLOW;
	}

	@Alpha
	@ConfigItem(
		keyName = "openedDoorColor",
		name = "Highlight Door[Opened]",
		description = "Color to highlight opened tomb doors with.",
		position = 6,
		section = HIGHLIGHT_SECTION
	)
	default Color openedDoorColor()
	{
		return new Color(255, 255, 255, 0);
	}
	@Alpha
	@ConfigItem(
		keyName = "closedUrnColor",
		name = "Highlight Urn[Closed]",
		description = "Color to highlight closed urns with.",
		position = 7,
		section = HIGHLIGHT_SECTION
	)
	default Color closedUrnColor()
	{
		return Color.YELLOW;
	}
	@Alpha
	@ConfigItem(
		keyName = "snakeUrnColor",
		name = "Highlight Urn[Snake]",
		description = "Color to highlight snake urns with.",
		position = 8,
		section = HIGHLIGHT_SECTION
	)
	default Color snakeUrnColor()
	{
		return Color.GREEN;
	}
	@Alpha
	@ConfigItem(
		keyName = "charmedUrnColor",
		name = "Highlight Urn[Charmed]",
		description = "Color to highlight charmed snake urns with.",
		position = 9,
		section = HIGHLIGHT_SECTION
	)
	default Color charmedUrnColor()
	{
		return new Color(255, 255, 255, 0);
	}
	@Alpha
	@ConfigItem(
		keyName = "openedUrnColor",
		name = "Highlight Urn[Opened]",
		description = "Color to highlight opened urns with.",
		position = 10,
		section = HIGHLIGHT_SECTION
	)
	default Color openedUrnColor()
	{
		return new Color(255, 255, 255, 0);
	}
	@Alpha
	@ConfigItem(
		keyName = "spearTrapColor",
		name = "Highlight Spear Trap",
		description = "Color to highlight spear traps with.",
		position = 12,
		section = HIGHLIGHT_SECTION
	)
	default Color spearTrapColor()
	{
		return Color.ORANGE;
	}
	//endregion


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

	@Range(
		min = 1,
		max = 4
	)
	@ConfigItem(
		keyName = "borderThickness",
		name = "Border Thickness",
		description = "The thickness of the border",
		position = 2,
		section = OVERLAY_SECTION
	)
	default int borderThickness()
	{
		return 1;
	}

	@Range(
		min = 0,
		max = 4
	)
	@ConfigItem(
		keyName = "borderFeather",
		name = "Border Feather",
		description = "The feather of the border",
		position = 3,
		section = OVERLAY_SECTION
	)
	default int borderFeather()
	{
		return 0;
	}

	// alpha
	@Range(
		min = 0,
		max = 255
	)
	@ConfigItem(
		keyName = "borderAlpha",
		name = "Border Alpha",
		description = "The alpha of the border highlight",
		position = 4,
		section = OVERLAY_SECTION
	)
	default int borderAlpha()
	{
		return 255;
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
