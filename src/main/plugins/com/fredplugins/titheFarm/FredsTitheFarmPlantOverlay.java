/*
 * Copyright (c) 2018, Unmoon <https://github.com/Unmoon>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
package com.fredplugins.titheFarm;
import com.google.inject.Inject;
import com.google.inject.Singleton;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import net.runelite.client.util.ColorUtil;

@Singleton
@Slf4j 
public class FredsTitheFarmPlantOverlay extends Overlay
{
	private final Client client;
	private final FredsTitheFarmPlugin plugin;
	private final FredsTitheFarmPluginConfig config;

	private final Map<Class<? extends SPlantState>, Color> borders = new HashMap<>();
	private final Map<Class<? extends SPlantState>, Color> fills = new HashMap<>();
	@Inject
	public FredsTitheFarmPlantOverlay(Client client, FredsTitheFarmPlugin plugin, FredsTitheFarmPluginConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
		this.config = config;
		this.client = client;
	}

	/**
	 * Updates the timer colors.
	 */
	public void updateConfig()
	{
		borders.clear();
		fills.clear();

		final Color colorUnwateredBorder = config.getColorUnwatered();
		final Color colorUnwatered = ColorUtil.colorWithAlpha(colorUnwateredBorder, (int) (colorUnwateredBorder.getAlpha() / 2.5));
		borders.put(SPlantState.Unwatered.class, colorUnwateredBorder);
		fills.put(SPlantState.Unwatered.class, colorUnwatered);

		final Color colorWateredBorder = config.getColorWatered();
		final Color colorWatered = ColorUtil.colorWithAlpha(colorWateredBorder, (int) (colorWateredBorder.getAlpha() / 2.5));
		borders.put(SPlantState.Watered.class, colorWateredBorder);
		fills.put(SPlantState.Watered.class, colorWatered);

		final Color colorGrownBorder = config.getColorGrown();
		final Color colorGrown = ColorUtil.colorWithAlpha(colorGrownBorder, (int) (colorGrownBorder.getAlpha() / 2.5));
		borders.put(SPlantState.Grown$.class, colorGrownBorder);
		fills.put(SPlantState.Grown$.class, colorGrown);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (TitheFarmPlant plant : plugin.getPlants())
		{
			if (plant.getState() instanceof SPlantState.Dead || plant.getState() == SPlantState.Grown$.MODULE$)
			{
				continue;
			}

			final LocalPoint localLocation = LocalPoint.fromWorld(client, plant.getWorldLocation());

			if (localLocation == null)
			{
				continue;
			}

			final Point canvasLocation = Perspective.localToCanvas(client, localLocation, client.getPlane());

			if (canvasLocation != null)
			{
				final ProgressPieComponent progressPieComponent = new ProgressPieComponent();
				progressPieComponent.setPosition(canvasLocation);
				progressPieComponent.setProgress(1 - plant.getPlantTimeRelative());
				progressPieComponent.setBorderColor(borders.get(plant.getState().getClass()));
				progressPieComponent.setFill(fills.get(plant.getState().getClass()));
				progressPieComponent.render(graphics);
			}
		}

		return null;
	}
}
