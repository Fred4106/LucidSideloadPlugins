package com.fredplugins.pvmDebugger.dt2;

import net.runelite.api.Prayer;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

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
		name = "Prayers",
		description = "What prayers to enable (non cb ones included for air mage)",
		position = 13,
		keyName = "extraPrayers",
		section = helperSection
	)
	default Set<ExtraPrayer> extraPrayers()
	{
		return Collections.emptySet();
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

	enum ExtraPrayer {
		THICK_SKIN(Prayer.THICK_SKIN),
		ROCK_SKIN(Prayer.ROCK_SKIN),
		STEEL_SKIN(Prayer.STEEL_SKIN),

		SHARP_EYE(Prayer.SHARP_EYE),
		HAWK_EYE(Prayer.HAWK_EYE),
		EAGLE_EYE(Prayer.EAGLE_EYE),

		MYSTIC_WILL(Prayer.MYSTIC_WILL),
		MYSTIC_LORE(Prayer.MYSTIC_LORE),
		MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT),

		BURST_OF_STRENGTH(Prayer.BURST_OF_STRENGTH),
		SUPERHUMAN_STRENGTH(Prayer.SUPERHUMAN_STRENGTH),
		ULTIMATE_STRENGTH(Prayer.ULTIMATE_STRENGTH),

		CLARITY_OF_THOUGHT(Prayer.CLARITY_OF_THOUGHT),
		IMPROVED_REFLEXES(Prayer.IMPROVED_REFLEXES),
		INCREDIBLE_REFLEXES(Prayer.INCREDIBLE_REFLEXES),

		PIETY(Prayer.PIETY),
		RAPID_RESTORE(Prayer.RAPID_RESTORE),
		RAPID_HEAL(Prayer.RAPID_HEAL),
		PROTECT_ITEM(Prayer.PROTECT_ITEM),
		PRESERVE(Prayer.PRESERVE);
		private final Prayer prayer;
		ExtraPrayer(Prayer prayer) {
			this.prayer = prayer;
		}

		public Prayer getPrayer() {
			return this.prayer;
		}

		public ExtraPrayer[] blocks() {
			switch (this) {
				case THICK_SKIN:
					return new ExtraPrayer[] {
						STEEL_SKIN,
						ROCK_SKIN,
						PIETY
					};
				case ROCK_SKIN:
					return new ExtraPrayer[] {
						THICK_SKIN,
						STEEL_SKIN,
						PIETY
					};
				case STEEL_SKIN:
					return new ExtraPrayer[] {
						THICK_SKIN,
						ROCK_SKIN,
						PIETY
					};
				case MYSTIC_LORE:
					return new ExtraPrayer[] {
						SHARP_EYE,
						HAWK_EYE,
						EAGLE_EYE,
						MYSTIC_WILL,
						MYSTIC_MIGHT,
						BURST_OF_STRENGTH,
						SUPERHUMAN_STRENGTH,
						CLARITY_OF_THOUGHT,
						IMPROVED_REFLEXES,
						ULTIMATE_STRENGTH,
						INCREDIBLE_REFLEXES,
						PIETY
					};
				case MYSTIC_WILL:
					return new ExtraPrayer[] {
						SHARP_EYE,
						HAWK_EYE,
						EAGLE_EYE,
						MYSTIC_MIGHT,
						MYSTIC_LORE,
						BURST_OF_STRENGTH,
						SUPERHUMAN_STRENGTH,
						CLARITY_OF_THOUGHT,
						IMPROVED_REFLEXES,
						ULTIMATE_STRENGTH,
						INCREDIBLE_REFLEXES,
						PIETY
					};
				case MYSTIC_MIGHT:
					return new ExtraPrayer[] {
						SHARP_EYE,
						HAWK_EYE,
						EAGLE_EYE,
						MYSTIC_WILL,
						MYSTIC_LORE,
						BURST_OF_STRENGTH,
						SUPERHUMAN_STRENGTH,
						CLARITY_OF_THOUGHT,
						IMPROVED_REFLEXES,
						ULTIMATE_STRENGTH,
						INCREDIBLE_REFLEXES,
						PIETY
					};
				case HAWK_EYE:
					return new ExtraPrayer[] {
						SHARP_EYE,
						EAGLE_EYE,
						MYSTIC_WILL,
						MYSTIC_LORE,
						MYSTIC_MIGHT,
						BURST_OF_STRENGTH,
						SUPERHUMAN_STRENGTH,
						CLARITY_OF_THOUGHT,
						IMPROVED_REFLEXES,
						ULTIMATE_STRENGTH,
						INCREDIBLE_REFLEXES,
						PIETY
					};
				case SHARP_EYE:
					return new ExtraPrayer[] {
						EAGLE_EYE,
						HAWK_EYE,
						MYSTIC_WILL,
						MYSTIC_LORE,
						MYSTIC_MIGHT,
						BURST_OF_STRENGTH,
						SUPERHUMAN_STRENGTH,
						CLARITY_OF_THOUGHT,
						IMPROVED_REFLEXES,
						ULTIMATE_STRENGTH,
						INCREDIBLE_REFLEXES,
						PIETY
					};
				case EAGLE_EYE:
					return new ExtraPrayer[] {
						SHARP_EYE,
						HAWK_EYE,
						MYSTIC_WILL,
						MYSTIC_LORE,
						MYSTIC_MIGHT,
						BURST_OF_STRENGTH,
						SUPERHUMAN_STRENGTH,
						CLARITY_OF_THOUGHT,
						IMPROVED_REFLEXES,
						ULTIMATE_STRENGTH,
						INCREDIBLE_REFLEXES,
						PIETY
					};
				case BURST_OF_STRENGTH:
				case CLARITY_OF_THOUGHT:
					return new ExtraPrayer[] {
						ULTIMATE_STRENGTH,
						SUPERHUMAN_STRENGTH,
						INCREDIBLE_REFLEXES,
						IMPROVED_REFLEXES,
						SHARP_EYE,
						HAWK_EYE,
						EAGLE_EYE,
						MYSTIC_WILL,
						MYSTIC_LORE,
						MYSTIC_MIGHT,
						PIETY
					};
				case SUPERHUMAN_STRENGTH:
				case IMPROVED_REFLEXES:
					return new ExtraPrayer[] {
						BURST_OF_STRENGTH,
						ULTIMATE_STRENGTH,
						CLARITY_OF_THOUGHT,
						INCREDIBLE_REFLEXES,
						SHARP_EYE,
						HAWK_EYE,
						EAGLE_EYE,
						MYSTIC_WILL,
						MYSTIC_LORE,
						MYSTIC_MIGHT,
						PIETY
					};
				case ULTIMATE_STRENGTH:
				case INCREDIBLE_REFLEXES:
					return new ExtraPrayer[] {
						BURST_OF_STRENGTH,
						SUPERHUMAN_STRENGTH,
						CLARITY_OF_THOUGHT,
						IMPROVED_REFLEXES,
						SHARP_EYE,
						HAWK_EYE,
						EAGLE_EYE,
						MYSTIC_WILL,
						MYSTIC_LORE,
						MYSTIC_MIGHT,
						PIETY
					};
				case PIETY:
					return new ExtraPrayer[] {
						ROCK_SKIN,
						THICK_SKIN,
						STEEL_SKIN,
						SHARP_EYE,
						HAWK_EYE,
						EAGLE_EYE,
						MYSTIC_WILL,
						MYSTIC_LORE,
						MYSTIC_MIGHT,
						BURST_OF_STRENGTH,
						SUPERHUMAN_STRENGTH,
						CLARITY_OF_THOUGHT,
						IMPROVED_REFLEXES,
						ULTIMATE_STRENGTH,
						INCREDIBLE_REFLEXES
					};

				default:
					return new ExtraPrayer[] {};
			}
		}
	}
}