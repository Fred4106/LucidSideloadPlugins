package com.fredplugins.mixology;
import com.formdev.flatlaf.util.ColorFunctions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fredplugins.mixology.MixType.Aga;
import static com.fredplugins.mixology.MixType.Mox;
import static com.fredplugins.mixology.MixType.Lye;

public enum Brew {

	AAA("Alco-augmentator", Aga, Aga, Aga, 190),
	MMM("Mammoth-might mix", Mox, Mox, Mox, 190),
	LLL("Liplack liquor", Lye, Lye, Lye, 190),
	MMA("Mystic mana amalgam", Mox, Mox, Aga, 215),
	MML("Marley's moonlight", Mox, Mox, Lye, 240),
	AAM("Azure aura mix", Aga, Aga, Mox, 265),
	ALA("Aqualux amalgam", Aga, Lye, Aga, 290),
	MLL("Megalite liquid", Mox, Lye, Lye, 315),
	ALL("Anti-leech lotion", Aga, Lye, Lye, 340),
	MAL("Mixalot", Mox, Aga, Lye, 365);

	final String name;
	private final ImmutableMap<MixType, Integer> recipe;
	final int xp;

	Brew(String name, MixType i1, MixType i2, MixType i3, int xp){
		this.name = name;
		ImmutableMap.Builder<MixType, Integer> builder = new ImmutableMap.Builder<MixType, Integer>();
		for(MixType mixT : MixType.realValues) {
			int count = List.of(i1, i2, i3).stream().filter(a -> a == mixT).collect(Collectors.toList()).size();
			if(count > 0) {
				builder.put(mixT, count);
			}
		}
		this.recipe = builder.build();
		this.xp = xp;
	}

	public int worth(MixType tpe) {
		switch(recipe.getOrDefault(tpe, 0)) {
			case 0: return 0;
			case 1: return 10;
			case 2:
			case 3:
				return 20;
		}
		throw new RuntimeException("cant calcluate points of " + this.toString());
	}
	public int worth() {
		return MixType.realValues.stream().mapToInt(z -> worth(z)).sum();
	}
}
