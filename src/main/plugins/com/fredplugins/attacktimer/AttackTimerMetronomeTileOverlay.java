package com.fredplugins.attacktimer;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

public class AttackTimerMetronomeTileOverlay extends Overlay {

	private final Client client;
	private final AttackTimerMetronomeConfig config;
	private final AttackTimerMetronomePlugin plugin;

	@Inject
	public AttackTimerMetronomeTileOverlay(Client client, AttackTimerMetronomeConfig config, AttackTimerMetronomePlugin plugin) {
		super(plugin);
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		plugin.renderedState = plugin.attackState;
		if(plugin.attackState == AttackTimerMetronomePlugin.AttackState.NOT_ATTACKING) {
			return null;
		}

		if(config.showTick()) {
			if(config.fontType() == FontTypes.REGULAR) {
				graphics.setFont(new Font(FontManager.getRunescapeFont().getName(), Font.PLAIN, config.fontSize()));
			} else {
				graphics.setFont(new Font(config.fontType().toString(), Font.PLAIN, config.fontSize()));
			}

			final int height = client.getLocalPlayer().getLogicalHeight() + 20;
			final LocalPoint localLocation = client.getLocalPlayer().getLocalLocation();
			final Point playerPoint = Perspective.localToCanvas(client, localLocation, client.getTopLevelWorldView().getPlane(), height);

			// Countdown ticks instead of up.
			// plugin.tickCounter => ticksRemaining
			int ticksRemaining = plugin.getTicksUntilNextAttack();
			OverlayUtil.renderTextLocation(graphics, playerPoint, String.valueOf(ticksRemaining), config.NumberColor());
		}

		return null;
	}

	private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color, final Color fillColor, final double borderWidth) {
		if(dest == null) {
			return;
		}

		final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

		if(poly == null) {
			return;
		}

		OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
	}
}

