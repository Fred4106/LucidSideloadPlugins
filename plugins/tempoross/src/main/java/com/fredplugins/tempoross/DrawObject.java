package com.fredplugins.tempoross;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Tile;

import java.awt.*;
import java.time.Instant;

@Getter
@AllArgsConstructor
class DrawObject {
	private final Tile tile;
	@Setter
	private Instant startTime;
	private final int duration;
	@Setter
	private Color color;
}