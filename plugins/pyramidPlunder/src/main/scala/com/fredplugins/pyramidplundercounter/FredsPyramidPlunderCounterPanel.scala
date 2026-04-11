package com.fredplugins.pyramidplundercounter

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.overlays.*
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.{OldOverlayUtil, overlays}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.Perspective.localToCanvas
import net.runelite.api.coords.LocalPoint
import net.runelite.api.{Client, GameState, Point}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, ProgressPieComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.*
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.*
import scala.jdk.CollectionConverters.{IteratorHasAsScala, ListHasAsScala}
import scala.util.chaining.*

@Singleton
class FredsPyramidPlunderCounterPanel @Inject()(val client: Client, val plugin: FredsPyramidPlunderCounterPlugin, val config: FredsPyramidPlunderCounterConfig) extends OverlayPanel(plugin) {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.TOP_LEFT)
	setPreferredSize(new Dimension(200, 100))
//	setLayer(OverlayLayer.UNDER_WIDGETS)
//	setPriority(Overlay.PRIORITY_HIGHEST)

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics
		given Client = client
//		def elems: collection.mutable.ArrayBuffer[LayoutableRenderableEntity]/*java.util.List[LayoutableRenderableEntity]*/ = panelComponent.getChildren.asScala

		plugin.currentRoom.collect(room => {
			Seq(
				LineComponent
					.builder
					.left(s"Room ${room}")
					.right(s"1/${room.baseRate}")
					.build
			).appendedAll(
			room.areas.zipWithIndex.map(ra => {
				LineComponent
					.builder
					.left(s"Area[${ra._2}]")
					.right(s"${ra._1.pipe(wa => {
						s"WorldArea(x=${wa.getX}, y=${wa.getY}, width=${wa.getWidth}, height=${wa.getHeight}, plane=${wa.getPlane})"
					})}")
					.rightColor(
						if(ra._1.contains(client.getLocalPlayer.getWorldLocation)) Color.GREEN
						else Color.RED
					)
					.build
			}))
		}).toList.flatten.foreach(panelComponent.getChildren.add(_))
//		elems.addOne()
		plugin.stateLines.map{
			case (str, (vid, vvalue)) => {
				LineComponent
					.builder
					.left(s"${str}[${vid}]")
					.right(s"${vvalue}")
					.rightColor(
						if(vvalue == 0) Color.RED
						else if(vvalue == 1) Color.GREEN
						else Color.YELLOW)
					.leftColor(
						if(str.contains("URN")) new Color(0, 255, 255, 255)
						else if(str.contains("DOOR")) new Color(0, 255, 0, 255)
						else if (str.contains("SARCOPHAGUS") || str.contains("GOLDEN_CHEST")) new Color(255, 255, 0, 255)
						else new Color(255, 150, 150, 255))
					.build
			}
		}.foreach {
			case x => panelComponent.getChildren.add(x)
		}
			//			if (config.showPetChance) elems.addOne(LineComponent
//																						.builder
//																						.left("% Chance of pet:")
//																						.right(String.format("%f", plugin.petDryChance * 100))
//																						.build)
//		}
		super.render(graphics)
	}
}
