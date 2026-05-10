package com.fredplugins.pvmDebugger.hespori;

import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup(value = "FredsHesporiHelper", secondaryConfig = true)
public interface FredsHesporiConfig extends Config {
	@ConfigItem(
		name = "Enabled",
		description = "Is Hespori helper enabled?",
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

	@ConfigSection(
		name = "Colors",
		description = "Color settings.",
		position = 10
	)
	String colorsSection = "Colors";

	@ConfigItem(
		keyName = "aoeProjectileColor",
		name = "AOE Projectile Color",
		description = "Color of the aoe projectile.",
		position = 21,
		section = colorsSection
	)
	@Alpha
	default Color aoeProjectileColor() {
		return Color.MAGENTA;
	}

	@ConfigItem(
		keyName = "magicProjectileColor",
		name = "Magic Projectile Color",
		description = "Color of the magic projectile.",
		position = 22,
		section = colorsSection
	)
	@Alpha
	default Color magicProjectileColor() {
		return Color.CYAN;
	}

	@ConfigItem(
		keyName = "rangedProjectileColor",
		name = "Ranged Projectile Color",
		description = "Color of the ranged projectile.",
		position = 23,
		section = colorsSection
	)
	@Alpha
	default Color rangedProjectileColor() {
		return Color.ORANGE;
	}
}
