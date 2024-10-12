package com.fredplugins.mixology;

import com.google.common.collect.ImmutableList;
import net.runelite.api.TileObject;

import java.util.Arrays;
import java.util.Optional;

public enum MixType {
	Mox(54907, 54910, 54913),
	Lye(54906, 54909, 54912),
	Aga(54905, 54908, 54911);

	final static ImmutableList<MixType> realValues = ImmutableList.of(Mox, Lye, Aga);

	final ImmutableList<Integer> pedestal_ids;

	MixType(int id1, int id2, int id3) {
		this.pedestal_ids = ImmutableList.of(id1, id2, id3);
	}

	public static Optional<MixType> fromId(int id) {
		return Arrays.stream(values()).filter(mt -> mt.pedestal_ids.contains(id)).findFirst();
	}

}
