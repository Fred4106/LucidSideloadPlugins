package com.fredplugins.attacktimer;

import com.google.common.collect.ImmutableMap;

enum Spellbook {
	STANDARD(0),
	ANCIENT(1),
	LUNAR(2),
	ARCEUUS(3);

	private static final ImmutableMap<Integer, Spellbook> books;

	static {
		ImmutableMap.Builder<Integer, Spellbook> builder = new ImmutableMap.Builder<>();

		for(Spellbook data : values()) {
			builder.put(data.id, data);
		}

		books = builder.build();
	}

	private final int id;

	Spellbook(int id) {
		this.id = id;
	}

	public static Spellbook fromVarbit(int varbit) {
		return books.get(varbit);
	}
}
