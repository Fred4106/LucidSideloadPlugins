package com.fredplugins.pvmHelper2;

import net.runelite.client.config.*;

@ConfigGroup(FredsPvmHelperConfig2.GroupName)
public interface FredsPvmHelperConfig2 extends Config {
	final String GroupName = "fredspvmhelper2";
	//region Debug
	@ConfigSection(
		name = "Debug",
		description = "Debug details",
		position = 0
	)
	String DEBUG_SECTION = "Debug";

	@ConfigItem(
		name = "Debug NPCs",
		description = "Enter the the npc ids that should be logged, separated by commas",
		position = 0,
		keyName = "debugNpcIds",
		section = DEBUG_SECTION
	)
	default String getNpcIds() {
		return "";
	}

	@ConfigItem(
		name = "Debug Projectiles",
		description = "Enter the projectile ids that should be logged, separated by commas",
		position = 1,
		keyName = "debugProjectileIds",
		section = DEBUG_SECTION
	)
	default String getProjectileIds() {
		return "";
	}
	//endregion

	//region Panel
	@ConfigSection(
		name = "Panel",
		description = "Debug Panel details",
		position = 5
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
	default int getFontSize() {
		return 14;
	}


	@ConfigItem(
		keyName = "fontBold",
		name = "Bold Font",
		description = "sets bold font for overlay",
		position = 1,
		section = PANEL_SECTION
	)
	default boolean getFontBold() {
		return true;
	}
	//endregion
}