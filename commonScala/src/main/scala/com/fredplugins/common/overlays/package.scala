package com.fredplugins.common

import com.fredplugins.common.extensions.ObjectExtensions.*
import net.runelite.api.Actor
import net.runelite.api.NPC
import net.runelite.api.Perspective.localToCanvas
import net.runelite.api.Player
import net.runelite.api.Projectile
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Client, GameObject, Perspective, Point}
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil

import java.awt.geom.Rectangle2D
import java.awt.{BasicStroke, Color, Font, FontMetrics, Graphics2D, Rectangle, Shape}
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
	def renderGameObjectOverlayBak(gameObject: GameObject, text: String, borderColor: Color, dashed: Boolean)(using g: Graphics2D, client: Client, modelOutlineRenderer: ModelOutlineRenderer): Unit = {
		modelOutlineRenderer.drawOutline(gameObject, 3, borderColor.darker, 0)
		Option(gameObject.getConvexHull).foreach(s => {
			OverlayUtil.renderPolygon(g, s, borderColor.brighter(), ColorUtil.colorWithAlpha(borderColor, 16), getStroke(1, dashed))
		})

		val localLoc = gameObject.getLocalLocation

		renderMinimapArea(localLoc, gameObject.composition.pipe(c => c.getSizeX -> c.getSizeY), 1, borderColor, 24, dashed)

		Option(text).filter(_.nonEmpty).zip(Option(getCanvasTextLocation(localLoc, text, 0))).foreach {
			case (str, strLoc@(x, y, b)) => {
				val padding        = 5
//				val textBackground = new Rectangle(x - padding, y - padding, w + padding * 2, h + padding * 2)
//				OverlayUtil.renderPolygon(summon[Graphics2D], textBackground, Color.BLACK, ColorUtil.colorWithAlpha(Color.WHITE, 64), overlays.getStroke(2, true))
				OverlayUtil.renderTextLocation(g, new Point(x, y), str, Color.BLACK)
			}
		}
	}
	def renderGameObjectOverlay(gameObject: GameObject, text: String)(outlineThickness:Int, feather: Int, borderColor: Color, dashed: Boolean)(using g: Graphics2D, client: Client, modelOutlineRenderer: ModelOutlineRenderer): Unit = {
		modelOutlineRenderer.drawOutline(gameObject, outlineThickness, borderColor.darker, feather)
//		Option(gameObject.getConvexHull).foreach(s => {
//			OverlayUtil.renderPolygon(g, s, borderColor.brighter(), ColorUtil.colorWithAlpha(borderColor, 16), getStroke(1, dashed))
//		})

		val localLoc = gameObject.getLocalLocation
		renderMinimapArea(localLoc, gameObject.composition.pipe(c => c.getSizeX -> c.getSizeY), 1, borderColor, 24, dashed)

		Option(text).filter(_.nonEmpty).zip(Option(getCanvasTextLocation(localLoc, text, 0))).foreach {
			case (str, strLoc@(x, y, b)) => {
				val padding = 5
				//				val textBackground = new Rectangle(x - padding, y - padding, w + padding * 2, h + padding * 2)
				//				OverlayUtil.renderPolygon(summon[Graphics2D], textBackground, Color.BLACK, ColorUtil.colorWithAlpha(Color.WHITE, 64), overlays.getStroke(2, true))
				OverlayUtil.renderTextLocation(g, new Point(x, y), str, Color.BLACK)
			}
		}
	}

	def renderProjectileOverlay(projectile: Projectile, text: String)(outlineThickness: Int, feather: Int, borderColor: Color)(using g: Graphics2D, client: Client, modelOutlineRenderer: ModelOutlineRenderer): Unit = {
		val lp = LocalPoint(projectile.getX.toInt, projectile.getY.toInt, -1)
		modelOutlineRenderer.drawModelOutline(projectile.getModel, lp.getX, lp.getY, projectile.getZ.toInt, 0, outlineThickness,  borderColor, feather)
		Option(text).filter(_.nonEmpty).zip(Option(getCanvasTextLocation(lp, text, 0))).foreach {
			case (str, strLoc@(x, y, b)) => {
				val padding = 5
				val textBackground = new Rectangle(x - padding, y - padding - (b.getHeight.toInt / 2), b.getWidth.toInt + padding * 2, b.getHeight.toInt + padding * 2)
				OverlayUtil.renderPolygon(summon[Graphics2D], textBackground, Color.BLACK, ColorUtil.colorWithAlpha(Color.WHITE, 64), overlays.getStroke(2, true))
				OverlayUtil.renderTextLocation(g, new Point(x, y), str, Color.BLACK)
			}
		}
	}

	def drawActorOutline(actor: Actor, outlineW: Int, borderC: Color, feather: Int)(using modelOutlineRenderer: ModelOutlineRenderer): Unit = actor match {
		case npc: NPC => modelOutlineRenderer.drawOutline(npc, outlineW, borderC, feather)
		case player: Player => modelOutlineRenderer.drawOutline(player, outlineW, borderC, feather)
	}
	def renderActorOverlay(actor: Actor, text: String)(outlineThickness:Int, feather: Int, borderColor: Color, dashed: Boolean)(using g: Graphics2D, client: Client, modelOutlineRenderer: ModelOutlineRenderer): Unit = {
		drawActorOutline(actor, outlineThickness, borderColor.darker, feather)

		Option(actor.getConvexHull).foreach(s => {
			OverlayUtil.renderPolygon(g, s, borderColor.brighter(), ColorUtil.colorWithAlpha(borderColor, 16), getStroke(1, dashed))
		})

		val localLoc = actor.getLocalLocation
		renderMinimapArea(localLoc, actor.getWorldArea.pipe(wa => wa.getWidth -> wa.getHeight), 1, borderColor, 24, dashed)

		Option(text).filter(_.nonEmpty).zip(Option(getCanvasTextLocation(localLoc, text, 0))).foreach {
			case (str, strLoc@(x, y, b)) => {
				val padding        = 5
//				val textBackground = new Rectangle(x - padding, y - padding, w + padding * 2, h + padding * 2)
//				OverlayUtil.renderPolygon(summon[Graphics2D], textBackground, Color.BLACK, ColorUtil.colorWithAlpha(Color.WHITE, 64), overlays.getStroke(2, true))
				OverlayUtil.renderTextLocation(g, new Point(x, y), str, Color.BLACK)
			}
		}
	}

	def renderTileOverlay(worldLocation: WorldPoint, text: String, fillColor: Color, dashed: Boolean)(using g: Graphics2D, client: Client): Unit = {
		val localPoint = LocalPoint.fromWorld(client.getTopLevelWorldView, worldLocation)
//		val poly       = Perspective.getCanvasTilePoly(client, localPoint)
//		if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(fillColor, 255), fillColor, getStroke(2, dashed))
		if(localPoint != null) {
			renderTileArea(localPoint, (1, 1), .4, 0, ColorUtil.colorWithAlpha(fillColor,32), fillColor.getAlpha, dashed)
			renderMinimapArea(localPoint, (1, 1), .4, ColorUtil.colorWithAlpha(fillColor, 32), fillColor.getAlpha, dashed)

			val textLocation@(x, y, b) = getCanvasTextLocation(localPoint, text, 0)
			if (textLocation != null) {
				val padding        = 5
				val textBackground = new Rectangle(x - padding, y - padding - (b.getHeight.toInt / 2), b.getWidth.toInt + padding * 2, b.getHeight.toInt + padding * 2)
				OverlayUtil.renderPolygon(summon[Graphics2D], textBackground, Color.BLACK, ColorUtil.colorWithAlpha(Color.WHITE, 64), overlays.getStroke(2, true))
				OverlayUtil.renderTextLocation(g, new Point(x, y), text, Color.BLACK)
			}
		}

//		val textLocation = Perspective.getCanvasTextLocation(client, g, localPoint, text, 0)
	}

//	if (text == null)
//		{
//			return null;
//		}
//
//		var wv = client.getWorldView(localLocation.getWorldView());
//		if (wv == null)
//		{
//			return null;
//		}
//
//		int plane = wv.getPlane();
//
//		Point p = localToCanvas(client, localLocation, plane, zOffset);
//
//		if (p == null)
//		{
//			return null;
//		}
//
//		FontMetrics fm = graphics.getFontMetrics();
//		Rectangle2D bounds = fm.getStringBounds(text, graphics);
//		int xOffset = p.getX() - (int) (bounds.getWidth() / 2);
//
//		return new Point(xOffset, p.getY());
	def getCanvasTextLocation(localLocation: LocalPoint, text: String, zOffset: Int)(using graphics: Graphics2D, client: Client): (Int, Int, Rectangle2D) = {
		if (text == null) return null
		val wv = client.getWorldView(localLocation.getWorldView)
		if (wv == null) return null
		val plane = wv.getPlane
		val p     = localToCanvas(client, localLocation, plane, zOffset)
		if (p == null) return null
		val fm      = graphics.getFontMetrics
		val bounds  = fm.getStringBounds(text, graphics)
		val xOffset = p.getX - (bounds.getWidth / 2).toInt
		val yOffset = p.getY - (bounds.getHeight / 2).toInt + fm.getAscent
		(xOffset, yOffset, bounds)
	}
}

