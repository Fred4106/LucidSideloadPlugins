package com.fredplugins.pvmDebugger.thermy;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup(value = "FredsThermyHelper", secondaryConfig = true)
public interface FredsThermyConfig extends Config {
	@ConfigItem(
		name = "Enabled",
		description = "Is Thermonuclear Smoke Devil helper enabled?",
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
		name = "Resonance walk-to only",
		description = "Hide all walk-to options except for resonance tiles.",
		position = 0,
		keyName = "resonanceWalkToOnly",
		section = baseSection
	)
	default boolean resonanceWalkToOnly()
	{
		return true;
	}

	@ConfigSection(
		name = "Colors",
		description = "Color settings.",
		position = 10
	)
	String colorsSection = "Colors";

	@ConfigItem(
		keyName = "specProjectileColor",
		name = "Spec Projectile Color",
		description = "Color of the special-attack projectile.",
		position = 21,
		section = colorsSection
	)
	@Alpha
	default Color specProjectileColor() {
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
		return Color.BLUE;
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
		return Color.RED;
	}
}
