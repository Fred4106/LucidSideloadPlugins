package com.fredplugins.demonicgorilla;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class DemonicGorillaOverlay extends Overlay {

	private static final Color COLOR_ICON_BACKGROUND = new Color(0, 0, 0, 128);
	private static final Color COLOR_ICON_BORDER = new Color(0, 0, 0, 255);
	private static final Color COLOR_ICON_BORDER_FILL = new Color(219, 175, 0, 255);
	private static final int OVERLAY_ICON_DISTANCE = 50;
	private static final int OVERLAY_ICON_MARGIN = 8;

	private final Client client;
	private final ChaosDemonicGorilla plugin;

	@Inject
	private SkillIconManager iconManager;

	@Inject
	public DemonicGorillaOverlay(final Client client, final ChaosDemonicGorilla plugin) {
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	private BufferedImage getIcon(DemonicGorilla.AttackStyle attackStyle) {
		switch (attackStyle) {
			case MELEE:
				return iconManager.getSkillImage(Skill.ATTACK);
			case RANGED:
				return iconManager.getSkillImage(Skill.RANGED);
			case MAGIC:
				return iconManager.getSkillImage(Skill.MAGIC);
			default:
				break;
		}
		return null;
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		for (DemonicGorilla gorilla : plugin.getGorillas().values()) {
			if (gorilla.getNpc().getInteracting() == null) {
				continue;
			}

			LocalPoint lp = gorilla.getNpc().getLocalLocation();
			if (lp != null) {
				Point point = Perspective.localToCanvas(client, lp, client.getPlane(),
						gorilla.getNpc().getLogicalHeight() + 16);
				if (point != null) {
					point = new Point(point.getX(), point.getY());

					List<DemonicGorilla.AttackStyle> attackStyles = gorilla.getNextPosibleAttackStyles();
					List<BufferedImage> icons = new ArrayList<>();
					int totalWidth = (attackStyles.size() - 1) * OVERLAY_ICON_MARGIN;
					for (DemonicGorilla.AttackStyle attackStyle : attackStyles) {
						BufferedImage icon = getIcon(attackStyle);
						icons.add(icon);
						if (icon != null) {
							totalWidth += icon.getWidth();
						}
					}

					int bgPadding = 4;
					int currentPosX = 0;
					for (BufferedImage icon : icons) {
						setProgressIcon(graphics, point, icon, totalWidth, bgPadding, currentPosX,
								COLOR_ICON_BACKGROUND, OVERLAY_ICON_DISTANCE, COLOR_ICON_BORDER,
								COLOR_ICON_BORDER_FILL);
						Arc2D.Double arc = new Arc2D.Double(
								point.getX() - totalWidth / 2 + currentPosX - bgPadding,
								point.getY() - (float) (icon.getHeight() / 2) - OVERLAY_ICON_DISTANCE - bgPadding,
								icon.getWidth() + bgPadding * 2,
								icon.getHeight() + bgPadding * 2,
								90.0,
								-360.0 * (DemonicGorilla.ATTACKS_PER_SWITCH
										- gorilla.getAttacksUntilSwitch()) / DemonicGorilla.ATTACKS_PER_SWITCH,
								Arc2D.OPEN);
						graphics.draw(arc);

						currentPosX += icon.getWidth() + OVERLAY_ICON_MARGIN;
					}
				}
			}
		}
		return null;
	}

	public static void setProgressIcon(Graphics2D graphics, Point point,
									   BufferedImage currentPhaseIcon, int totalWidth, int bgPadding, int currentPosX,
									   Color colorIconBackground, int overlayIconDistance, Color colorIconBorder,
									   Color colorIconBorderFill) {
		graphics.setStroke(new BasicStroke(2));
		graphics.setColor(colorIconBackground);
		graphics.fillOval(
				point.getX() - totalWidth / 2 + currentPosX - bgPadding,
				point.getY() - currentPhaseIcon.getHeight() / 2 - overlayIconDistance - bgPadding,
				currentPhaseIcon.getWidth() + bgPadding * 2,
				currentPhaseIcon.getHeight() + bgPadding * 2);

		graphics.setColor(colorIconBorder);
		graphics.drawOval(
				point.getX() - totalWidth / 2 + currentPosX - bgPadding,
				point.getY() - currentPhaseIcon.getHeight() / 2 - overlayIconDistance - bgPadding,
				currentPhaseIcon.getWidth() + bgPadding * 2,
				currentPhaseIcon.getHeight() + bgPadding * 2);

		graphics.drawImage(
				currentPhaseIcon,
				point.getX() - totalWidth / 2 + currentPosX,
				point.getY() - currentPhaseIcon.getHeight() / 2 - overlayIconDistance,
				null);

		graphics.setColor(colorIconBorderFill);
	}
}