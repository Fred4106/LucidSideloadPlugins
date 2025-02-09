package com.fredplugins.pvmHelper2

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.{Client, GameObject, TileObject}
import net.runelite.client.plugins.Plugin
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import net.runelite.client.util.Text
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import java.time.Instant
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
@Singleton
class FredsPvmHelper2Panel @Inject()(/*val client: Client, */ plugin: FredsPvmHelper2) extends OverlayPanel(plugin) {
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)

	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	override def render(graphics: Graphics2D): Dimension = {
//		if (plugin.inArea()) {
//			buildEntrySet(plugin).foreach(bte => {
//				panelComponent.getChildren.add(bte)
//			})
//		}
		super.render(graphics)
	}
}
