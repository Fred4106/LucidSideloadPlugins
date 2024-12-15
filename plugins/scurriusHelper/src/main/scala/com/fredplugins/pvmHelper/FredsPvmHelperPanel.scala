package com.fredplugins.pvmHelper

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.{Client, GameObject, TileObject}
import net.runelite.client.plugins.Plugin
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import java.time.Instant
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

abstract class FredsPvmHelperPanel[P <: Plugin & BossToolTrait](val plugin: P) extends OverlayPanel(plugin) {
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	override def render(graphics: Graphics2D): Dimension = {
		def buildEntrySet(btt: BossToolTrait): Seq[LayoutableRenderableEntity] = {
			Option(btt.layoutPanel()).filter(_.nonEmpty).map(_.prepended(TitleComponent.builder.text(plugin.getName).color(Color.CYAN).build)).getOrElse(Seq.empty)
		}

		if (plugin.inArea()) {
			buildEntrySet(plugin).foreach(bte => {
				panelComponent.getChildren.add(bte)
			})
		}
		super.render(graphics)
	}
}
