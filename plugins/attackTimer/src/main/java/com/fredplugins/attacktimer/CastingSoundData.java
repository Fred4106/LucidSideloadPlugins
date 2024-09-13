package com.fredplugins.attacktimer;

import com.google.common.collect.ImmutableMap;

enum CastingSoundData {
	// Keep in spellbook order then alphabetical order and oneline

	// God spells are silent
	STANDARD_BIND(101, Spellbook.STANDARD),
	STANDARD_CONFUSE(119, Spellbook.STANDARD),
	STANDARD_CRUMBLE_UNDEAD(122, Spellbook.STANDARD),
	STANDARD_CURSE(127, Spellbook.STANDARD),
	STANDARD_EARTH_BLAST(128, Spellbook.STANDARD),
	STANDARD_EARTH_BOLT(130, Spellbook.STANDARD),
	STANDARD_EARTH_STRIKE(132, Spellbook.STANDARD),
	STANDARD_EARTH_SURGE(4025, Spellbook.STANDARD),
	STANDARD_EARTH_WAVE(134, Spellbook.STANDARD),
	STANDARD_ENFEEBLE(148, Spellbook.STANDARD),
	STANDARD_ENTANGLE(151, Spellbook.STANDARD),
	STANDARD_FIRE_BLAST(155, Spellbook.STANDARD),
	STANDARD_FIRE_BOLT(157, Spellbook.STANDARD),
	STANDARD_FIRE_STRIKE(160, Spellbook.STANDARD),
	STANDARD_FIRE_SURGE(4032, Spellbook.STANDARD),
	STANDARD_IBANS_BLAST_FIRE_WAVE(162, Spellbook.STANDARD),
	STANDARD_SLAYER_DART(1718, Spellbook.STANDARD),
	STANDARD_SNARE(3003, Spellbook.STANDARD),
	STANDARD_STUN(3004, Spellbook.STANDARD),
	STANDARD_VULNERABILITY(3009, Spellbook.STANDARD),
	STANDARD_WATER_BLAST(207, Spellbook.STANDARD),
	STANDARD_WATER_BOLT(209, Spellbook.STANDARD),
	STANDARD_WATER_STRIKE(221, Spellbook.STANDARD),
	STANDARD_WATER_SURGE(4030, Spellbook.STANDARD),
	STANDARD_WATER_WAVE(213, Spellbook.STANDARD),
	STANDARD_WEAKEN(3011, Spellbook.STANDARD),
	STANDARD_WIND_BLAST(216, Spellbook.STANDARD),
	STANDARD_WIND_BOLT(218, Spellbook.STANDARD),
	STANDARD_WIND_STRIKE(220, Spellbook.STANDARD),
	STANDARD_WIND_SURGE(4028, Spellbook.STANDARD),
	STANDARD_WIND_WAVE(222, Spellbook.STANDARD),

	ANCIENT_BLOOD_SPELL(106, Spellbook.ANCIENT),
	ANCIENT_ICE_SPELL(171, Spellbook.ANCIENT),
	ANCIENT_SHADOW_SPELL(178, Spellbook.ANCIENT),
	ANCIENT_SMOKE_SPELL(183, Spellbook.ANCIENT),

	ARCEUUS_DARK_DEMONBANE(5053, Spellbook.ARCEUUS),
	ARCEUUS_GHOSTLY_GRASP(5042, Spellbook.ARCEUUS),
	ARCEUUS_INFERIOR_DEMONBANE(5038, Spellbook.ARCEUUS),
	ARCEUUS_SKELETAL_GRASP(5026, Spellbook.ARCEUUS),
	ARCEUUS_SUPERIOR_DEMONBANE(5027, Spellbook.ARCEUUS),
	ARCEUUS_UNDEAD_GRASP(5030, Spellbook.ARCEUUS);

	private static final ImmutableMap<Integer, CastingSoundData> sounds;

	static {
		ImmutableMap.Builder<Integer, CastingSoundData> builder = new ImmutableMap.Builder<>();

		for(CastingSoundData data : values()) {
			builder.put(data.id, data);
		}

		sounds = builder.build();
	}

	private final int id;
	private final Spellbook spellbook;

	CastingSoundData(int id, Spellbook b) {
		this.id = id;
		this.spellbook = b;
	}

	public static boolean isCastingSound(int id) {
		return sounds.containsKey(id);
	}

	public static Spellbook getSpellBookFromId(int id) {
		if(!sounds.containsKey(id)) {
			return null;
		}
		return sounds.get(id).spellbook;
	}
}
