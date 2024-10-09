package com.fredplugins.mixology;

import com.google.common.collect.ImmutableList;

public enum MixType {
	None(54914, 54915, 54916),
	Mox(54907, 54910, 54913),
	Lye(54906, 54909, 54912),
	Aga(54905, 54908, 54911);

	final static ImmutableList<MixType> realValues = ImmutableList.of(Mox, Lye, Aga);

	final ImmutableList<Integer> pedestal_ids;

	MixType(int id1, int id2, int id3) {
		this.pedestal_ids = ImmutableList.of(id1, id2, id3);
	}

//	public static MixType fromVarbit(int varbit) {
//		return books.get(varbit);
//	}
}
