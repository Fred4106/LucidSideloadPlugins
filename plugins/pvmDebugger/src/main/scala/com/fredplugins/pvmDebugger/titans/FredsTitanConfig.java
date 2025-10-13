package com.fredplugins.pvmDebugger.titans;

import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value = FredsTitanConfig.GROUP, secondaryConfig = true)
public interface FredsTitanConfig extends Config {
	static final String GROUP = "FredsTitanHelper";

	@ConfigItem(
		name = "Enabled",
		description = "Is Royal Titans helper enabled?",
		position = 0,
		keyName = "enabled"
	)
	default boolean enabled()
	{
		return false;
	}

	//region General
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
	//endregion
	
	//region Prayers
	@ConfigSection(
		name = "Prayers",
		description = "Prayer Settings",
		position = 2
	)
	String prayersSection = "Prayers";

	@ConfigItem(
		position = 1,
		name = "Melee Prayer",
		description = "Which prayer will be activated when melee weapon is equipped.",
		keyName = "meleePrayer",
		section = prayersSection
	)
	default MeleePrayer meleePrayer()
	{
		return MeleePrayer.PIETY;
	}

	@ConfigItem(
		position = 2,
		name = "Range Prayer",
		description = "Which prayer will be activated when ranged weapon is equipped.",
		keyName = "rangePrayer",
		section = prayersSection
	)
	default RangePrayer rangePrayer()
	{
		return RangePrayer.DEAD_EYE;
	}

	@ConfigItem(
		position = 3,
		name = "Mage Prayer",
		description = "Which prayer will be activated when magic weapon is equipped.",
		keyName = "magePrayer",
		section = prayersSection
	)
	default MagePrayer magePrayer()
	{
		return MagePrayer.NONE;
	}
	//endregion

	//region Gear
	@ConfigSection(
		name = "Gear",
		description = "Gear Settings",
		position = 3
	)
	String gearSection = "Gear";

	@ConfigItem(
		position = 10,
		keyName = "meleeWeaponIds",
		name = "Melee Weapon Ids",
		description = "IDs of melee weapons separated by line, semicolon or comma.",
		section = gearSection
	)
	default String meleeWeaponIds() {
		return "";
	}

	@ConfigItem(
		position = 11,
		keyName = "meleeGearIds",
		name = "Melee Gear Ids",
		description = "IDs of all melee equipment except weapon separated by line, semicolon or comma.",
		section = gearSection
	)
	default String meleeGearIds() {
		return "";
	}

	@ConfigItem(
		position = 20,
		keyName = "rangeWeaponIds",
		name = "Ranged Weapon Ids",
		description = "IDs of ranged weapons separated by line, semicolon or comma.",
		section = gearSection
	)
	default String rangeWeaponIds() {
		return "";
	}

	@ConfigItem(
		position = 21,
		keyName = "rangeGearIds",
		name = "Ranged Gear Ids",
		description = "IDs of all ranged equipment except weapon separated by line, semicolon or comma.",
		section = gearSection
	)
	default String rangeGearIds() {
		return "";
	}

	@ConfigItem(
		position = 30,
		keyName = "magicWeaponIds",
		name = "Magic Weapon Ids",
		description = "IDs of magic weapons separated by line, semicolon or comma.",
		section = gearSection
	)
	default String magicWeaponIds() {
		return "";
	}

	@ConfigItem(
		position = 31,
		keyName = "magicGearIds",
		name = "Magic Gear Ids",
		description = "IDs of all magic equipment except weapon separated by line, semicolon or comma.",
		section = gearSection
	)
	default String magicGearIds() {
		return "";
	}
	//endregion
	interface PrayerErum {
		abstract Prayer getPrayer();
	}
	enum RangePrayer implements PrayerErum {
		NONE, SHARP_EYE(Prayer.SHARP_EYE), HAWK_EYE(Prayer.HAWK_EYE), EAGLE_EYE(Prayer.EAGLE_EYE), DEAD_EYE(Prayer.DEADEYE), RIGOUR(Prayer.RIGOUR);

		@Getter
		private final Prayer prayer;
		RangePrayer(Prayer prayer)
        {
            this.prayer = prayer;
        }
		RangePrayer()
        {
            this.prayer = null;
        }
	}

	enum MagePrayer implements PrayerErum {
		NONE, MYSTIC_WILL(Prayer.MYSTIC_WILL), MYSTIC_LORE(Prayer.MYSTIC_LORE), MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT), MYSITC_VIGOR(Prayer.MYSTIC_VIGOUR), AUGURY(Prayer.AUGURY);

		@Getter
		private final Prayer prayer;
		MagePrayer(Prayer prayer)
        {
            this.prayer = prayer;
        }
		MagePrayer() {
			this.prayer = null;
		}
	}

	enum MeleePrayer implements PrayerErum {
		NONE, BURST_OF_STRENGTH(Prayer.BURST_OF_STRENGTH), SUPERHUMAN_STRENGTH(Prayer.SUPERHUMAN_STRENGTH), ULTIMATE_STRENGTH(Prayer.ULTIMATE_STRENGTH), CHIVALRY(Prayer.CHIVALRY), PIETY(Prayer.PIETY);

		@Getter
		private final Prayer prayer;
		MeleePrayer(Prayer prayer)
        {
            this.prayer = prayer;
        }
		MeleePrayer()
        {
            this.prayer = null;
        }
    }
}
