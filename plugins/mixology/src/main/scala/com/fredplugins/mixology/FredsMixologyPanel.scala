package com.fredplugins.mixology

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.{Client, GameObject, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.client.ui.overlay.components.{LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import javax.annotation.concurrent.Immutable
import scala.jdk.{Accumulator, IntAccumulator}
import scala.runtime.BoxedUnit

//import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import scala.jdk.OptionConverters.*
import scala.util.{Random, Try}
import scala.util.chaining.*
@Singleton
class FredsMixologyPanel @Inject()(val client: Client, plugin: FredsMixologyPlugin) extends OverlayPanel(plugin) {
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	override def render(graphics: Graphics2D): Dimension = {
		if(!plugin.isInRegion.previousValue && !plugin.isInRegion.value) return null
		panelComponent.getChildren.add(TitleComponent.builder.text("Mixology").color(Color.GREEN).build)
		if (plugin.isInRegion.value) {

			MixType.realValues.forEach(mt => {
				panelComponent.getChildren.add(
					LineComponent.builder
						.left(s"${mt}")
						.right(s"${plugin.pedistals.value.find(_._1==mt).get._2
						}")
						.build
				)
			})
		}
		super.render(graphics)
	}
}
