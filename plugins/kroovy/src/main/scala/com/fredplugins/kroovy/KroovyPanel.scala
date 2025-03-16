package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.ui.{ImageElement, ItemIconSource, LayoutRenderElement, LineElement, SplitElement, TextElement}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.Client
import net.runelite.client.callback.ClientThread
import net.runelite.client.game.ItemManager
import net.runelite.client.ui.overlay.components.{ComponentOrientation, LayoutableRenderableEntity, LineComponent, SplitComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import scala.Seq
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*

@Singleton
class KroovyPanel @Inject()(plugin: KroovyPlugin) extends OverlayPanel(plugin) {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: KroovyConfig = null
	@Inject val itemManager: ItemManager = null

	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	type LineData = (String, String) | String
	private def lines: Seq[LayoutRenderElement] = {
//		val mixCodeLine: LineData = "Mix" -> TileObjects.search().withId(55392, 55393, 55394).result().asScala.toList.sortBy(_.getId).flatMap {
//			to => SMixType.fromPedestal(to)
//		}.foldLeft("")((j, j2) => s"${j}${j2.letter}")

//		val benchesLines: Seq[LineData] = List(SProcessType.Alembic -> plugin.alembicPotionType, SProcessType.Agitator -> plugin.agitatorPotionType, SProcessType.Retort -> plugin.retortPotionType).map {
//			case (z, zz) => (s"${z}", zz.map(zzz => s"${zzz}").getOrElse("Empty"))
//		}.map[LineData] {
//			case (processesTypeStr, brewStr) => processesTypeStr -> brewStr
//		}.prepended("Toolbenches")
//		val ordersLines: Seq[LineData] = plugin.potionOrders.pipe(x => List(x._1, x._2, x._3).zipWithIndex).map[LineData] {
//			case ((pt, br), idx) => s"Order ${idx + 1}" -> s"${pt} ${br}"
////			case (processType, brew) =>
////			case (, idx) => s"Order ${idx + 1}" -> s"${pt} ${br}"
//		}.prepended("Orders")
		val inventoryLines: Seq[LayoutRenderElement] = plugin.inventorySnapshot
			.map[LayoutRenderElement] {
				case (idx, id, qty) => {
					val compo = clientThread.runOnClientThread(() => itemManager.getItemComposition(id))
					val leftText = TextElement(s"${idx}", Color.BLUE)
					val rightText = TextElement(s"(${id}, ${qty})")
					val text = LineElement(leftText, rightText)
					val icon = ImageElement(ItemIconSource(id, qty, compo.isStackable()))
					SplitElement(icon, text)
				}
			}.prepended(TextElement("Inventory"))

//		val debugLines: Seq[LineData] = List(
//			SProcessType.Retort -> (plugin.previousRetortProgess, if(plugin.previousRetortProgess < 17 && plugin.previousRetortProgess > 0) 1 else 0),
//			SProcessType.Agitator -> (plugin.previousAgitatorProgess, plugin.agitatorQuickActionTicks),
//			SProcessType.Alembic -> (plugin.previousAlembicProgress, plugin.alembicQuickActionTicks),
//		)
//			.map[LineData] {
//				case (processType, (prog, ticks)) => s"${processType}" -> s"${prog} | ${ticks}"
//			}.prepended("debug")
//		log.info("benchesLines: {}", benchesLines)
		List(/*mixCodeLine, benchesLines, ordersLines, */inventoryLines/*, debugLines*/).flatten
	}
	override def render(graphics: Graphics2D): Dimension = {
//		if (plugin.inLab) {
		lines.prepended(TextElement("Kroovy", Color.GREEN)).map(_.create).foreach(lre => panelComponent.getChildren.add(lre))
//		List[LayoutableRenderableEntity|Seq[LayoutableRenderableEntity]](
//			TitleComponent.builder.text("Kroovy").color(Color.GREEN).build,
//			lines.map {
//					case (left, right) => LineComponent.builder.left(left).right(right).build
//					case line: String => TitleComponent.builder.text(line).build
//				}
//		).flatMap {
//			case x: LayoutableRenderableEntity => List(x)
//			case x: Seq[LayoutableRenderableEntity] => x
//		}.foreach(u => panelComponent.getChildren.add(u))
////		}
		super.render(graphics)
	}
}
