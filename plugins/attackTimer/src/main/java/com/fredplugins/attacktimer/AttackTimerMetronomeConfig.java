package com.fredplugins.attacktimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

import java.awt.*;

@ConfigGroup("Fredsattacktimermetronome")
public interface AttackTimerMetronomeConfig extends Config {
	@ConfigSection(
			name = "Attack Cooldown Tick Settings",
			description = "Change attack tick cooldown settings",
			position = 1
	)
	String TickNumberSettings = "Attack Cooldown Tick Settings";


	@ConfigItem(
			position = 1,
			keyName = "showTick",
			name = "Show Attack Cooldown Ticks",
			description = "Shows number of ticks until next attack",
			section = TickNumberSettings
	)
	default boolean showTick() {
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "disableFontScaling",
			name = "Disable Font Size Scaling (Metronome Tick Only)",
			description = "Disables font size scaling for metronome tick number",
			section = TickNumberSettings
	)
	default boolean disableFontScaling() {
		return false;
	}

	@Range(
			min = 8,
			max = 50
	)
	@ConfigItem(
			position = 4,
			keyName = "fontSize",
			name = "Font Size (Overhead Tick Only)",
			description = "Change the font size of the overhead attack cooldown ticks",
			section = TickNumberSettings
	)
	default int fontSize() {
		return 15;
	}

	@ConfigItem(
			position = 5,
			keyName = "countColor",
			name = "Tick Number Color",
			description = "Configures the color of tick number",
			section = TickNumberSettings
	)
	default Color NumberColor() {
		return Color.CYAN;
	}

	@ConfigItem(
			position = 6,
			keyName = "fontType",
			name = "Font Type",
			description = "Change the font of the Tick Number",
			section = TickNumberSettings
	)
	default FontTypes fontType() {
		return FontTypes.REGULAR;
	}

	@ConfigSection(
			name = "Attack Bar",
			description = "Change the colors and number of colors to cycle through",
			position = 2
	)
	String AttackBarSettings = "Attack Cooldown Bar Settings";

	@ConfigItem(
			position = 1,
			keyName = "attackBar",
			name = "Show Attack Bar",
			description = "Show the attack bar",
			section = AttackBarSettings
	)
	default boolean showBar() {
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "attackBarHeightOffset",
			name = "Height Offset",
			description = "Height offset for the bar from top of player model",
			section = AttackBarSettings
	)
	default int heightOffset() {
		return 0;
	}

	@ConfigItem(
			position = 3,
			keyName = "attackBarEmpties",
			name = "Empties Before Attack",
			description = "Controls whether the attack bar will fully empty before a new attack can occur",
			section = AttackBarSettings
	)
	default boolean barEmpties() {
		return true;
	}

	@ConfigItem(
			position = 4,
			keyName = "attackBarFills",
			name = "Fills Before Attack",
			description = "Controls whether the attack bar will fill completely after an attack",
			section = AttackBarSettings
	)
	default boolean barFills() {
		return true;
	}

	@ConfigItem(
			position = 5,
			keyName = "attackBarDirection",
			name = "Attack Bar Fills or Drains",
			description = "Controls whether the attack bar will fill or drain as a cooldown",
			section = AttackBarSettings
	)
	default boolean barDirection() {
		return true;
	}

//	@ConfigSection(
//			name = "Automation",
//			description = "Automation settings",
//			position = 3
//	)
//	String automationSettings = "auto";

//	@ConfigItem(
//			position = 1,
//			keyName = "enablePrayerFlicking",
//			name = "Enable Prayer Flicking",
//			description = "Controls prayer flicking before attacks",
//			section = automationSettings
//	)
//	default boolean enablePrayerFlicking() {
//		return false;
//	}
//	@Range(
//			min = 1,
//			max = 4
//	)
//	@ConfigItem(
//			position = 2,
//			keyName = "prayerOnAtTicksRemaining",
//			name = "Enable at Ticks Remaining",
//			description = "Enable prayer when x ticks remaining till attack",
//			section = automationSettings
//	)
//	default int prayerOnAtTicksRemaining() {
//		return 1;
//	}
//
//	@ConfigItem(
//			name = "Attack Prayer",
//			description = "Which prayer to use when attacking",
//			position = 3,
//			keyName = "attackPrayer",
//			section = automationSettings
//	)
//	default Prayer attackPrayer()
//	{
//		return Prayer.PIETY;
//	}
}

