package com.fredplugins.pvmHelper

import com.fredplugins.common.overlays.{getCanvasTextLocation, renderGameObjectOverlay, renderTileOverlay, withFont}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.{OldOverlayUtil, overlays}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.Perspective.localToCanvas
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Client, Perspective, Point}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.*
import java.awt.geom.Rectangle2D
import scala.compiletime.uninitialized
import scala.util.chaining.*

@Singleton
class FredsPvmHelperOverlay @Inject()(val client: Client, val config: FredsPvmHelperConfig, val modelOutlineRenderer: ModelOutlineRenderer) extends Overlay() {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_WIDGETS)
	setPriority(Overlay.PRIORITY_HIGHEST)

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics

		given ModelOutlineRenderer = modelOutlineRenderer

		given Client = client

		//		withFont(Cache.cachedFont) {
		//			plugin.getFallingCeilingToTicks.toList.foreach(in => {
		//				val wp = WorldPoint.fromLocal(client, in._1.getLocation)
		//				renderTileOverlay(wp, s"${in._2}", Color.RED, false)
		//			})
		//			plugin.getAttacks.foreach(proj => {
		//				val lp = LocalPoint(proj.getX.toInt, proj.getY.toInt, proj.getTarget.getWorldView)
		//				val wp = WorldPoint.fromLocal(client, lp)
		//				renderTileOverlay(wp, s"${proj.getId}", Color.GREEN, false)
		//			})
		//		}
		null
	}

	object Cache {
		var cachedFont: Font = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), config.getFontSize)
		var countdownFont: Font = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), (config.getFontSize * 1.5).toInt)
	}
}
