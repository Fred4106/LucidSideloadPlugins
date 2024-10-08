package com.fredplugins.superClickHelper;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

import java.awt.*;

@ConfigGroup(SuperClickHelperConfig.GroupName)
public interface SuperClickHelperConfig extends Config
{
	final String GroupName = "superClickHelper";
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

//	@Alpha
//	@ConfigItem(
//		position = 2,
//		keyName = "hexColorWatered",
//		name = "Watered plant",
//		description = "Color of watered plant timer"
//	)
//	default Color getColorWatered()
//	{
//		return new Color(0, 153, 255);
//	}
//
//	@Alpha
//	@ConfigItem(
//		position = 3,
//		keyName = "hexColorGrown",
//		name = "Grown plant",
//		description = "Color of grown plant timer"
//	)
//	default Color getColorGrown()
//	{
//		return new Color(0, 217, 0);
//	}
//
//	@Alpha
//	@ConfigItem(
//		position = 4,
//		keyName = "hexColorDead",
//		name = "Dead plant",
//		description = "Color of dead plant timer"
//	)
//	default Color getColorDead() {
//		return new Color(64, 64, 64);
//	}
//
//		@Alpha
//	@ConfigItem(
//		position = 5,
//		keyName = "hexColorEmpty",
//		name = "Empty plant",
//		description = "Color of empty plant fields"
//	)
//	default Color getColorEmpty() {
//		return new Color(93, 56, 45);
//	}

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
		keyName = "debugClicks",
		name = "Debug Actions",
		description = "enables debug overlay for menu clicks",
		position = 12
	)
	default boolean isDebugClicks()
	{
		return true;
	}
}
