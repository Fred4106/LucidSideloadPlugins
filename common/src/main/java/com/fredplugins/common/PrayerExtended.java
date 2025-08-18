package com.fredplugins.common;

import com.google.common.collect.ImmutableMap;
import lombok.Value;
import net.runelite.api.Prayer;
import net.runelite.api.gameval.InterfaceID;

import java.util.Optional;

public class PrayerExtended {
	@Value
	private static class PrayerExtendedData {
		int level;
		double drainRate;
		int widgetId;
		int quickPrayerIndex;

		public PrayerExtendedData(int level, double drainRate, int childId, int quickPrayerActiveIndex) {
			this.level = level;
			this.drainRate = drainRate;
			this.widgetId = InterfaceID.PRAYERBOOK << 16 | childId;
			this.quickPrayerIndex = quickPrayerActiveIndex;
		}
	}

	private static final ImmutableMap< Prayer, PrayerExtendedData> prayerExtendedData = ImmutableMap.< Prayer, PrayerExtendedData>builder()
			.put(Prayer.THICK_SKIN, new PrayerExtendedData(1, 5.0, 9, 0))
			.put(Prayer.BURST_OF_STRENGTH, new PrayerExtendedData(4, 5.0, 10, 1))
			.put(Prayer.CLARITY_OF_THOUGHT, new PrayerExtendedData(7, 5.0, 11, 2))
			.put(Prayer.SHARP_EYE, new PrayerExtendedData(8, 5.0, 27, 18))
			.put(Prayer.MYSTIC_WILL, new PrayerExtendedData(9, 5.0, 30, 19))
			.put(Prayer.ROCK_SKIN, new PrayerExtendedData(10, 10.0, 12, 3))
			.put(Prayer.SUPERHUMAN_STRENGTH, new PrayerExtendedData(13, 10.0, 13, 4))
			.put(Prayer.IMPROVED_REFLEXES, new PrayerExtendedData(16, 10.0, 14, 5))
			.put(Prayer.RAPID_RESTORE, new PrayerExtendedData(19, 5.0 / 3, 15,6))
			.put(Prayer.RAPID_HEAL, new PrayerExtendedData(22, 10.0 / 3, 16, 7))
			.put(Prayer.PROTECT_ITEM, new PrayerExtendedData(25, 10.0 / 3, 17, 8))
			.put(Prayer.HAWK_EYE, new PrayerExtendedData(26, 10.0, 28,20 ))
			.put(Prayer.MYSTIC_LORE, new PrayerExtendedData(27, 10.0, 31, 21))
			.put(Prayer.STEEL_SKIN, new PrayerExtendedData(28, 20.0, 18, 9))
			.put(Prayer.ULTIMATE_STRENGTH, new PrayerExtendedData(31, 20.0,19 ,10))
			.put(Prayer.INCREDIBLE_REFLEXES, new PrayerExtendedData(34, 20.0, 20, 11))
			.put(Prayer.PROTECT_FROM_MAGIC, new PrayerExtendedData(37, 20.0, 21, 12))
			.put(Prayer.PROTECT_FROM_MISSILES, new PrayerExtendedData(40, 20.0, 22, 13))
			.put(Prayer.PROTECT_FROM_MELEE, new PrayerExtendedData(43, 20.0, 23, 14))
			.put(Prayer.EAGLE_EYE, new PrayerExtendedData(44, 20.0, 29, 22))
			.put(Prayer.MYSTIC_MIGHT, new PrayerExtendedData(45, 20.0, 32, 23))
			.put(Prayer.RETRIBUTION, new PrayerExtendedData(46, 5.0, 24, 15))
			.put(Prayer.REDEMPTION, new PrayerExtendedData(49, 10.0, 25,16 ))
			.put(Prayer.SMITE, new PrayerExtendedData(52, 30.0, 26, 17))
			.put(Prayer.CHIVALRY, new PrayerExtendedData(60, 40.0, 34,25 ))
			.put(Prayer.DEADEYE, new PrayerExtendedData(62, 20.0, 29, 22))
			.put(Prayer.MYSTIC_VIGOUR, new PrayerExtendedData(63, 20.0, 31, 23))
			.put(Prayer.PIETY, new PrayerExtendedData(70, 40.0, 35, 26))
			.put(Prayer.PRESERVE, new PrayerExtendedData(55, 10.0 / 3, 37, 28))
			.put(Prayer.RIGOUR, new PrayerExtendedData(74, 40.0, 33, 24))
			.put(Prayer.AUGURY, new PrayerExtendedData(77, 40.0, 36, 27))
			.build();

	public static int getLevel(Prayer p) {
		return Optional.ofNullable(prayerExtendedData.get(p)).map(PrayerExtendedData::getLevel).get();//.orElse(-1);
	}

	public static double getDrainRate(Prayer p) {
		return Optional.ofNullable(prayerExtendedData.get(p)).map(PrayerExtendedData::getDrainRate).get();//.orElse(0.0);
	}

	public static int getWidgetId(Prayer p) {
		return Optional.ofNullable(prayerExtendedData.get(p)).map(PrayerExtendedData::getWidgetId).get();
	}

	public static int getQuickPrayerIndex(Prayer p) {
		return Optional.ofNullable(prayerExtendedData.get(p)).map(PrayerExtendedData::getQuickPrayerIndex).get();
	}
}
