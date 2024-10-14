package com.fredplugins.mixology

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.{Client, GameObject, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
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
	type LineData = (String, String) | String
	private def lines: Seq[LineData] = {
		val mixCodeLine: LineData = "Mix" -> plugin.state.pedestals.foldLeft("")((a, b) => s"$a${b.letter}")

		val benchesLines: Seq[LineData] = plugin.state.toolBenches.flatMap((pt, tpl) => tpl._2.map(pt -> _)).map[LineData] {
			case (processesType, brew) => s"$processesType" -> s"$brew"
		}.prepended("Toolbenches")
		val ordersLines: Seq[LineData] = plugin.state.orders.zipWithIndex.map[LineData] {
			case ((pt, br), idx) => s"Order ${idx + 1}" -> s"${pt} ${br}"
		}.prepended("Orders")

		List[LineData | Seq[LineData]](mixCodeLine, benchesLines, ordersLines).flatMap {
			case a: LineData => Seq(a)
			case b: Seq[LineData] => b
		}
	}
	override def render(graphics: Graphics2D): Dimension = {
		if(!plugin.state.isInRegion && !plugin.previousState.isInRegion) return null
		if (plugin.state.isInRegion) {
			List[LayoutableRenderableEntity|Seq[LayoutableRenderableEntity]](
				TitleComponent.builder.text("Mixology").color(Color.GREEN).build,
				lines.map {
						case (left, right) => LineComponent.builder.left(left).right(right).build
						case line: String => TitleComponent.builder.text(line).build
					}
			).flatMap {
				case x: LayoutableRenderableEntity => List(x)
				case x: Seq[LayoutableRenderableEntity] => x
			}.foreach(u => panelComponent.getChildren.add(u))
		}
		super.render(graphics)
	}
}
