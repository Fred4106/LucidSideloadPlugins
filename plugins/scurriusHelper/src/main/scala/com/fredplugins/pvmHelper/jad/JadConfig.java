package com.fredplugins.pvmHelper.jad;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(JadConfig.GroupName)
public interface JadConfig extends Config {
	final String GroupName = "fredspvmhelper_jad";
	//region waves
	@ConfigSection(
		name = "Waves",
		description = "Settings to control waves before JAD.",
		position = 0
	)
	String waveSettings = "waveSettings";

	@ConfigItem(
		name = "Auto Pray Mage",
		description = "Enables Protect from Mage against Ket-Zek",
		position = 0,
		keyName = "autoPrayMage",
		section = waveSettings
	)
	default boolean autoPrayMage() {
		return false;
	}

	@ConfigItem(
		name = "Auto Pray Range",
		description = "Enables Protect from Range against Tok-Xil",
		position = 1,
		keyName = "autoPrayRange",
		section = waveSettings
	)
	default boolean autoPrayRange() {
		return false;
	}

	@ConfigItem(
		name = "Auto Pray Melee",
		description = "Enables Protect from Melee against Yt-MejKot",
		position = 2,
		keyName = "autoPrayMelee",
		section = waveSettings
	)
	default boolean autoPrayMelee() {
		return false;
	}

	@ConfigItem(
		name = "Auto Prayer Pot",
		description = "Minimum Prayer to allow before repotting",
		position = 3,
		keyName = "minimumPrayer",
		section = waveSettings
	)
	default int minimumPrayer() {
		return 12;
	}
	//endregion


	@ConfigSection(
		name = "Jad",
		description = "Settings relating to JAD.",
		position = 1
	)
	String jadSettings = "jadSettings";

	@ConfigItem(
		name = "Use Melee Prayer",
		description = "Enables Protect from Melee when JAD is not doing a range or mage attack.",
		position = 0,
		keyName = "defaultToMelee",
		section = jadSettings
	)
	default boolean defaultToMelee() {
		return false;
	}
	//endregion
}