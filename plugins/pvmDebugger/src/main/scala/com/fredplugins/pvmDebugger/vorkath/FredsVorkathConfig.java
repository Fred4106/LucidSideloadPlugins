package com.fredplugins.pvmDebugger.vorkath;


import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value = FredsVorkathConfig.GROUP, secondaryConfig = true)
public interface FredsVorkathConfig extends Config
{
	static final String GROUP = "FredsVorkathHelper";

	@ConfigItem(
		name = "Enabled",
		description = "Is vorkath helper enabled?",
		position = 0,
		keyName = "enabled"
	)
	default boolean enabled()
	{
		return false;
	}

	@ConfigSection(
		position = 1,
		name = "Acid",
		description = ""
	)
	String acidTitle = "Acid";

	@ConfigItem(
		keyName = "indicateAcidPools",
		name = "Acid Pools",
		description = "Indicate the acid pools",
		position = 2,
		section = acidTitle
	)
	default boolean indicateAcidPools()
	{
		return false;
	}

	@ConfigItem(
		keyName = "indicateAcidFreePath",
		name = "Acid Free Path",
		description = "Indicate the most efficient acid free path",
		position = 3,
		section = acidTitle
	)
	default boolean indicateAcidFreePath()
	{
		return true;
	}

	@ConfigItem(
		keyName = "acidFreePathMinLength",
		name = "Minimum Length Acid Free Path",
		description = "The minimum length of an acid free path",
		position = 4,
		section = acidTitle
	)
	default int acidFreePathLength()
	{
		return 5;
	}

	@ConfigSection(
		position = 5,
		name = "Woox walk",
		description = ""
	)
	String wooxTitle = "Woox walk";

	@ConfigItem(
		keyName = "indicateWooxWalkPath",
		name = "WooxWalk Path",
		description = "Indicate the closest WooxWalk path",
		position = 6,
		section = wooxTitle
	)
	default boolean indicateWooxWalkPath()
	{
		return true;
	}

	@ConfigItem(
		keyName = "indicateWooxWalkTick",
		name = "WooxWalk Tick",
		description = "Indicate on which tile to click during each game tick",
		position = 7,
		section = wooxTitle
	)
	default boolean indicateWooxWalkTick()
	{
		return true;
	}
}
