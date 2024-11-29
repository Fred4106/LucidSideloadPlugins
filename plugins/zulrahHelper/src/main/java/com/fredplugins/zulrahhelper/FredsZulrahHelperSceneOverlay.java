package com.fredplugins.zulrahhelper;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class FredsZulrahHelperSceneOverlay extends Overlay
{
	private final FredsZulrahHelperPlugin plugin;
	private final Client client;

	@Inject
	public FredsZulrahHelperSceneOverlay(FredsZulrahHelperPlugin plugin, Client client)
	{
		this.plugin = plugin;
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{

		List<WorldPoint> safePoints = plugin.getCurrentNode().getValue().getPoints().stream().flatMap(sl -> {
			return Optional.ofNullable(sl.getWorldPoint(plugin.startPosition)).stream();
		}).collect(Collectors.toList());

		List<WorldPoint> nextRoundSafePoints = plugin.getCurrentNode().getChildren().stream().flatMap(n -> {
			return n.getValue().getPoints().stream();
		}).flatMap(sl -> {
			return Optional.ofNullable(sl.getWorldPoint(plugin.startPosition)).stream().filter(wp -> {
				return !safePoints.contains(wp);
			});
		}).collect(Collectors.toList());
		nextRoundSafePoints.forEach(wp -> {
			renderTile(graphics, wp, wp.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0 ? Color.cyan : Color.pink);
		});
		safePoints.forEach(wp -> {
			renderTile(graphics, wp, wp.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0 ? Color.GREEN : Color.RED);
		});
//		renderTile(graphics, client.getLocalPlayer().getWorldLocation().dx(4).dy(2));
		Optional.ofNullable(plugin.startPosition).stream().forEach(wp -> {
			renderTile(graphics, wp, Color.ORANGE);
		});
		return null;
	}

	public void renderTile(Graphics2D graphics, WorldPoint wp, Color c) {
			LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), wp);
			Polygon poly = Perspective.getCanvasTilePoly(client, lp);
			if(poly != null) {
				graphics.setColor(c);
				graphics.drawPolygon(poly);
			}
	}
}
