package com.fredplugins.pvmHelper

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, GameObject, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import java.time.Instant
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
@Singleton
class FredsPvmHelperPanel @Inject()(val client: Client, plugin: FredsPvmHelper) extends OverlayPanel(plugin) {
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)
		val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	override def render(graphics: Graphics2D): Dimension = {
		def buildEntrySet(btt: BossToolTrait): Seq[LayoutableRenderableEntity] = {
			Option(btt.layoutPanel()).filter(_.nonEmpty).map(_.prepended(TitleComponent.builder.text(btt.name).color(Color.CYAN).build)).getOrElse(Seq.empty)
		}
		plugin
			.bossLogics
			.filter(_.inArea())
			.foreach(bt => {
				buildEntrySet(bt).foreach(bte => {
					panelComponent.getChildren.add(bte)
				})
			})
		super.render(graphics)
	}
}
