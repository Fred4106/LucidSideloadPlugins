package com.fredplugins.common

import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Client, GameObject, Perspective}
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil

import java.awt.{BasicStroke, Color, Font, Graphics2D, Rectangle, Shape}
import scala.util.chaining.*

package object overlays {
	def withFont[A1](font: Font)(x: => A1)(using g: Graphics2D): Unit = {
		val oldFont = g.getFont
		g.setFont(font)
		x
		g.setFont(oldFont)
	}
	def withRotation[T](loc: (Int, Int), theta: Double)(thunk: => T)(using g: Graphics2D): Unit = {
		g.rotate(theta, loc._1, loc._2)
		thunk
		g.rotate(-theta, loc._1, loc._2)
	}

	def getStroke(width: Int, dashed: Boolean): BasicStroke = new BasicStroke(width, 2, 0, 10.0f, if (dashed) Array[Float](2, .25f, 0f, 1f) else null, 0.0f)

	def renderTileArea(localPoint: LocalPoint, size: (Int, Int), scale: Double, zOffset: Int, borderColor: Color, fillAlpha: Int, dashed: Boolean)(using graphics: Graphics2D, client: Client): Unit = {
		val poly = OldOverlayUtil.getCanvasTileAreaPoly(client, localPoint, size._1, size._2, scale, zOffset)
		if(poly != null) OverlayUtil.renderPolygon(graphics, poly, borderColor, ColorUtil.colorWithAlpha(borderColor, fillAlpha), getStroke(2, dashed))
	}
	def renderMinimapArea(localPoint: LocalPoint, size: (Int, Int), scale: Double, borderColor: Color, fillAlpha: Int, dashed: Boolean)(using graphics: Graphics2D, client: Client): Unit = {
		val mmPoint = Perspective.localToMinimap(client, localPoint)
		if (mmPoint == null) return
		val realSize     = (size._1 * client.getMinimapZoom * scale).toInt -> (size._2 * client.getMinimapZoom * scale).toInt
		val r: Rectangle = new Rectangle(realSize._1, realSize._2).tap(_.setLocation(mmPoint.getX - (realSize._1 / 2), mmPoint.getY - (realSize._2 / 2)))
		val s: Shape     = r
		val rot          = client.getCameraYawTarget.doubleValue * Perspective.UNIT
		withRotation((mmPoint.getX, mmPoint.getY), rot) {
			OverlayUtil.renderPolygon(graphics, s, borderColor, ColorUtil.colorWithAlpha(borderColor, fillAlpha), getStroke(2, dashed))
		}
	}
	def renderGameObjectOverlay(gameObject: GameObject, text: String, borderColor: Color, dashed: Boolean)(using g: Graphics2D, client: Client, modelOutlineRenderer: ModelOutlineRenderer): Unit = {
		modelOutlineRenderer.drawOutline(gameObject, 3, borderColor.darker, 0)
		Option(gameObject.getConvexHull).foreach(s => {
			OverlayUtil.renderPolygon(g, s, borderColor.brighter(), ColorUtil.colorWithAlpha(borderColor, 16), getStroke(1, dashed))
		})

		val localLoc = gameObject.getLocalLocation
		renderMinimapArea(localLoc, (3, 3), 1, borderColor, 24, dashed)

		Option(text).filter(_.nonEmpty).zip(Option(gameObject.getCanvasTextLocation(g, text, 0))).foreach {
			case (str, strLoc) => {
				OverlayUtil.renderTextLocation(g, strLoc, str, Color.BLACK)
			}
		}
	}
	def renderTileOverlay(worldLocation: WorldPoint, text: String, fillColor: Color, dashed: Boolean)(using g: Graphics2D, client: Client): Unit = {
		val localPoint = LocalPoint.fromWorld(client, worldLocation)
//		val poly       = Perspective.getCanvasTilePoly(client, localPoint)
//		if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(fillColor, 255), fillColor, getStroke(2, dashed))

		renderTileArea(localPoint, (1, 1), .4, 0, ColorUtil.colorWithAlpha(fillColor, 255), fillColor.getAlpha, dashed)
		renderMinimapArea(localPoint, (1, 1), .4, ColorUtil.colorWithAlpha(fillColor, 255), fillColor.getAlpha, dashed)

		val textLocation = Perspective.getCanvasTextLocation(client, g, localPoint, text, 0)
		if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, Color.BLACK)
	}
}
