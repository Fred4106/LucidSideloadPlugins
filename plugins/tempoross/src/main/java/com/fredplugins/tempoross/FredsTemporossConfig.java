package com.fredplugins.tempoross;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup(FredsTemporossConfig.GroupName)
public interface FredsTemporossConfig extends Config {
	final String GroupName = "superClickHelper";

	enum TimerModes {
		OFF,
		PIE,
		TICKS,
		SECONDS
	}

	// Sections
	@ConfigSection(
			name = "Fire",
			description = "Fire section.",
			position = 0,
			closedByDefault = false
	)
	String fireSection = "Fire";

	@ConfigItem(
			keyName = "highlightFires",
			name = "Highlight Fires",
			description = "Draws a square around the fires, and shows a timer when a fire spawns, or when a fire is going to spread",
			position = 0,
			section = fireSection
	)
	default TimerModes highlightFires() {
		return TimerModes.PIE;
	}

	@ConfigItem(
			keyName = "fireColor",
			name = "Fire Color",
			description = "Color of the Fire highlight tiles",
			position = 1,
			section = fireSection
	)
	default Color fireColor() {
		return Color.ORANGE;
	}


	@ConfigItem(
			keyName = "fireStormNotification",
			name = "Storm Cloud Notification",
			description = "Notify when a storm clouds appear",
			position = 2,
			section = fireSection
	)
	default boolean fireStormNotification() {
		return false;
	}
	@ConfigSection(
			name = "Storm",
			description = "Storm section.",
			position = 1,
			closedByDefault = false
	)
	String stormSection = "Storm";
	@ConfigItem(
			keyName = "stormIntensityNotification",
			name = "Storm Intensity Notification",
			description = "Notify when The storm intensity is above 90%",
			position = 0,
			section = stormSection
	)
	default boolean stormIntensityNotification() {
		return false;
	}


	@ConfigSection(
			name = "Fishing spots",
			description = "Fishing spots section.",
			position = 2,
			closedByDefault = false
	)
	String fishSpotSection = "FishSpots";

	@ConfigItem(
			keyName = "highlightDoubleSpot",
			name = "Highlight Double Fishing Spot",
			description = "Highlights the fishing spot where you can get double fish as well as a timer when it approximately depletes",
			position = 0,
			section = fishSpotSection
	)
	default boolean highlightDoubleSpot() {
		return true;
	}

	@ConfigItem(
			keyName = "doubleSpotColor",
			name = "Double Spot Color",
			description = "Color of the Double Spot highlight tiles",
			position = 1,
			section = fishSpotSection
	)
	default Color doubleSpotColor() {
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "doubleSpotNotification",
			name = "Double Spot Notification",
			description = "Notify when a double spot appears",
			position = 2,
			section = fishSpotSection
	)
	default boolean doubleSpotNotification() {
		return false;
	}

	@ConfigSection(
			name = "Wave",
			description = "Wave section.",
			position = 3,
			closedByDefault = false
	)
	String waveSection = "Waves";

	@ConfigItem(
			keyName = "useWaveTimer",
			name = "Enable Wave Timer",
			description = "Shows a selected type of timer that indicates when the wave damage will hit on a totem pole",
			position = 0,
			section = waveSection
	)
	default TimerModes useWaveTimer() {
		return TimerModes.PIE;
	}

	@ConfigItem(
			keyName = "waveTimerColor",
			name = "Wave Timer Color",
			description = "Color of the Wave Timer when untethered",
			position = 1,
			section = waveSection
	)
	default Color waveTimerColor() {
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "tetheredColor",
			name = "Tethered Color",
			description = "Color of the Wave Timer when tethered",
			position = 2,
			section = waveSection
	)
	default Color tetheredColor() {
		return Color.GREEN;
	}

	@ConfigItem(
			keyName = "poleBrokenColor",
			name = "Broken Pole Color",
			description = "Color of the Wave Timer when the pole/mast is broken",
			position = 3,
			section = waveSection
	)
	default Color poleBrokenColor() {
		return Color.RED;
	}

	@ConfigItem(
			keyName = "waveNotification",
			name = "Wave Incoming Notification",
			description = "Notify when a wave is incoming",
			position = 4,
			section = waveSection
	)
	default boolean waveNotification() {
		return false;
	}
	@ConfigSection(
			name = "Infoboxes",
			description = "Infoboxes section.",
			position = 4,
			closedByDefault = false
	)
	String infoSection = "Infoboxes";

	@ConfigItem(
			keyName = "fishIndicator",
			name = "Show fish amount",
			description = "Shows the amount of cooked, and uncooked fish in your inventory, and how much damage that does to the boss",
			position = 0,
			section = infoSection
	)
	default boolean fishIndicator() {
		return true;
	}

	@ConfigItem(
			keyName = "damageIndicator",
			name = "Show damage",
			description = "Shows the amount of damage you can do to the boss with the fish in your inventory",
			position = 1,
			section = infoSection
	)
	default boolean damageIndicator() {
		return true;
	}

	@ConfigItem(
			keyName = "phaseIndicator",
			name = "Show phases",
			description = "Shows which phase of tempoross you're on",
			position = 2,
			section = infoSection

	)
	default boolean phaseIndicator() {
		return true;
	}
}