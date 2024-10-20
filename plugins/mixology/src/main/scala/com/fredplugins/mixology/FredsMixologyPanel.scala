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
		val mixCodeLine: LineData = "Mix" -> TileObjects.search().withId(55392, 55393, 55394).result().asScala.toList.sortBy(_.getId).flatMap {
			to => SMixType.fromPedestal(to)
		}.foldLeft("")((j, j2) => s"${j}${j2.letter}")

//		val benchesLines: Seq[LineData] = plugin.state.toolBenches.flatMap((pt, tpl) => tpl._2.map(pt -> _)).map[LineData] {
//			case (processesType, brew) => s"$processesType" -> s"$brew"
//		}.prepended("Toolbenches")
		val benchesLines: Seq[LineData] = List(SProcessType.Alembic -> plugin.alembicPotionType, SProcessType.Agitator -> plugin.agitatorPotionType, SProcessType.Retort -> plugin.retortPotionType).map {
			case (z, zz) => (s"${z}", zz.map(zzz => s"${zzz}").getOrElse("Empty"))
		}.map[LineData] {
			case (processesTypeStr, brewStr) => processesTypeStr -> brewStr
		}.prepended("Toolbenches")
		val ordersLines: Seq[LineData] = plugin.potionOrders.pipe(x => List(x._1, x._2, x._3).zipWithIndex).map[LineData] {
			case ((pt, br), idx) => s"Order ${idx + 1}" -> s"${pt} ${br}"
//			case (processType, brew) =>
//			case (, idx) => s"Order ${idx + 1}" -> s"${pt} ${br}"
		}.prepended("Orders")
		val inventoryLines: Seq[LineData] = plugin.inventorySnapshot//.filter(j => SBrew.values.exists(b => b.unprocessedId == j._2 || b.processedId == j._2))
			.map[LineData] {
//				case (idx, id, qty) if SBrew.values.exists(b => b.unprocessedId == id) => s"${idx}" -> s"Unprocessed(${SBrew.values.find(b => b.unprocessedId == id).get})"
//				case (idx, id, qty) if SBrew.values.exists(b => b.processedId == id) => s"${idx}" -> s"Processed(${SBrew.values.find(b => b.processedId == id).get}, ${"Unknown"})"
				case (idx, id, qty) => s"${idx}" -> s"(${id}, ${qty})"
			}.prepended("Inventory")

		val debugLines: Seq[LineData] = List(
			SProcessType.Agitator -> (plugin.previousAgitatorProgess, plugin.agitatorQuickActionTicks),
			SProcessType.Alembic -> (plugin.previousAlembicProgress, plugin.alembicQuickActionTicks),
		)
			.map[LineData] {
				case (processType, (prog, ticks)) => s"${processType}" -> s"${prog} | ${ticks}"
			}.prepended("debug")
//		log.info("benchesLines: {}", benchesLines)
		List[LineData | Seq[LineData]](mixCodeLine, benchesLines, ordersLines, inventoryLines, debugLines).flatMap {
			case a: LineData => Seq(a)
			case b: Seq[LineData] => b
		}
	}
	override def render(graphics: Graphics2D): Dimension = {
//		if(!plugin.state.isInRegion && !plugin.previousState.isInRegion) return null
		if (plugin.inLab) {
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
