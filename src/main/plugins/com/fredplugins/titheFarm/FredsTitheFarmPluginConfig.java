package com.fredplugins.titheFarm;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("fredsTitheFarmPlugin")
public interface FredsTitheFarmPluginConfig extends Config
{
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
}
