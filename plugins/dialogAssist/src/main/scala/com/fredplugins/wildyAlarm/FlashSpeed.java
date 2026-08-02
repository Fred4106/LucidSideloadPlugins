package com.fredplugins.wildyAlarm;

public enum FlashSpeed {
	OFF("Off", -1),
	SLOW("Slow", 80),
	NORMAL("Normal", 40),
	FAST("Fast", 20),
	SOLID("Solid color", -1);

	private final String text;
	private final int rate;

	FlashSpeed(String text, int rate) {
	this.text = text;
	this.rate = rate;
	}

	@Override
	public String toString() {
	return text;
	}

	public int getRate() {
	return rate;
	}
}