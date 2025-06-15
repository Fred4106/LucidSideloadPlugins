package com.fredplugins.pvmDebugger.moons;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value = FredsMoonConfig.GROUP)
public interface FredsMoonConfig extends Config {
	static final String GROUP = "FredsMoonHelper";

	@ConfigSection(
		name = "General",
		description = "General settings shared between rooms",
		position = 0
	)
	String baseSection = "General";

	@ConfigItem(
		position = 1,
		keyName = "showKillTimers",
		name = "Time kills",
		description = "Displays the amount of time taken as an infobox.",
		section = baseSection
	)
	default boolean showKillTimers() {
		return true;
	}

	@ConfigSection(
		name = "Eclipse Moon",
		description = "Eclipse Moon Helper",
		position = 1
	)
	String eclipseSection = "Eclipse Moon";

	@ConfigItem(
		position = 1,
		keyName = "eclipse.enabled",
		name = "Eclipse Helper",
		description = "Displays the time taken to kill Boss as an infobox.",
		section = eclipseSection
	)
	default boolean eclipse_enabled() {
		return true;
	}


	@ConfigSection(
		name = "Blue Moon",
		description = "Blue Moon Helper",
		position = 2
	)
	String blueSection = "Blue Moon";
		@ConfigItem(
		position = 1,
		keyName = "blue.enabled",
		name = "Blue Helper",
		description = "Displays the time taken to kill Boss as an infobox.",
		section = blueSection
	)
	default boolean blue_enabled() {
		return true;
	}


	@ConfigSection(
		name = "Blood Moon",
		description = "Blood Moon Helper",
		position = 3
	)
	String bloodSection = "Blood Moon";
	@ConfigItem(
		position = 1,
		keyName = "blood.enabled",
		name = "Blood Helper",
		description = "Displays the time taken to kill Boss as an infobox.",
		section = bloodSection
	)
	default boolean blood_enabled() {
		return true;
	}
}
