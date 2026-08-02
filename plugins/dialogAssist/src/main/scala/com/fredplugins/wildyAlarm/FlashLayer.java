package com.fredplugins.wildyAlarm;

import net.runelite.client.ui.overlay.OverlayLayer;

public enum FlashLayer {
	ABOVE_SCENE("Above scene", OverlayLayer.ABOVE_SCENE),
	UNDER_WIDGETS("Under widgets", OverlayLayer.UNDER_WIDGETS),
	ABOVE_WIDGETS("Above widgets", OverlayLayer.ABOVE_WIDGETS),
	ALWAYS_ON_TOP("Always on top", OverlayLayer.ALWAYS_ON_TOP);

	private final String text;
	private final OverlayLayer layer;

	FlashLayer(String text, OverlayLayer layer) {
		this.text = text;
		this.layer = layer;
	}

	public String getText() {
		return text;
	}

	public OverlayLayer getLayer() {
		return layer;
	}

	@Override
	public String toString() {
		return text;
	}
}