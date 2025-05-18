package com.fredplugins.common.constants;

import java.awt.*;

public enum FontStyle {
	BOLD("Bold", Font.BOLD),
	ITALIC("Italic", Font.ITALIC),
	PLAIN("Plain", Font.PLAIN);

	private final String name;
	private final int font;

	FontStyle(String name, int font) {
		this.name = name;
		this.font = font;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return this.name;
	}

	public int getFont() {
		return this.font;
	}
}
