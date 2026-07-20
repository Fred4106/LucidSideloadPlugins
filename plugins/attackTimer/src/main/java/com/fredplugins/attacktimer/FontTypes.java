package com.fredplugins.attacktimer;

public enum FontTypes {
	REGULAR("RS Regular"),
	ARIAL("Arial"),
	BOLD("Bold"),
	CAMBRIA("Cambria"),
	ROCKWELL("Rockwell"),
	SEGOE_UI("Segoe Ui"),
	TIMES_NEW_ROMAN("Times New Roman"),
	VERDANA("Verdana");

	private final String name;

	private FontTypes(String name) {
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
