package com.fredplugins.attacktimer;

public enum AttackBarStyle {
	AUTO("Auto"),
	STANDARD("Standard"),
	HIGH_DETAIL("High Detail");

	private final String name;

	private AttackBarStyle(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return this.name;
	}
}
