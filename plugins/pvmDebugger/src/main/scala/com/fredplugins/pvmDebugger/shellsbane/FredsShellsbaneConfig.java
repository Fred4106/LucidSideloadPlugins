package com.fredplugins.pvmDebugger.shellsbane;

import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup(value = FredsShellsbaneConfig.GROUP, secondaryConfig = true)
public interface FredsShellsbaneConfig extends Config {
	static final String GROUP = "FredsShellsbaneHelper";

	@ConfigItem(
		name = "Enabled",
		description = "Is Shellsbane helper enabled?",
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
		name = "Thralls Enabled",
		description = "Auto recast summon thrall.",
		position = 10,
		keyName = "thrallsEnabled",
		section = baseSection
	)
	default boolean thrallsEnabled()
	{
		return false;
	}

	@ConfigItem(
		name = "Death Charge Enabled",
		description = "Auto recast death charge.",
		position = 11,
		keyName = "deathChargeEnabled",
		section = baseSection
	)
	default boolean deathChargeEnabled()
	{
		return false;
	}

	@ConfigItem(
		name = "Divine Super Combat Enabled",
		description = "Auto drink divine super combat potions as needed.",
		position = 12,
		keyName = "divineSuperCombatsEnabled",
		section = baseSection
	)
	default boolean divineSuperCombatsEnabled()
	{
		return false;
	}

	@ConfigItem(
		name = "Prayer Regen Enabled",
		description = "Auto drink prayer regeneration potions as needed.",
		position = 13,
		keyName = "prayerRegensEnabled",
		section = baseSection
	)
	default boolean prayerRegensEnabled()
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
		keyName = "acidColor",
		name = "Acid Color",
		description = "Color of the corrosive projectile.",
		position = 1,
		section = colorsSection
	)
	@Alpha
	default Color acidColor() {
		return Color.ORANGE;
	}

	@ConfigItem(
		keyName = "whirlwindColor",
		name = "Whirlwind Color",
		description = "Color of the summoned whirlwind.",
		position = 2,
		section = colorsSection
	)
	@Alpha
	default Color whirlwindColor() {
		return Color.CYAN;
	}
}
