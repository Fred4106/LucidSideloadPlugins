package com.fredplugins.superClickHelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(SuperClickHelperConfig.GroupName)
public interface SuperClickHelperConfig extends Config
{
	final String GroupName = "superClickHelper";
	@ConfigSection(
		name = "Alches",
		description = "One click alch settings",
		position = 0
	)
	String ALCH_SECTION = "Alch";

	@ConfigItem(
		position = 0,
		keyName = "isAlchNotesOnly",
		name = "Notes Only",
		description = "Only add alch one-click to noted items.",
		section = ALCH_SECTION
	)
	default boolean isAlchNotesOnly()
	{
		return true;
	}


	@ConfigSection(
		name = "Cooking",
		description = "One click cooking settings",
		position = 1
	)
	String COOKING_SECTION = "Cooking";

	@ConfigItem(
		position = 0,
		keyName = "isCookingEnabled",
		name = "Enabled",
		description = "Enables the CookingHelper module.",
		section = COOKING_SECTION
	)
	default boolean isCookingEnabled()
	{
		return true;
	}

//	@Alpha
//	@ConfigItem(
//		position = 2,
//		keyName = "hexColorWatered",
//		name = "Watered plant",
//		description = "Color of watered plant timer"
//	)
//	default Color getColorWatered()
//	{
//		return new Color(0, 153, 255);
//	}
//
//	@Alpha
//	@ConfigItem(
//		position = 3,
//		keyName = "hexColorGrown",
//		name = "Grown plant",
//		description = "Color of grown plant timer"
//	)
//	default Color getColorGrown()
//	{
//		return new Color(0, 217, 0);
//	}
//
//	@Alpha
//	@ConfigItem(
//		position = 4,
//		keyName = "hexColorDead",
//		name = "Dead plant",
//		description = "Color of dead plant timer"
//	)
//	default Color getColorDead() {
//		return new Color(64, 64, 64);
//	}
//
//		@Alpha
//	@ConfigItem(
//		position = 5,
//		keyName = "hexColorEmpty",
//		name = "Empty plant",
//		description = "Color of empty plant fields"
//	)
//	default Color getColorEmpty() {
//		return new Color(93, 56, 45);
//	}

	@ConfigSection(
		name = "Hallowed Sepulchre",
		description = "Hallowed Sepulchre config details",
		position = 8
	)
	String HALLOWED_SEPULCHRE_SECTION = "Hallowed Sepulchre";
	@ConfigItem(
		keyName = "hallowedSepulchrePanel",
		name = "Enable Hallowed Sepulchre Panel",
		description = "enables debug panel for hallowed sepulchre",
		position = 0,
		section = HALLOWED_SEPULCHRE_SECTION
	)
	default boolean isHallowedSepulchrePanel()
	{
		return true;
	}

		@ConfigItem(
		keyName = "hallowedSepulchreOverlay",
		name = "Enable Hallowed Sepulchre Overlay",
		description = "enables debug overlay for hallowed sepulchre",
		position = 1,
		section = HALLOWED_SEPULCHRE_SECTION
	)
	default boolean isHallowedSepulchreOverlay()
	{
		return true;
	}


	@ConfigSection(
		name = "Debug",
		description = "Debug details",
		position = 9
	)
	String DEBUG_SECTION = "Debug";
	@Range(
		min = 6,
		max = 32
	)
	@ConfigItem(
		keyName = "fontSize",
		name = "Font Size",
		description = "sets font size for overlay",
		position = 0,
		section = DEBUG_SECTION
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
		section = DEBUG_SECTION
	)
	default boolean getFontBold()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugClicks",
		name = "Debug Clicks",
		description = "enables debug overlay for clicked targets",
		position = 2,
		section = DEBUG_SECTION
	)
	default boolean isDebugClicks()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugMenus",
		name = "Debug MenuEntries",
		description = "enables debug overlay for menu entries",
		position = 3,
		section = DEBUG_SECTION
	)
	default boolean isDebugMenu()
	{
		return false;
	}

	@ConfigItem(
		keyName = "debugSelectedWidget",
		name = "Debug Selected Widget",
		description = "enables debug for selected widget value",
		position = 4,
		section = DEBUG_SECTION
	)
	default boolean isDebugSelectedWidget()
	{
		return true;
	}


	@ConfigItem(
		keyName = "debugInventoryMonitorService",
		name = "Debug Inventory Service",
		description = "shows debug overlay for the inventory monitor service",
		position = 5,
		section = DEBUG_SECTION
	)
	default boolean isDebugInventoryMonitorService()
	{
		return true;
	}
}
