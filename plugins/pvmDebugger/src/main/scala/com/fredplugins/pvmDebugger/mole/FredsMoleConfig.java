package com.fredplugins.pvmDebugger.mole;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup(value = FredsMoleConfig.GROUP, secondaryConfig = true)
public interface FredsMoleConfig extends Config {
	static final String GROUP = "FredsMoleHelper";

	@ConfigItem(
		name = "Enabled",
		description = "Is Giant Mole helper enabled?",
		position = 0,
		keyName = "enabled"
	)
	default boolean enabled()
	{
		return false;
	}

	@ConfigSection(
		name = "General",
		description = "General settings",
		position = 0
	)
	String baseSection = "General";

	@ConfigItem(
		name = "Auto Prayer Deadeye",
		description = "Auto enable deadeye when in range of mole.",
		position = 10,
		keyName = "deadeyeEnabled",
		section = baseSection
	)
	default boolean deadeyeEnabled()
	{
		return false;
	}

	@ConfigItem(
		name = "Flick Deadeye",
		description = "Auto flick deadeye when in range of mole.",
		position = 11,
		keyName = "deadeyeFLick",
		section = baseSection
	)
	default boolean deadeyeFlick()
	{
		return false;
	}

	@ConfigItem(
		name = "Protect from Melee",
		description = "Auto enable protect from melee when in range of mole.",
		position = 12,
		keyName = "protectFromMeleeEnabled",
		section = baseSection
	)
	default boolean protectFromMeleeEnabled()
	{
		return false;
	}
	@ConfigItem(
		name = "Auto Prayer Potion",
		description = "Auto drink prayer potions as needed.",
		position = 14,
		keyName = "prayerPotEnabled",
		section = baseSection
	)
	default boolean prayerPotEnabled()
	{
		return false;
	}

	@ConfigItem(
		name = "Auto Divine Range Potion",
		description = "Auto drink divine ranged potion as needed.",
		position = 15,
		keyName = "divineRangeEnabled",
		section = baseSection
	)
	default boolean divineRangeEnabled()
	{
		return false;
	}

	@ConfigItem(
		name = "Auto Stamina Potion",
		description = "Auto drink stamina potion as needed.",
		position = 16,
		keyName = "staminaEnabled",
		section = baseSection
	)
	default boolean staminaEnabled()
	{
		return false;
	}

	@ConfigSection(
		name = "Colors",
		description = "Color settings.",
		position = 10
	)
	String colorsSection = "Colors";


	@ConfigItem(
		keyName = "moleColor",
		name = "Mole Color",
		description = "Giant mole highlight color.",
		position = 1,
		section = colorsSection
	)
	@Alpha
	default Color moleColor() {
		return Color.CYAN;
	}

//	@ConfigItem(
//		keyName = "whirlwindColor",
//		name = "Whirlwind Color",
//		description = "Color of the summoned whirlwind.",
//		position = 2,
//		section = colorsSection
//	)
//	@Alpha
//	default Color whirlwindColor() {
//		return Color.CYAN;
//	}
}
