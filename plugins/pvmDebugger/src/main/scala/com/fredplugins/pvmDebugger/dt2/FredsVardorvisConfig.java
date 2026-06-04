package com.fredplugins.pvmDebugger.dt2;

import net.runelite.api.Prayer;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup(value = "FredsVardorvisHelper", secondaryConfig = true)
public interface FredsVardorvisConfig extends Config
{
	@ConfigItem(
		name = "Enabled",
		description = "Is Vardorvis helper enabled?",
		position = 0,
		keyName = "enabled"
	)
	default boolean enabled()
	{
		return false;
	}

	@ConfigSection(
		name = "Helper Settings",
		description = "Automatic helper features.",
		position = 1
	)
	String helperSection = "Helper";

	@ConfigItem(
		name = "Auto-blood captcha",
		description = "Automatically does the blood captcha attack",
		position = 10,
		keyName = "autoBlood",
		section = helperSection
	)
	default boolean autoBlood()
	{
		return false;
	}

	@ConfigItem(
		name = "Blood Splats Per Tick",
		description = "How many blood splats to Destroy each tick",
		position = 11,
		keyName = "splatsPerTick",
		section = helperSection
	)
	default int splatsPerTick()
	{
		return 3;
	}

	@ConfigItem(
		name = "Auto-pray against attacks",
		description = "Automatically activates prayers for projectiles and swaps to protect melee afterwards",
		position = 12,
		keyName = "autoPray",
		section = helperSection
	)
	default boolean autoPray()
	{
		return false;
	}

	@ConfigItem(
		name = "Offensive Prayer",
		description = "What offensive prayer to keep enabled when first activating prayers. De-activates when vard dies",
		position = 13,
		keyName = "offensivePrayer",
		section = helperSection
	)
	default OffensivePrayer offensivePrayer()
	{
		return OffensivePrayer.NONE;
	}


	@ConfigItem(
		name = "Defensive Prayer",
		description = "What defensive prayer to keep enabled when first activating prayers. De-activates when vard dies",
		position = 14,
		keyName = "defensivePrayer",
		section = helperSection
	)
	default DefensivePrayer defensivePrayer()
	{
		return DefensivePrayer.NONE;
	}

	@ConfigItem(
		name = "Auto-dodge Axes",
		description = "Automatically dodges axes if you are on the correct tile",
		position = 15,
		keyName = "autoDodge",
		section = helperSection
	)
	default boolean autoDodge()
	{
		return false;
	}

	@ConfigItem(
		name = "Auto-attack after dodge",
		description = "Automatically attacks Vardorvis again after auto-dodging",
		position = 16,
		keyName = "autoAttack",
		section = helperSection
	)
	default boolean autoAttack()
	{
		return false;
	}

	@ConfigSection(
		name = "Colors",
		description = "Color settings.",
		position = 10
	)
	String colorsSection = "Colors";

	@ConfigItem(
		keyName = "magicProjectileColor",
		name = "Magic Projectile Color",
		description = "Color of the magic projectile.",
		position = 21,
		section = colorsSection
	)
	@Alpha
	default Color magicProjectileColor() {
		return Color.BLUE;
	}

	@ConfigItem(
		keyName = "rangedProjectileColor",
		name = "Ranged Projectile Color",
		description = "Color of the ranged projectile.",
		position = 22,
		section = colorsSection
	)
	@Alpha
	default Color rangedProjectileColor() {
		return Color.RED;
	}

	enum OffensivePrayer {
		MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT), AUGURY(Prayer.AUGURY, true),
		EAGLE_EYE(Prayer.EAGLE_EYE), RIGOUR(Prayer.RIGOUR, true),
		ULTIMATE_STRENGTH(Prayer.ULTIMATE_STRENGTH), CHIVALRY(Prayer.CHIVALRY, true), PIETY(Prayer.PIETY, true),
		NONE(null);
		private final Prayer prayer;
		private final boolean defensive;

		OffensivePrayer(Prayer prayer) {
			this(prayer, false);
		}

		OffensivePrayer(Prayer prayer, boolean defensive) {
			this.prayer = prayer;
			this.defensive = defensive;
		}

		public Prayer getPrayer() {
			return this.prayer;
		}

		public boolean isDefensive() {
			return this.defensive;
		}
	}

	enum DefensivePrayer {
		THICK_SKIN(Prayer.THICK_SKIN), ROCK_SKIN(Prayer.ROCK_SKIN), STEEL_SKIN(Prayer.STEEL_SKIN), NONE(null);

		private final Prayer prayer;

		DefensivePrayer(Prayer prayer) {
			this.prayer = prayer;
		}

		public Prayer getPrayer() {
			return this.prayer;
		}
	}
}