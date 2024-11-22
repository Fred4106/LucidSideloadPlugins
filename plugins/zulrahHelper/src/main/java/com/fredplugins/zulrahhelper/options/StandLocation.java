/*
 * Copyright (c) 2024, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.fredplugins.zulrahhelper.options;

import java.awt.Color;
import java.awt.Graphics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

@RequiredArgsConstructor
@Slf4j
public enum StandLocation
{

	START(19, 52),
	START_MAGMA(11, 43),
	PILLAR_1_NORTH(20, 23),
	SOUTH(41, 10),
	PILLAR_2_NORTH(62, 23),
	PILLAR_2_WEST(63, 16);

	private static final int WIDTH = 6;
	private static final int HEIGHT = 6;

	private final int x;
	private final int y;

	public void drawX(Graphics g, int px, int py)
	{
		var x = px + this.x - WIDTH / 2;
		var y = py + this.y - HEIGHT / 2;

		g.setColor(Color.WHITE);

		g.drawLine(x, y, x + WIDTH, y + HEIGHT);
		g.drawLine(x, y + HEIGHT, x + WIDTH, y);

		g.drawLine(x - 1, y, x + WIDTH - 1, y + HEIGHT);
		g.drawLine(x - 1, y + HEIGHT, x + WIDTH - 1, y);
	}

	public static WorldPoint getWorldPoint(WorldPoint zulrahStart, StandLocation sl) {
		if(zulrahStart == null) {
			return null;
		}
		switch(sl) {
			case START:
				return new WorldPoint(zulrahStart.getX() + 8, zulrahStart.getY() + 4, zulrahStart.getPlane());
			case START_MAGMA:
				return new WorldPoint(zulrahStart.getX() + 10, zulrahStart.getY() + 3, zulrahStart.getPlane());
			default:
				log.debug("Cant find worldpoint for {} starting from {}", sl, zulrahStart);
				return new WorldPoint(zulrahStart.getX(), zulrahStart.getY(), zulrahStart.getPlane());
		}
	}
}
