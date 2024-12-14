package com.fredplugins.pvmHelper;

import net.runelite.client.config.*;

@ConfigGroup(FredsPvmHelperConfig.GroupName)
public interface FredsPvmHelperConfig extends Config {
	final String GroupName = "fredspvmhelper";

	//region Scurrius
	@ConfigSection(
		name = "Scurrius",
		description = "Scurrius settings",
		position = 0
	)
	String SCURRIUS_SECTION = "scurrius";

	@ConfigItem(
		name = "Dodge within melee range",
		description = "Dodges only to tiles within melee range of Scurrius (for meleeing)",
		position = 0,
		keyName = "scurrius-stayMelee",
		section = SCURRIUS_SECTION
	)
	default boolean stayMelee() {
		return false;
	}

	@ConfigItem(
		name = "Attack after dodging",
		description = "Attacks Scurrius after dodging the falling ceiling attack",
		position = 1,
		keyName = "scurrius-attackAfterDodge",
		section = SCURRIUS_SECTION
	)
	default boolean attackAfterDodge() {
		return false;
	}

	@ConfigItem(
		name = "Attack On Spawn",
		description = "Attacks Scurrius when he spawns",
		position = 2,
		keyName = "scurrius-attackOnSpawn",
		section = SCURRIUS_SECTION
	)
	default boolean attackOnSpawn() {
		return false;
	}

	@ConfigItem(
		name = "Attack Giant Rats",
		description = "Attacks Giant Rats When Scurrius is dead",
		position = 3,
		keyName = "scurrius-attackRats",
		section = SCURRIUS_SECTION
	)
	default boolean attackRats() {
		return false;
	}

	@ConfigItem(
		name = "Prioritize Giant Rats over Scurrius",
		description = "Attacks Giant Rats Over Scurrius to kill them first",
		position = 4,
		keyName = "scurrius-prioritizeRats",
		section = SCURRIUS_SECTION
	)
	default boolean prioritizeRats() {
		return false;
	}

	@ConfigItem(
		name = "Auto Pray",
		description = "Auto pray against attacks",
		position = 5,
		keyName = "scurrius-autoPray",
		section = SCURRIUS_SECTION
	)
	default boolean autoPray() {
		return false;
	}
	//endregion

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