package com.fredplugins.attacktimer;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Map;

import static com.fredplugins.attacktimer.AttackStyle.ACCURATE;
import static com.fredplugins.attacktimer.AttackStyle.AGGRESSIVE;
import static com.fredplugins.attacktimer.AttackStyle.CASTING;
import static com.fredplugins.attacktimer.AttackStyle.CONTROLLED;
import static com.fredplugins.attacktimer.AttackStyle.DEFENSIVE;
import static com.fredplugins.attacktimer.AttackStyle.DEFENSIVE_CASTING;
import static com.fredplugins.attacktimer.AttackStyle.LONGRANGE;
import static com.fredplugins.attacktimer.AttackStyle.OTHER;
import static com.fredplugins.attacktimer.AttackStyle.RANGING;

enum WeaponType {
	TYPE_0(ACCURATE, AGGRESSIVE, null, DEFENSIVE),
	TYPE_1(ACCURATE, AGGRESSIVE, AGGRESSIVE, DEFENSIVE),
	TYPE_2(ACCURATE, AGGRESSIVE, null, DEFENSIVE),
	TYPE_3(RANGING, RANGING, null, LONGRANGE),
	TYPE_4(ACCURATE, AGGRESSIVE, CONTROLLED, DEFENSIVE),
	TYPE_5(RANGING, RANGING, null, LONGRANGE),
	TYPE_6(AGGRESSIVE, RANGING, CASTING, null),
	TYPE_7(RANGING, RANGING, null, LONGRANGE),
	TYPE_8(OTHER, AGGRESSIVE, null, null),
	TYPE_9(ACCURATE, AGGRESSIVE, CONTROLLED, DEFENSIVE),
	TYPE_10(ACCURATE, AGGRESSIVE, AGGRESSIVE, DEFENSIVE),
	TYPE_11(ACCURATE, AGGRESSIVE, AGGRESSIVE, DEFENSIVE),
	TYPE_12(CONTROLLED, AGGRESSIVE, null, DEFENSIVE),
	TYPE_13(ACCURATE, AGGRESSIVE, null, DEFENSIVE),
	TYPE_14(ACCURATE, AGGRESSIVE, AGGRESSIVE, DEFENSIVE),
	TYPE_15(CONTROLLED, CONTROLLED, CONTROLLED, DEFENSIVE),
	TYPE_16(ACCURATE, AGGRESSIVE, CONTROLLED, DEFENSIVE),
	TYPE_17(ACCURATE, AGGRESSIVE, AGGRESSIVE, DEFENSIVE),
	TYPE_18(ACCURATE, AGGRESSIVE, null, DEFENSIVE, CASTING, DEFENSIVE_CASTING),
	TYPE_19(RANGING, RANGING, null, LONGRANGE),
	TYPE_20(ACCURATE, CONTROLLED, null, DEFENSIVE),
	TYPE_21(ACCURATE, AGGRESSIVE, null, DEFENSIVE, CASTING, DEFENSIVE_CASTING),
	TYPE_22(ACCURATE, AGGRESSIVE, AGGRESSIVE, DEFENSIVE),
	TYPE_23(CASTING, CASTING, null, DEFENSIVE_CASTING),
	TYPE_24(ACCURATE, AGGRESSIVE, CONTROLLED, DEFENSIVE),
	TYPE_25(CONTROLLED, AGGRESSIVE, null, DEFENSIVE),
	TYPE_26(AGGRESSIVE, AGGRESSIVE, null, AGGRESSIVE),
	TYPE_27(ACCURATE, null, null, OTHER),
	TYPE_28(ACCURATE, ACCURATE, LONGRANGE),
	TYPE_29(ACCURATE, AGGRESSIVE, AGGRESSIVE, DEFENSIVE),
	TYPE_30(ACCURATE, AGGRESSIVE, AGGRESSIVE, DEFENSIVE); // Keris weirdness - even though its the same as 29.

	private static final Map<Integer, WeaponType> weaponTypes;

	static {
		ImmutableMap.Builder<Integer, WeaponType> builder = new ImmutableMap.Builder<>();

		for(WeaponType weaponType : values()) {
			builder.put(weaponType.ordinal(), weaponType);
		}

		weaponTypes = builder.build();
	}

	@Getter
	private final AttackStyle[] attackStyles;

	WeaponType(AttackStyle... attackStyles) {
		this.attackStyles = attackStyles;
	}

	public static WeaponType getWeaponType(int id) {
		return weaponTypes.get(id);
	}
}