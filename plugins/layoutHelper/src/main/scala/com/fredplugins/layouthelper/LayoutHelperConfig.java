package com.fredplugins.layouthelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(LayoutHelperConfig.GROUP)
public interface LayoutHelperConfig extends Config
{
	String GROUP = "layoutHelper";

	@ConfigItem(
		keyName = "idleTimeout",
		name = "Idle timeout",
		description = "Amount of time before you are logged out for being idle."
	)
	@Units(Units.MINUTES)
	@Range(min = 5, max = 360)
	default int getIdleTimeout()
	{
		return 5;
	}
}
