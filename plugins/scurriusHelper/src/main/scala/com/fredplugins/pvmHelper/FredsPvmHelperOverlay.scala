package com.fredplugins.pvmHelper
import net.runelite.client.plugins.Plugin
import com.fredplugins.common.overlays.{getCanvasTextLocation, renderGameObjectOverlay, renderTileArea, renderTileOverlay, withFont}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.{OldOverlayUtil, overlays}
import com.fredplugins.pvmHelper.hunllef.HunllefLogic
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
abstract class FredsPvmHelperOverlay[P <: Plugin & BossToolTrait](val plugin: P)(using client: Client, modelOutlineRenderer: ModelOutlineRenderer) extends Overlay(plugin) {
//	val config: FredsPvmHelperConfig= plugin.getInjector.getProvider(classOf[FredsPvmHelperConfig]).get()
//	val modelOutlineRenderer: ModelOutlineRenderer= plugin.getInjector.getProvider(classOf[ModelOutlineRenderer]).get()

	override def getName: String = super.getName + plugin.getClass.getSimpleName
	val log: Logger = ShimUtils.getLogger(this.getClass.getName+s"[${plugin.getClass.getName}]", "DEBUG")

	setPosition(OverlayPosition.DYNAMIC);
	setPriority(Overlay.PRIORITY_HIGH);
	setLayer(OverlayLayer.UNDER_WIDGETS);

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics
		given Client = client

		for (elem <- plugin.tilesToPaint()) {
			OverlayUtil.renderActorOverlay(graphics, elem._1, elem._3, elem._2)
		}

//		plugin.tilesToPaint().foreach {
//			case (wp, color, str) => renderTileOverlay(wp, str, color, dashed = false)
//		}
		null
	}
}
