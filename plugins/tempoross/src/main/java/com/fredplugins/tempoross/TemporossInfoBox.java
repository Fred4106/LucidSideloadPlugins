package com.fredplugins.tempoross;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TemporossInfoBox extends InfoBox {
	@Setter
	@Getter
	private String text;
	@Setter
	private String name;

	public TemporossInfoBox(BufferedImage image, Plugin plugin, String name) {
		super(image, plugin);
		this.name = name;
	}

	@Override
	public String getName() {
		return super.getName() + "_" + this.name;
	}

	public Color getTextColor() {
		return Color.WHITE;
	}
}