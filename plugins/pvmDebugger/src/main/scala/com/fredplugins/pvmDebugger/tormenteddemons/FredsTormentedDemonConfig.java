package com.fredplugins.pvmDebugger.tormenteddemons;
import lombok.Getter;
import net.runelite.client.config.*;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@ConfigGroup(value = FredsTormentedDemonConfig.GroupName, secondaryConfig = true)
public interface FredsTormentedDemonConfig extends Config { 
	static final String GroupName = "FredsTormentedDemonsHelper";
	@ConfigItem(
		name = "Enabled",
		description = "Is Tormented Demons helper enabled?",
		position = 0,
		keyName = "enabled"
	)
	default boolean enabled()
	{
		return false;
	}
	@ConfigSection(
			name = "General Settings",
			description = "Full Auto or Combat only",
			position = 0
	)
	String generalSettings = "generalSettings";

	@ConfigItem(
			keyName = "mode",
			name = "Mode",
			description = "Choose the bot mode: Full Auto or Combat Only",
			section = generalSettings,
			position = 0
	)
	default MODE mode() {
		return MODE.FULL_AUTO;
	}

	@ConfigSection(
			name = "Tormented Demon Settings",
			description = "Settings for Tormented Demon boss helper",
			position = 1
	)
	String tormentedDemonSection = "tormentedDemon";

	@ConfigItem(
			keyName = "autoPrayerSwitch",
			name = "Enable Defensive Prayer",
			description = "Automatically switch prayers for Tormented Demon.",
			section = tormentedDemonSection,
			position = 2
	)
	default boolean enableDefensivePrayer() {
		return true;
	}

	@ConfigItem(
			keyName = "enableOffensivePrayer",
			name = "Enable Offensive Prayer",
			description = "Toggle to enable or disable offensive prayer during combat",
			section = tormentedDemonSection,
			position = 3
	)
	default boolean enableOffensivePrayer() {
		return false;
	}

	@ConfigItem(
			keyName = "autoGearSwitch",
			name = "Auto Gear Switch",
			description = "Automatically switch gear for Tormented Demon.",
			section = tormentedDemonSection,
			position = 4
	)
	default boolean autoGearSwitch() {
		return true;
	}


	@ConfigItem(
			keyName = "dodgeDelay",
			name = "Dodging delay(ms)",
			description = "Change this if the dodging is not working properly",
			section = tormentedDemonSection,
			position = 5
	)
	default int dodgeDelay() {
		return 800;
	}


	@ConfigSection(
			name = "Looting",
			description = "Settings for item looting",
			position = 2
	)
	String lootingSection = "looting";

	@ConfigItem(
			keyName = "lootItems",
			name = "Loot Items",
			description = "Comma-separated list of item names to loot regardless of value",
			section = lootingSection,
			position = 0
	)
	default String lootItems() {
		return "";
	}

	@ConfigItem(
			keyName = "scatterAshes",
			name = "Scatter Ashes",
			description = "Scatter Infernal Ashes upon looting",
			section = lootingSection,
			position = 2
	)
	default boolean scatterAshes() {
		return false;
	}



	@ConfigSection(
			name = "Food and Potions",
			description = "Settings for banking and required supplies",
			position = 3
	)
	String bankingAndSuppliesSection = "bankingAndSupplies";

	@ConfigItem(
			keyName = "minEatPercent",
			name = "Minimum Health Percent",
			description = "Percentage of health below which the bot will eat food",
			section = bankingAndSuppliesSection,
			position = 0
	)
	default int minEatPercent() {
		return 50;
	}

	@ConfigItem(
			keyName = "minPrayerPercent",
			name = "Minimum Prayer Percent",
			description = "Percentage of prayer points below which the bot will drink a prayer potion",
			section = bankingAndSuppliesSection,
			position = 1
	)
	default int minPrayerPercent() {
		return 20;
	}

	@ConfigItem(
			keyName = "healthThreshold",
			name = "Health Threshold to Exit",
			description = "Minimum health percentage to stay and fight",
			section = bankingAndSuppliesSection,
			position = 2
	)
	default int healthThreshold() {
		return 30;
	}


	@ConfigItem(
			keyName = "combatPotionType",
			name = "Combat Potion Type",
			description = "Select the type of combat potion to use",
			section = bankingAndSuppliesSection,
			position = 3
	)
	default CombatPotionType combatPotionType() {
		return CombatPotionType.SUPER_COMBAT;
	}

	@ConfigItem(
			keyName = "rangingPotionType",
			name = "Ranging Potion Type",
			description = "Select the type of ranging potion to use",
			section = bankingAndSuppliesSection,
			position = 4
	)
	default RangingPotionType rangingPotionType() {
		return RangingPotionType.RANGING;
	}

	@ConfigItem(
			keyName = "boostedStatsThreshold",
			name = "% Boosted Stats Threshold",
			description = "The threshold for using a potion when the boosted stats are below the maximum.",
			section = bankingAndSuppliesSection,
			position = 5
	)
	@Range(
			min = 1,
			max = 100
	)
	default int boostedStatsThreshold() {
		return 10;
	}

	@ConfigSection(
			name = "Gear Settings",
			description = "Specify gear sets and combat styles for switching",
			position = 4,
			closedByDefault = true
	)
	String gearSettingsSection = "gearSettings";

	@ConfigItem(
			keyName = "copyGear",
			name = "Copy Gear Setup",
			description = "Specify a setup name to copy gear from your other configurations",
			section = gearSettingsSection,
			position = 0
	)
	default boolean copyGear() {
		return false;
	}

	@ConfigItem(
			keyName = "useRangeStyle",
			name = "Use Range Style",
			description = "Toggle to use range gear and style",
			section = gearSettingsSection,
			position = 1
	)
	default boolean useRangeStyle() {
		return true;
	}

	@ConfigItem(
			keyName = "rangeGear",
			name = "Range Gear",
			description = "List of items to equip for range attacks (comma separated)",
			section = gearSettingsSection,
			position = 2
	)
	default String rangeGear() {
		return " ";
	}

	@ConfigItem(
			keyName = "useMagicStyle",
			name = "Use Magic Style",
			description = "Toggle to use magic gear and style",
			section = gearSettingsSection,
			position = 3
	)
	default boolean useMagicStyle() {
		return true;
	}

	@ConfigItem(
			keyName = "magicGear",
			name = "Magic Gear",
			description = "List of items to equip for magic attacks (comma separated)",
			section = gearSettingsSection,
			position = 4
	)
	default String magicGear() {
		return " ";
	}

	@ConfigItem(
			keyName = "useMeleeStyle",
			name = "Use Melee Style",
			description = "Toggle to use melee gear and style",
			section = gearSettingsSection,
			position = 5
	)
	default boolean useMeleeStyle() {
		return true;
	}

	@ConfigItem(
			keyName = "meleeGear",
			name = "Melee Gear",
			description = "List of items to equip for melee attacks (comma separated)",
			section = gearSettingsSection,
			position = 6
	)
	default String meleeGear() {
		return " ";
	}

	@ConfigSection(
			name = "Hotkey Settings",
			description = "Specify hotkeys for swapping gear and prayers",
			position = 5,
			closedByDefault = true
	)
	String hotkeySettingsSection = "hotkeySettings";

	@ConfigItem(
		name = "Dodge Fireball Hotkey",
		description = "Hotkey to trigger fireball dodge",
		position = 0,
		keyName = "dodgeFireballHotkey",
		section = hotkeySettingsSection
	)
	default Keybind dodgeFireballHotkey()
	{
		return new Keybind(KeyEvent.VK_F, 0);
	}

	@ConfigItem(
		name = "Melee Swap Hotkey",
		description = "Hotkey to trigger a melee gear swap",
		position = 1,
		keyName = "swapMeleeGearHotkey",
		section = hotkeySettingsSection
	)
	default Keybind swapMeleeGearHotkey()
	{
		return new Keybind(KeyEvent.VK_A, 0);
	}

	@ConfigItem(
		name = "Mage Swap Hotkey",
		description = "Hotkey to trigger a magic gear swap",
		position = 2,
		keyName = "swapMageGearHotkey",
		section = hotkeySettingsSection
	)
	default Keybind swapMageGearHotkey()
	{
		return new Keybind(KeyEvent.VK_S, 0);
	}

	@ConfigItem(
		name = "Range Swap Hotkey",
		description = "Hotkey to trigger a ranged gear swap",
		position = 3,
		keyName = "swapRangeGearHotkey",
		section = hotkeySettingsSection
	)
	default Keybind swapRangeGearHotkey()
	{
		return new Keybind(KeyEvent.VK_D, 0);
	}


	enum MODE {
		FULL_AUTO,
		COMBAT_ONLY
	}

	@Getter
	enum CombatPotionType {
		SUPER_COMBAT,
		DIVINE_SUPER_COMBAT

	}

	@Getter
	enum RangingPotionType {
		RANGING,
		DIVINE_RANGING,
		BASTION
	}

}