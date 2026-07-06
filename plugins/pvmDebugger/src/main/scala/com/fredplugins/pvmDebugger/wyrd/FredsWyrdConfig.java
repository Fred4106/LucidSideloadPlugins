package com.fredplugins.pvmDebugger.wyrd;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup(value = "FredsWyrdHelper", secondaryConfig = true)
public interface FredsWyrdConfig extends Config {
	@ConfigItem(
		name = "Enabled",
		description = "Is Wyrd helper enabled?",
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
		keyName = "bossColor",
		name = "Highlight Boss Color",
		description = "Color of the boss overlay highlight.",
		position = 21,
		section = colorsSection
	)
	@Alpha
	default Color bossColor() {
		return Color.MAGENTA;
	}
}
