package com.fredplugins.pvmHelper.hunleff;

import net.runelite.client.config.*;

@ConfigGroup(HunleffConfig.GroupName)
public interface HunleffConfig extends Config {
	final String GroupName = "fredspvmhelper_hunllef";
	//region general
	@ConfigSection(
		name = "General",
		description = "Settings to control fight with Hunllef",
		position = 0
	)
	String hunllefSection = "hunllefSettings";

	@ConfigItem(
		name = "Auto Pray Mage",
		description = "Automatically use protection prayer against hunllef",
		position = 0,
		keyName = "autoPray",
		section = hunllefSection
	)
	default boolean autoPray() {
		return false;
	}

	@ConfigItem(
		name = "Auto offense pray",
		description = "Auto selects offensive prayer when weapon swapping.",
		position = 3,
		keyName = "autoOffensive",
		section = hunllefSection
	)
	default boolean autoOffense()
	{
		return false;
	}
	@ConfigItem(
		name = "Auto defense pray",
		description = "Auto selects defence prayer.",
		position = 4,
		keyName = "autoDefense",
		section = hunllefSection
	)
	default boolean autoDefense()
	{
		return false;
	}

	@ConfigItem(
		name = "Auto dodge",
		description = "Automatically dodges unsafe tiles and tornados",
		position = 7,
		keyName = "autoDodge",
		section = hunllefSection
	)
	default boolean autoDodge()
	{
		return false;
	}
	//endregion
	//region iconSize
	@ConfigSection(
		name = "overlay",
		description = "Settings to control Hunllef overlays",
		position = 1
	)
	String overlaySection = "overlaySettings";
	@Range(
		min = 12,
		max = 64
	)
	@ConfigItem(
		name = "Icon size",
		description = "Change the size of the attack style icon.",
		position = 10,
		keyName = "hunllefAttackStyleIconSize",
		section = overlaySection
	)
	@Units(Units.PIXELS)
	default int hunllefAttackStyleIconSize()
	{
		return 18;
	}

	@Range(
		min = 12,
		max = 64
	)
	@ConfigItem(
		name = "Icon size",
		description = "Change the size of the projectile icons.",
		position = 2,
		keyName = "projectileIconSize",
		section = overlaySection
	)
	@Units(Units.PIXELS)
	default int projectileIconSize()
	{
		return 18;
	}
}