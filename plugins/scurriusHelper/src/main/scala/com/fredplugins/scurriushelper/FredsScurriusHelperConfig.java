package com.fredplugins.scurriushelper;

import net.runelite.client.config.*;

@ConfigGroup(FredsScurriusHelperConfig.GroupName)
public interface FredsScurriusHelperConfig extends Config {
	final String GroupName = "freds-scurrius-helper";
	@ConfigSection(
		name = "General",
		description = "General settings",
		position = 0
	)
	String GENERAL_PANEL = "General";

	@ConfigSection(
		name = "Panel",
		description = "Debug Panel details",
		position = 1
	)
	String DEBUG_PANEL = "Debug";


	@ConfigItem(
		name = "Dodge within melee range",
		description = "Dodges only to tiles within melee range of Scurrius (for meleeing)",
		position = 0,
		keyName = "stayMelee",
		section = GENERAL_PANEL
	)
	default boolean stayMelee() {
		return false;
	}

	@ConfigItem(
		name = "Attack after dodging",
		description = "Attacks Scurrius after dodging the falling ceiling attack",
		position = 1,
		keyName = "attackAfterDodge",
		section = GENERAL_PANEL
	)
	default boolean attackAfterDodge() {
		return false;
	}

	@ConfigItem(
		name = "Attack On Spawn",
		description = "Attacks Scurrius when he spawns",
		position = 2,
		keyName = "attackOnSpawn",
		section = GENERAL_PANEL
	)
	default boolean attackOnSpawn() {
		return false;
	}

	@ConfigItem(
		name = "Attack Giant Rats",
		description = "Attacks Giant Rats When Scurrius is dead",
		position = 3,
		keyName = "attackRats",
		section = GENERAL_PANEL
	)
	default boolean attackRats() {
		return false;
	}

	@ConfigItem(
		name = "Prioritize Giant Rats over Scurrius",
		description = "Attacks Giant Rats Over Scurrius to kill them first",
		position = 4,
		keyName = "prioritizeRats",
		section = GENERAL_PANEL
	)
	default boolean prioritizeRats() {
		return false;
	}

	@ConfigItem(
		name = "Auto Pray",
		description = "Auto pray against attacks",
		position = 5,
		keyName = "autoPray",
		section = GENERAL_PANEL
	)
	default boolean autoPray() {
		return false;
	}




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
}