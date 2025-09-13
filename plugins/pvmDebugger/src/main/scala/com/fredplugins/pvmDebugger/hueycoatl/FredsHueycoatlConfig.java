package com.fredplugins.pvmDebugger.hueycoatl;

import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value = FredsHueycoatlConfig.GROUP, secondaryConfig = true)
public interface FredsHueycoatlConfig extends Config {
	static final String GROUP = "FredsHueycoatlHelper";

	@Getter
	enum CombatPotionType {
		SUPER_ATK_PLUS_STR,
		DIVINE_SUPER_ATK_PLUS_STR,
		SUPER_COMBAT,
		DIVINE_SUPER_COMBAT
	}

	@Getter
	enum ProtectPrayerType {
		PROTECT_FROM_MELEE,
		PROTECT_FROM_MAGE,
		PROTECT_FROM_MISSILE,
		NONE;

		public Prayer getPrayer() {
			switch(this) {
				case PROTECT_FROM_MAGE:
					return Prayer.PROTECT_FROM_MAGIC;
				case PROTECT_FROM_MELEE:
					return Prayer.PROTECT_FROM_MELEE;
				case PROTECT_FROM_MISSILE:
					return Prayer.PROTECT_FROM_MISSILES;
				case NONE:
				default:
					return null;
			}
		}
	}

	@Getter
	enum ThrallTier {
		LESSER,
		SUPERIOR,
		GREATER
	}
	@Getter
	enum ThrallType {
		GHOST,
		SKELETON,
		ZOMBIE,
	}

	@ConfigItem(
		name = "Enabled",
		description = "Is Hueycoatl helper enabled?",
		position = 0,
		keyName = "enabled"
	)
	default boolean enabled()
	{
		return false;
	}

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

	@ConfigItem(
		position = 5,
		keyName = "campPrayer",
		name = "Prayer to Camp",
		description = "Camps 	Protect from Mage prayer as needed.",
		section = baseSection
	)
	default ProtectPrayerType campPrayer() {
		return ProtectPrayerType.PROTECT_FROM_MISSILE;
	}

	@ConfigItem(
		position = 6,
		keyName = "autoPrayPiety",
		name = "Auto Pray Piety",
		description = "Enables the Piety prayer as needed.",
		section = baseSection
	)
	default boolean autoPrayPiety() {
		return true;
	}

	@ConfigItem(
		position = 10,
		keyName = "enablePotions",
		name = "Use Combat Potion",
		description = "Enables the use of combat boosting potions during the fight as needed.",
		section = baseSection
	)
	default boolean enablePotions() {
		return true;
	}


	@ConfigItem(
		position = 11,
		keyName = "combatPotionType",
		name = "Combat Potion",
		description = "Select the combat potion to use during the fight.",
		section = baseSection
	)
	default CombatPotionType combatPotionType() {
		return CombatPotionType.SUPER_COMBAT;
	}

	@ConfigItem(
		position = 20,
		keyName = "enableThralls",
		name = "Enable Thralls",
		description = "Enables the summoning of thralls as needed during the fight.",
		section = baseSection
	)
	default boolean enableThralls() {
		return true;
	}

	@ConfigItem(
		position = 21,
		keyName = "thrallTier",
		name = "Thrall Tier",
		description = "Select the tier of thrall to use during the fight.",
		section = baseSection
	)
	default ThrallTier thrallTier() {
		return ThrallTier.GREATER;
	}

	@ConfigItem(
		position = 22,
		keyName = "thrallType",
		name = "Thrall Type",
		description = "Select the type of thrall to use during the fight.",
		section = baseSection
	)
	default ThrallType thrallType() {
		return ThrallType.GHOST;
	}
}
