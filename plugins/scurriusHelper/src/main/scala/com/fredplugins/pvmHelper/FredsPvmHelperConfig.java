package com.fredplugins.pvmHelper;

import net.runelite.client.config.*;

@ConfigGroup(FredsPvmHelperConfig.GroupName)
public interface FredsPvmHelperConfig extends Config {
	final String GroupName = "fredspvmhelper";
	//region Debug
	@ConfigSection(
		name = "Panel",
		description = "Debug Panel details",
		position = 1
	)
	String DEBUG_PANEL = "Debug";

	@Range(
		min = 6,
		max = 32
	)
	@ConfigItem(
		keyName = "fontSize",
		name = "Font Size",
		description = "sets font size for overlay",
		position = 0,
		section = DEBUG_PANEL
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
		section = DEBUG_PANEL
	)
	default boolean getFontBold()
	{
		return true;
	}
	//endregion
}