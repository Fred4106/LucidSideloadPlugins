package com.fredplugins.pvmHelper

import net.runelite.client.plugins.Plugin
import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.{Client, Perspective, Point}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition}
import org.slf4j.Logger

import java.awt.*
import java.awt.geom.Rectangle2D
import scala.compiletime.uninitialized
import scala.util.chaining.*

abstract class FredsPvmHelperOverlay[P <: Plugin & BossToolTrait](val plugin: P) extends Overlay(plugin) {
	given ModelOutlineRenderer = plugin.getInjector.getInstance(classOf[ModelOutlineRenderer])

	//	given Client = plugin.getInjector.getInstance(classOf[Client])
	override def getName: String = super.getName + plugin.getClass.getSimpleName

	val log: Logger = ShimUtils.getLogger(this.getClass.getName + s"[${plugin.getClass.getName}]", "DEBUG")
	setPosition(OverlayPosition.DYNAMIC);
	setPriority(Overlay.PRIORITY_HIGH);
	setLayer(OverlayLayer.UNDER_WIDGETS);

	def render()(using graphics: Graphics2D, client: Client, modelOutlineRenderer: ModelOutlineRenderer): Unit

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics

		given Client = plugin.client

		render()
		//		for (elem: (WorldPoint, Color, String) <- plugin.tilesToPaint()) {
		//			val (wp, c, msg) = elem
		//			renderTileOverlay(wp, msg, c, false)
		//		}

		//		plugin.tilesToPaint().foreach {
		//			case (wp, color, str) => renderTileOverlay(wp, str, color, dashed = false)
		//		}
		null
	}
}
