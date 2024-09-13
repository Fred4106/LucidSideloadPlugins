package com.fredplugins.titheFarm2;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(FredsTitheFarmV2PluginConfig.GroupName)
public interface FredsTitheFarmV2PluginConfig extends Config
{
	final String GroupName = "fredsTitheFarmPluginV2";
	@Alpha
	@ConfigItem(
		position = 1,
		keyName = "hexColorUnwatered",
		name = "Unwatered plant",
		description = "Color of unwatered plant timer"
	)
	default Color getColorUnwatered()
	{
		return new Color(255, 187, 0);
	}

	@Alpha
	@ConfigItem(
		position = 2,
		keyName = "hexColorWatered",
		name = "Watered plant",
		description = "Color of watered plant timer"
	)
	default Color getColorWatered()
	{
		return new Color(0, 153, 255);
	}

	@Alpha
	@ConfigItem(
		position = 3,
		keyName = "hexColorGrown",
		name = "Grown plant",
		description = "Color of grown plant timer"
	)
	default Color getColorGrown()
	{
		return new Color(0, 217, 0);
	}

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "hexColorDead",
		name = "Dead plant",
		description = "Color of dead plant timer"
	)
	default Color getColorDead() {
		return new Color(64, 64, 64);
	}

	@Range(
		min = 6,
		max = 32
	)
	@ConfigItem(
		keyName = "fontSize",
		name = "Font Size",
		description = "sets font size for overlay",
		position = 10
	)
	default int getFontSize()
	{
		return 14;
	}


	@ConfigItem(
		keyName = "fontBold",
		name = "Bold Font",
		description = "sets bold font for overlay",
		position = 11
	)
	default boolean getFontBold()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugObjectClicked",
		name = "Debug Object Actions",
		description = "enables debug overlay for game object menu clicks",
		position = 12
	)
	default boolean isDebugObjectClicked()
	{
		return true;
	}
}
