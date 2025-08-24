package com.fredplugins.common.magic;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum OldRune {
	AIR(1, 556),
	WATER(2, 555),
	EARTH(3, 557),
	FIRE(4, 554),
	MIND(5, 558),
	CHAOS(6, 562),
	DEATH(7, 560),
	BLOOD(8, 565),
	COSMIC(9, 564),
	NATURE(10, 561),
	LAW(11, 563),
	BODY(12, 559),
	SOUL(13, 566),
	ASTRAL(14, 9075),
	MIST(15, 4695),
	MUD(16, 4698),
	DUST(17, 4696),
	LAVA(18, 4699),
	STEAM(19, 4694),
	SMOKE(20, 4697),
	WRATH(21, 21880);

	public final int varbitValue;
	public final int itemId;

	private static final Map<Integer, OldRune> lookupMap;

	static {
		ImmutableMap.Builder<Integer, OldRune> b = new ImmutableMap.Builder<Integer, OldRune>();
		Arrays.stream(values()).forEach(r -> {
			b.put(r.varbitValue, r);
		});
		lookupMap = b.build();
	}
	public static Optional<OldRune> getRuneFromIndex(int index) {
		return  Optional.ofNullable(lookupMap.get(index));
	}
	public static Optional<OldRune> getRuneFromItemId(int itemId) {
		return  Arrays.stream(values()).filter(r ->r.getItemId() == itemId).findFirst();
	}
}
