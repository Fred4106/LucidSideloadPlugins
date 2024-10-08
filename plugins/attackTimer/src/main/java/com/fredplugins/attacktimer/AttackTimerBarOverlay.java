package com.fredplugins.attacktimer;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
class AttackTimerBarOverlay extends Overlay {
	private static final Color BAR_FILL_COLOR = new Color(201, 161, 28);

	private static final Color BAR_BG_COLOR = Color.black;
	private static final Dimension ATTACK_BAR_SIZE = new Dimension(30, 5);

	private final Client client;
	private final AttackTimerMetronomeConfig config;
	private final AttackTimerMetronomePlugin plugin;

	@Inject
	private AttackTimerBarOverlay(final Client client, final AttackTimerMetronomeConfig config, final AttackTimerMetronomePlugin plugin) {
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if(shouldShowBar()) {
			final int height = client.getLocalPlayer().getLogicalHeight() + config.heightOffset() - 20;
			final LocalPoint localLocation = client.getLocalPlayer().getLocalLocation();
			final Point canvasPoint = Perspective.localToCanvas(client, localLocation, client.getTopLevelWorldView().getPlane(), height);

			int denomMod = (config.barEmpties()) ? 1 : 0;
			int numerMod = (config.barFills()) ? 1 : 0;
			float ratio = (float) (plugin.getTicksUntilNextAttack() - numerMod) / (float) (plugin.getWeaponPeriod() - denomMod);
			if(!config.barDirection()) {
				ratio = Math.max(1.0f - ratio, 0f);
			}

			// Draw bar
			final int barX = canvasPoint.getX() - 15;
			final int barY = canvasPoint.getY();
			final int barWidth = ATTACK_BAR_SIZE.width;
			final int barHeight = ATTACK_BAR_SIZE.height;

			// Restricted by the width to prevent the bar from being too long while you are boosted above your real prayer level.
			final int progressFill = (int) Math.ceil(Math.min((barWidth * ratio), barWidth));

			graphics.setColor(BAR_BG_COLOR);
			graphics.fillRect(barX, barY, barWidth, barHeight);
			graphics.setColor(BAR_FILL_COLOR);
			graphics.fillRect(barX, barY, progressFill, barHeight);
		}
		return null;
	}

	private boolean shouldShowBar() {
//		shouldShowBar = config.showBar() && plugin.isAttackCooldownPending();
		return config.showBar() && plugin.isAttackCooldownPending();
	}
}