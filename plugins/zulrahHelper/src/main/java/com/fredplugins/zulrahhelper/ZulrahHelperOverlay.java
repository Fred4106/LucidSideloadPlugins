/*
 * Copyright (c) 2018, Devin French <https://github.com/devinfrench>
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
package com.fredplugins.zulrahhelper;

import com.fredplugins.zulrahhelper.options.Prayer;
import com.fredplugins.zulrahhelper.options.StandLocation;
import com.fredplugins.zulrahhelper.tree.Node;
import com.fredplugins.zulrahhelper.tree.Step;
import com.google.inject.Inject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ZulrahHelperOverlay extends Overlay
{
	private final PanelComponent panelComponent = new PanelComponent();
	private FredsZulrahHelperPlugin plugin;

	@Inject
	ZulrahHelperOverlay(FredsZulrahHelperPlugin plugin)
	{
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		Node currentNode = plugin.getCurrentNode();
		if (currentNode == null)
		{return null;}
		Step currentStep = currentNode.getValue();

		panelComponent.getChildren().add(TitleComponent.builder().text("Zulrah Helper").build());
		panelComponent.getChildren().add(LineComponent.builder().left( "Step").right(currentStep.getTitle()).build());

		LineComponent.LineComponentBuilder prayerBuilder = LineComponent.builder().left("Prayer");
		if(currentStep.getPrayers().isEmpty()) {
			prayerBuilder = prayerBuilder
					.right("None")
					.rightColor((plugin.isPrayerEnabled(Prayer.RANGE) || plugin.isPrayerEnabled(Prayer.MAGIC)) ? Color.RED : Color.GREEN);
		} else {
			prayerBuilder = prayerBuilder
					.right(
							currentStep.getPrayers().stream().map(p -> p.name()).reduce("", (a, b) -> {

								if(a.length() == 0) {
									return b;
								} else if(b.length() > 0) {
									return a + " | " + b;
								} else {
									return "";
								}
							})
					)
					.rightColor(currentStep.getPrayers().stream().map(p -> plugin.isPrayerEnabled(p)).reduce(false, (a, b) -> a | b) ? Color.GREEN : Color.RED);
		}
		panelComponent.getChildren().add(prayerBuilder.build());
		panelComponent.getChildren().add(
				LineComponent.builder()
						.left("StartLocation")
						.right(
								Optional.ofNullable(plugin.startPosition).map(WorldPoint::toString).orElse("null")
						)
						.rightColor(
								Optional.ofNullable(plugin.startPosition).map(i -> Color.GREEN).orElse(Color.RED)
						)
						.build()
		);
		panelComponent.getChildren().add(
				LineComponent.builder()
						.left("StandLocation")
						.right(
								currentStep
										.getPoints()
										.stream()
										.map(p -> p.name() + "[" + plugin.distanceToTile(p) + "]")
										.reduce("", (a, b) -> {
											if(a.length() == 0) {
												return b;
											} else {
												return a + ", " + b;
											}
										})
						)
						.rightColor(
								currentStep
										.getPoints()
										.stream()
										.mapToInt(p -> plugin.distanceToTile(p))
										.min().orElse(100) == 0
										? Color.GREEN : Color.RED
						)
						.build()
		);
		return panelComponent.render(graphics);
	}
}
