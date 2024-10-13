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
import com.fredplugins.common.extensions.ObjectExtensions.*
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import scala.jdk.OptionConverters.*
import scala.util.{Random, Try}
import scala.util.chaining.*

@Singleton
class FredsMixologyPanel @Inject()(/*val client: Client, */plugin: FredsMixologyPlugin) extends OverlayPanel(plugin) {
	import plugin.given
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	override def render(graphics: Graphics2D): Dimension = {
		if(!plugin.isInRegion && !plugin.previousIsInRegion) return null
		panelComponent.getChildren.add(TitleComponent.builder.text("Mixology").color(Color.GREEN).build)
		if (plugin.isInRegion) {
			(plugin.pedestals).tap(current => {
				panelComponent.getChildren.add(
					LineComponent.builder
						.left(s"Recipe")
						.right(s"${current}")
						.build
				)
			})
			plugin.toolBenches.foreach{
				case (processesType, (tb, brewOpt)) => {
					LineComponent.builder
						.left(s"Station ${processesType}")
						.right(s"${tb.wrapped.sceneLoc} $brewOpt")
						.build.tap(panelComponent.getChildren.add(_))
				}
			}
			(0 until 3).map(plugin.getBrewType).zipWithIndex.map(j => (j._2,j._1._1, j._1._2)).foreach {
				case (idx,pt,br) => {
					LineComponent.builder
						.left(s"Order ${idx+1}")
						.right(s"${pt} ${br}")
						.build.tap(panelComponent.getChildren.add(_))
				}
			}
		}
		super.render(graphics)
	}
}
