/*
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
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
package com.fredplugins.demonicgorillaV2;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DemonicGorillaOverlay extends Overlay
{
	private static final Color COLOR_ICON_BACKGROUND = new Color(0, 0, 0, 128);
	private static final Color COLOR_ICON_BORDER = new Color(0, 0, 0, 255);
	private static final Color COLOR_ICON_BORDER_FILL = new Color(219, 175, 0, 255);
	private static final int OVERLAY_ICON_DISTANCE = 50;
	private static final int OVERLAY_ICON_MARGIN = 8;

	private final Client client;
	private final DemonicGorillaPlugin plugin;

	@Inject
	private SkillIconManager iconManager;
	@Inject
	private ModelOutlineRenderer modelOutlineRenderer;
	@Inject
	public DemonicGorillaOverlay(final Client client, final DemonicGorillaPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_HIGHEST);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	private BufferedImage getIcon(DemonicGorilla.AttackStyle attackStyle)
	{
		switch (attackStyle)
		{
			case MELEE:
				return iconManager.getSkillImage(Skill.ATTACK);
			case RANGED:
				return iconManager.getSkillImage(Skill.RANGED);
			case MAGIC:
				return iconManager.getSkillImage(Skill.MAGIC);
		}
		return null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (DemonicGorilla gorilla : plugin.getGorillas().values())
		{
			if (gorilla.getNpc().getInteracting() == null && gorilla.getLastTickInteracting() == null)
			{
				continue;
			}

			if(gorilla == plugin.getTargetGorilla()) {
				modelOutlineRenderer.drawOutline(gorilla.getNpc(), 4, COLOR_ICON_BORDER_FILL, 2);
			}


			LocalPoint lp = gorilla.getNpc().getLocalLocation();
			if (lp != null)
			{
				Point point = Perspective.localToCanvas(client, lp, client.getTopLevelWorldView().getPlane(), -48);
				//		,gorilla.getNpc().getLogicalHeight() - 32);
				if (point != null)
				{
//					point = new Point(point.getX(), point.getY());

					List<DemonicGorilla.AttackStyle> attackStyles = gorilla.getNextPossibleAttackStyles();
					List<BufferedImage> icons = new ArrayList<>();
					int totalWidth = (attackStyles.size() - 1) * OVERLAY_ICON_MARGIN;
					for (DemonicGorilla.AttackStyle attackStyle : attackStyles)
					{
						BufferedImage icon = getIcon(attackStyle);
						icons.add(icon);
						if (icon != null)
						{
							totalWidth += icon.getWidth();
						}
					}

					int bgPadding = 4;
					int currentPosX = 0;
					for (BufferedImage icon : icons)
					{
						double prog = ((double) DemonicGorilla.ATTACKS_PER_SWITCH - gorilla.getAttacksUntilSwitch()) / DemonicGorilla.ATTACKS_PER_SWITCH;
						setProgressIcon(graphics, point, icon, prog, totalWidth, bgPadding, currentPosX,
							COLOR_ICON_BACKGROUND, OVERLAY_ICON_DISTANCE, COLOR_ICON_BORDER, COLOR_ICON_BORDER_FILL);

						currentPosX += icon.getWidth() + OVERLAY_ICON_MARGIN;
					}
				}

				String textToShow = gorilla.debugString();
				FontMetrics fm = graphics.getFontMetrics();
				BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {2, .25f, 0f, 1f}, 0.0f);
				Rectangle2D bounds = fm.getStringBounds(textToShow, graphics);
				Point textLocation = Perspective.getCanvasTextLocation(client, graphics, lp, textToShow, 0);
				if(textLocation!= null) {
					Rectangle textBackground            = new Rectangle(textLocation.getX() +(int)  bounds.getX(), textLocation.getY() +(int) bounds.getY(), (int)(bounds.getWidth()), (int) bounds.getHeight());

					OverlayUtil.renderPolygon(graphics, textBackground, ColorUtil.colorWithAlpha(Color.BLACK, 0), ColorUtil.colorWithAlpha(Color.WHITE, 64), stroke);
					OverlayUtil.renderTextLocation(graphics, new Point(textLocation.getX(), textLocation.getY()), textToShow, Color.black);
				}
			}
		}
		return null;
	}

	public static void setProgressIcon(Graphics2D graphics, Point point, BufferedImage currentPhaseIcon, double progress, int totalWidth, int bgPadding, int currentPosX, Color colorIconBackground, int overlayIconDistance, Color colorIconBorder, Color colorIconBorderFill)
	{
		Ellipse2D.Double oval = new Ellipse2D.Double(
			point.getX() - (double) totalWidth / 2 + currentPosX - bgPadding,
			point.getY() - (double) currentPhaseIcon.getHeight() / 2 - overlayIconDistance - bgPadding,
			currentPhaseIcon.getWidth() + bgPadding * 2,
			currentPhaseIcon.getHeight() + bgPadding * 2);

//		graphics.fillOval(
//			point.getX() - totalWidth / 2 + currentPosX - bgPadding,
//			point.getY() - currentPhaseIcon.getHeight() / 2 - overlayIconDistance - bgPadding,
//			currentPhaseIcon.getWidth() + bgPadding * 2,
//			currentPhaseIcon.getHeight() + bgPadding * 2);

		graphics.setStroke(new BasicStroke(2));
		graphics.setColor(colorIconBackground);
		graphics.fill(oval);

		graphics.setColor(colorIconBorder);
		graphics.draw(oval);
//		graphics.drawOval(
//		graphics.drawOval(
//			point.getX() - totalWidth / 2 + currentPosX - bgPadding,
//			point.getY() - currentPhaseIcon.getHeight() / 2 - overlayIconDistance - bgPadding,
//			currentPhaseIcon.getWidth() + bgPadding * 2,
//			currentPhaseIcon.getHeight() + bgPadding * 2);

		graphics.drawImage(
			currentPhaseIcon,
			(int) oval.getX() + bgPadding,
			(int) oval.getY() + bgPadding,
			null);

		graphics.setColor(colorIconBorderFill);

		Arc2D.Double arc = new Arc2D.Double(
			oval.getX(),
			oval.getY(),
			oval.getWidth(),
			oval.getHeight(),
			90.0,
			-360.0 * progress,
			Arc2D.OPEN
		);

		graphics.draw(arc);
	}
}