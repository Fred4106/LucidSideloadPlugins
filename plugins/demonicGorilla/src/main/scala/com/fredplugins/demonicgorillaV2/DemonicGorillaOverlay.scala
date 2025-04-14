package com.fredplugins.demonicgorillaV2

import com.fredplugins.demonicgorillaV2.AttackStyle.Magic
import com.fredplugins.demonicgorillaV2.AttackStyle.Melee
import com.fredplugins.demonicgorillaV2.AttackStyle.Ranged
import com.fredplugins.demonicgorillaV2.DemonicGorillaOverlay.OVERLAY_ICON_DISTANCE
import com.fredplugins.demonicgorillaV2.DemonicGorillaOverlay.OVERLAY_ICON_MARGIN
import com.fredplugins.demonicgorillaV2.DemonicGorillaOverlay.setProgressIcon
import com.google.inject.Inject
import com.google.inject.Singleton
import net.runelite.api.Client
import net.runelite.api.Perspective
import net.runelite.api.Point
import net.runelite.api.Skill
import net.runelite.client.game.SkillIconManager
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPosition

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.Arc2D
import java.awt.image.BufferedImage
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object DemonicGorillaOverlay {
	private val COLOR_ICON_BACKGROUND  = new Color(0, 0, 0, 128)
	private val COLOR_ICON_BORDER      = new Color(0, 0, 0, 255)
	private val COLOR_ICON_BORDER_FILL = new Color(219, 175, 0, 255)
	private val OVERLAY_ICON_DISTANCE  = 50
	private val OVERLAY_ICON_MARGIN    = 8

	/**
	 * @param graphics
	 * @param point
	 * @param currentPhaseIcon
	 * @param totalWidth
	 * @param bgPadding
	 * @param currentPosX
	 * @param colorIconBackground
	 * @param overlayIconDistance
	 * @param colorIconBorder
	 * @param colorIconBorderFill
	 */
	def setProgressIcon(graphics:        Graphics2D, point: Point, currentPhaseIcon: BufferedImage, totalWidth: Int,
											bgPadding:       Int,
											currentPosX:     Int, colorIconBackground: Color, overlayIconDistance: Int,
											colorIconBorder: Color, colorIconBorderFill: Color
										 ): Unit = {
		graphics.setStroke(new BasicStroke(2))
		graphics.setColor(colorIconBackground)
		graphics.fillOval(
			(point.getX - totalWidth / 2 + currentPosX - bgPadding),
			(point.getY - currentPhaseIcon.getHeight / 2 - overlayIconDistance - bgPadding),
			currentPhaseIcon.getWidth + bgPadding * 2,
			currentPhaseIcon.getHeight + bgPadding * 2
			)
		graphics.setColor(colorIconBorder)
		graphics.drawOval(
			(point.getX - totalWidth / 2 + currentPosX - bgPadding),
			(point.getY - currentPhaseIcon.getHeight / 2 - overlayIconDistance - bgPadding),
			currentPhaseIcon.getWidth + bgPadding * 2,
			currentPhaseIcon.getHeight + bgPadding * 2)
		graphics.drawImage(currentPhaseIcon,
											 (point.getX - totalWidth / 2 + currentPosX),
											 (point.getY - currentPhaseIcon.getHeight / 2 - overlayIconDistance),
											 null)
		graphics.setColor(colorIconBorderFill)
	}

}
@Singleton
class DemonicGorillaOverlay @Inject()(val plugin: DemonicGorillaV2Plugin, val client: Client) extends Overlay(plugin) {
	setPosition(OverlayPosition.DYNAMIC)
	setPriority(Overlay.PRIORITY_HIGHEST)
	setLayer(OverlayLayer.UNDER_WIDGETS)

	@Inject() private val iconManager: SkillIconManager = null

	private def getIcon(attackStyle: AttackStyle): BufferedImage = {
		Option(attackStyle).collect {
			case Melee => Skill.ATTACK
			case Ranged => Skill.RANGED
			case Magic => Skill.MAGIC
		}.map(iconManager.getSkillImage).orNull
	}

	override def render(graphics: Graphics2D): Dimension = {
		val s1 = for {
			gorilla <- plugin.getGorillas.filter(_.getInteracting != null)
			lp <- Option(gorilla.localLocation)
			rlPoint <- Option(Perspective.localToCanvas(client, lp, client.getTopLevelWorldView.getPlane, -48))
		} yield (gorilla, rlPoint)

		s1.foreach {
			case (gorilla, rlPoint) => {
				val attackStyles = gorilla.getNextPosibleAttackStyles
				val icons        = attackStyles.map(getIcon(_))
				val totalWidth   = (attackStyles.size - 1) * OVERLAY_ICON_MARGIN + icons.map(_.getWidth).sum

				inline def bgPadding = 4

				var curPosX = 0
				for (icon <- icons) {
					setProgressIcon(
						graphics,
						rlPoint, icon, totalWidth, bgPadding, curPosX,
						DemonicGorillaOverlay.COLOR_ICON_BACKGROUND,
						DemonicGorillaOverlay.OVERLAY_ICON_DISTANCE,
						DemonicGorillaOverlay.COLOR_ICON_BORDER,
						DemonicGorillaOverlay.COLOR_ICON_BORDER_FILL
					)
					val arc = new Arc2D.Double(rlPoint.getX - totalWidth / 2 + curPosX - bgPadding,
																		 rlPoint.getY - (icon.getHeight / 2).toFloat - OVERLAY_ICON_DISTANCE - bgPadding,
																		 icon.getWidth + bgPadding * 2,
																		 icon.getHeight + bgPadding * 2,
																		 90.0,
																		 -360.0 * (Attacks_per_Switch - gorilla.getAttacksUntilSwitch) /
																			 Attacks_per_Switch,
																		 Arc2D.OPEN)
					graphics.draw(arc)

					curPosX += icon.getWidth + OVERLAY_ICON_MARGIN
				}
			}
		}
		null
	}

}
