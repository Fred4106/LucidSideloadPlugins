package com.fredplugins.wildyAlarm


import com.fredplugins.wildyAlarm.FlashSpeed.*
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Actor, Client, Player}
import net.runelite.client.callback.ClientThread
import net.runelite.client.ui.overlay.{Overlay, OverlayUtil}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil
import upickle.core.TraceVisitor.RootHasPath.parent

import java.awt.{Color, Dimension, Graphics2D, Rectangle}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized


@Singleton
class AlarmOverlay @Inject()(val plugin: FredsWildyAlarmPlugin, val config: FredsWildyAlarmConfig, val client: Client, val outlineRenderer: ModelOutlineRenderer) extends Overlay(plugin) {
	setPriority(0f)
//	setMovable(false)
//	setSnappable(false)
//	setDragTargetable(false)

	def renderActor(a: Player, text: String, zoffset: Int)(using g: Graphics2D): Unit = {
		outlineRenderer.drawOutline(a, 2, config.playerColor, 2)

		val poly = a.getCanvasTilePoly
		if (poly != null) OverlayUtil.renderPolygon(g, poly, config.playerColor)

		if (text != null && text.nonEmpty) {
			val textLocation = a.getCanvasTextLocation(g, text, a.getLogicalHeight + zoffset)
			if (textLocation != null) {
				val textBounds = g.getFontMetrics.getStringBounds(text, g)
				val offset = 4
				val textArea = Rectangle(textBounds.getX.toInt - offset, textBounds.getY.toInt - offset, textBounds.getWidth.toInt + offset + offset, textBounds.getHeight.toInt + offset + offset)
				val textColor = ColorUtil.colorLerp(ColorUtil.colorWithAlpha(config.playerColor, 255), Color.WHITE, .5f)
				//				val textArea = new Rectangle(textLocation.getX + (textBounds.getWidth / 2.0).toInt - , textLocation.getY, textBounds.getWidth.toInt, textBounds.getHeight.toInt)
				OverlayUtil.renderPolygon(g, textArea, new Color(255 - textColor.getRed, 255 - textColor.getGreen, 255 - textColor.getBlue, 180))
				OverlayUtil.renderTextLocation(g, textLocation, text, textColor)
			}
		}
	}

	private def renderFlash(using graphics: Graphics2D): Unit = {
		val configuredSpeed = config.flashControl
		val transparent = new Color(0, 0, 0, 0)
		configuredSpeed match {
			case OFF => graphics.setColor(transparent)
			case SOLID => graphics.setColor(config.flashColor)
			case _ =>
				graphics.setColor(
					if ((client.getGameCycle % config.flashControl.getRate) >= (config.flashControl.getRate / 2)) config.flashColor
					else transparent
				)
		}

		// Fill the rectangle using the client width and height
		graphics.fillRect(0, 0, client.getCanvasWidth, client.getCanvasHeight)
	}

	override def render(graphics: Graphics2D): Dimension = {
		//		val players =  clientThread.runOnClientThread(() => client.getTopLevelWorldView.players().asScala.toList)
		given Graphics2D = graphics
		if(plugin.shouldFlash) renderFlash
		plugin.playersToHighlight.foreach(p => {
			renderActor(p, p.getName, 12)
		})
		null.asInstanceOf[Dimension]
	}
}
