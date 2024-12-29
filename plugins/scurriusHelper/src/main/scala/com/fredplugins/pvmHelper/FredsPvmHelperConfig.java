package com.fredplugins.pvmHelper;

import net.runelite.client.config.*;

@ConfigGroup(FredsPvmHelperConfig.GroupName)
public interface FredsPvmHelperConfig extends Config {
	final String GroupName = "fredspvmhelper";
	//region Debug
	@ConfigSection(
		name = "Debug",
		description = "Debug details",
		position = 0
	)
	String DEBUG_SECTION = "Debug";
	@ConfigItem(
		name = "Debug Regions",
		description = "Enter the region ids that will enable logging, separated by commas",
		position = 0,
		keyName = "debugRegions",
		section = DEBUG_SECTION
	)
	default String getDebugRegions()
	{
		return "";
	}
	@ConfigItem(
		name = "Debug Varps",
		description = "Enter the varp ids that will enable logging, separated by commas",
		position = 1,
		keyName = "debugVarps",
		section = DEBUG_SECTION
	)
	default String getDebugVarps()
	{
		return "";
	}
	@ConfigItem(
		name = "Debug Varbits",
		description = "Enter the varbit ids that will enable logging, separated by commas",
		position = 2,
		keyName = "debugVarbits",
		section = DEBUG_SECTION
	)
	default String getDebugVarbits()
	{
		return "";
	}
	//endregion



	//region Panel
	@ConfigSection(
		name = "Panel",
		description = "Debug Panel details",
		position = 1
	)
	String PANEL_SECTION = "Panel";

	@Range(
		min = 6,
		max = 32
	)
	@ConfigItem(
		keyName = "fontSize",
		name = "Font Size",
		description = "sets font size for overlay",
		position = 0,
		section = PANEL_SECTION
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
		section = PANEL_SECTION
	)
	default boolean getFontBold()
	{
		return true;
	}
	//endregion
}