package com.recolor;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("recolorCG")
public interface RecolorCGConfig extends Config
{
	@ConfigSection(
			name = "Colors",
			description = "General color settings",
			position = 0
	)
	String colorsSection = "colors";

	@ConfigSection(
			name = "Toggles",
			description = "Toggle different recolors on or off",
			position = 1
	)
	String togglesSection = "toggles";

	//
	// Begin of color section
	//
	@ConfigItem(
			keyName = "color",
			name = "Color",
			description = "Recolor the Corrupted Gauntlet with this color.",
			position = 0,
			section = colorsSection
	)
	default Color color()
	{
		return new Color(25, 45, 135);
	}

	@ConfigItem(
			keyName = "secondcolor",
			name = "Secondary color",
			description = "Recolor certain parts of the gauntlet differently.",
			position = 1,
			section = colorsSection
	)
	default Color secondcolor()
	{
		return new Color(165, 210, 10);
	}

	@ConfigItem(
			keyName = "secondcolor_active",
			name = "Use secondary color",
			description = "If activated, the secondary color will be applied.",
			position = 2,
			section = colorsSection
	)
	default boolean secondcolor_active()
	{
		return true;
	}

	@ConfigItem(
			keyName = "random",
			name = "Random color each run",
			description = "Every time you enter the corrupted gauntlet, it will be a random color. To randomise both first and secondary color, activate the 'Use secondary color' feature.",
			position = 3,
			section = colorsSection
	)
	default boolean random()
	{
		return false;
	}

	//
	// Begin of toggle section
	//
	@ConfigItem(
			keyName = "npcRecolor",
			name = "Recolor NPCs",
			description = "Recolor all the NPCs in the corrupted gauntlet.",
			position = 0,
			section = togglesSection
	)
	default boolean npcRecolor()
	{
		return true;
	}

	@ConfigItem(
			keyName = "projectileRecolor",
			name = "Recolor projectiles",
			description = "Recolor all the projectiles from NPCs in the corrupted gauntlet.",
			position = 1,
			section = togglesSection
	)
	default boolean projectileRecolor()
	{
		return true;
	}

	@ConfigItem(
			keyName = "groundRecolor",
			name = "Recolor damaging floor",
			description = "Recolor the floor that damages you during the Hunllef fight.",
			position = 2,
			section = togglesSection
	)
	default boolean groundRecolor()
	{
		return true;
	}

	@ConfigItem(
			keyName = "tornado",
			name = "Recolor Hunleff's tornadoes",
			description = "Recolor the tornadoes that spawn during the hunllef fight.",
			position = 3,
			section = togglesSection
	)
	default boolean tornado()
	{
		return true;
	}

	@ConfigItem(
			keyName = "harmonize",
			name = "Harmonize colors",
			description = "Adjusts colors for a unified look.",
			position = 4,
			section = togglesSection
	)
	default boolean harmonize()
	{
		return true;
	}

}
