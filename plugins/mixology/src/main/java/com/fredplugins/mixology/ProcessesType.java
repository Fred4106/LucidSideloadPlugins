package com.fredplugins.mixology;

import com.google.common.collect.ImmutableList;

public enum ProcessesType {
	Agitator(55390, 54881),
	Retort(55389, 54870),
	Alembic(55391, 54892);

	public final int baseId;
	public final int emptyId;

	ProcessesType(int baseId, int emptyId) {
		this.baseId = baseId;
		this.emptyId = emptyId;
	}
}
