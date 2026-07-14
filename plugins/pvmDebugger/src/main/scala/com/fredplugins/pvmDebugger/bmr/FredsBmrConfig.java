package com.fredplugins.pvmDebugger.bmr;

import com.fredplugins.common.constants.FontStyle;
import com.fredplugins.common.constants.FontTypes;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

import java.awt.*;

@ConfigGroup(value = "FredsBmrHelper", secondaryConfig = true)
public interface FredsBmrConfig extends Config {
	@ConfigItem(
		name = "Enabled",
		description = "Is Blood Moon Rises helper enabled?",
		position = 0,
		keyName = "enabled"
	)
	default boolean enabled()
	{
		return false;
	}
	
	//<editor-fold desc="Drakan settings">
	@ConfigSection(
		name = "Drakan",
		description = "Drakan settings.",
		position = 30
	)
	String drakanSection = "Drakan";
	
	@ConfigItem(
		position = 0,
		keyName = "safeClickTiles",
		name = "Safe click tiles",
		description = "Paints your dodge tiles for the forecast combo, numbered in click order. CLICK ON THE BEAT: the next tile flashes with a pulsing outline exactly when clicking it procs the dodge roll — clicking before the flash just walks you there to stand and be hit (Drakan auto-hits anyone not rolling, anywhere). One click per flash, flares (*) included.",
		section = drakanSection
	)
	default boolean safeClickTiles()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "specialSafeSpots",
		name = "Special-attack safe spots",
		description = "Paints where to stand for the specials, with a tick countdown: front/back wave (spear at his side) → his flanks; semicircle (spear at his chest) → behind him; P3 reappear-charge → perpendicular sidestep tiles (clears automatically if the reappear turns out to be the blood barrage — pray Magic for that one).",
		section = drakanSection
	)
	default boolean specialSafeSpots()
	{
		return true;
	}

	@ConfigItem(
		position = 0,
		keyName = "lungeForecast",
		name = "Combo dodge forecast",
		description = "Predicts the full spear-combo chain from Drakan's queued forecast flashes and shows one dodge arrow per strike above him (arrow = the side to be on; * = radial AoE: hug him or get out). Appears ~2-3 ticks before the first strike.",
		section = drakanSection
	)
	default boolean lungeForecast()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "highlightAoe",
		name = "Radial AoE tiles",
		description = "During the big radial AoE, paint the danger ring red and the safe gap (hug Drakan / outside) green.",
		section = drakanSection
	)
	default boolean highlightAoe()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "prayerFlash",
		name = "Pray Magic flash",
		description = "Flash a Pray Magic warning when Drakan's blood projectiles are incoming (Phase 2+).",
		section = drakanSection
	)
	default boolean prayerFlash()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "aoeWarning",
		name = "AoE / combo banner",
		description = "Show a banner when the radial AoE or spear combo is active.",
		section = drakanSection
	)
	default boolean aoeWarning()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "showPhase",
		name = "Show phase / HP",
		description = "Show a small phase and HP indicator.",
		section = drakanSection
	)
	default boolean showPhase()
	{
		return false;
	}

	@Range(max = 900)
	@ConfigItem(
		position = 5,
		keyName = "beatDelayMs",
		name = "Beat delay (ms)",
		description = "Delays the cold→hot flash of the next click tile by this many milliseconds. Increase if the flash feels early (you roll before the strike), decrease if late.",
		section = drakanSection
	)
	default int beatDelayMs()
	{
		return 250;
	}

	@ConfigItem(
		position = 20,
		keyName = "aoeThreshold",
		name = "AoE tile threshold",
		description = "Number of blood-mark tiles above which the attack is treated as the big radial AoE.",
		section = drakanSection
	)
	default int aoeThreshold()
	{
		return 20;
	}
	//</editor-fold>

	//<editor-fold desc="Font settings">
	@ConfigSection(
		name = "Font",
		description = "Font settings",
		position = 90
	)
	String fontSection = "Font";

	@ConfigItem(
		position = 1,
		keyName = "fontType",
		name = "Font Type",
		description = "Change the overlay font",
		section = fontSection
	)
	default FontTypes fontType() {
		return FontTypes.REGULAR;
	}

	@Range(
		min = 8,
		max = 50
	)
	@ConfigItem(
		position = 2,
		keyName = "fontSize",
		name = "Font Size",
		description = "Change the overlay font size",
		section = fontSection
	)
	default int fontSize() {
		return 16;
	}

	@ConfigItem(
		name = "Font style",
		description = "Bold/Italics/Plain",
		position = 3,
		keyName = "fontStyle",
		section = fontSection
	)
	default FontStyle fontStyle()
	{
		return FontStyle.PLAIN;
	}
	//</editor-fold>

	//<editor-fold desc="Colors settings">
	@ConfigSection(
		name = "Colors",
		description = "Color settings.",
		position = 100
	)
	String colorsSection = "Colors";

	@ConfigItem(
		keyName = "bossColor",
		name = "Highlight Boss Color",
		description = "Color of the boss overlay highlight.",
		position = 0,
		section = colorsSection
	)
	@Alpha
	default Color bossColor() {
		return new Color(255, 0, 255, 70);
	}

	@Alpha
	@ConfigItem(
		position = 10,
		keyName = "dangerColor",
		name = "Danger colour",
		description = "Colour for danger tiles.",
		section = colorsSection
	)
	default Color dangerColor()
	{
		return new Color(255, 0, 0, 70);
	}

	@Alpha
	@ConfigItem(
		position = 11,
		keyName = "safeColor",
		name = "Safe colour",
		description = "Colour for safe tiles.",
		section = colorsSection
	)
	default Color safeColor()
	{
		return new Color(0, 230, 0, 70);
	}


	@Alpha
	@ConfigItem(
		position = 12,
		keyName = "p3SafeColor",
		name = "P3 Safe colour",
		description = "Colour for phase 3 dash attack safe tiles.",
		section = colorsSection
	)
	default Color p3safeColor()
	{
		return new Color(0, 0, 230, 70);
	}

	//</editor-fold>
}
