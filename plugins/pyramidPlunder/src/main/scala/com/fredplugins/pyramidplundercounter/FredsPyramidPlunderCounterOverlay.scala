package com.fredplugins.pyramidplundercounter

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.overlays.renderGameObjectOverlay
import com.fredplugins.common.overlays.{getCanvasTextLocation, renderGameObjectOverlayBak, renderTileOverlay, withFont}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.{OldOverlayUtil, overlays}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.GameState
import net.runelite.api.Perspective.localToCanvas
import net.runelite.api.coords.LocalPoint
import net.runelite.api.{Client, Point}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import scala.jdk.CollectionConverters.{IteratorHasAsScala, ListHasAsScala}
import java.awt.{Color, Dimension, Font, Graphics2D, Rectangle}
import scala.util.chaining.*
@Singleton
class FredsPyramidPlunderCounterOverlay @Inject()(val client: Client, val plugin: FredsPyramidPlunderCounterPlugin, val config: FredsPyramidPlunderCounterConfig, val modelOutlineRenderer: ModelOutlineRenderer, val eventbus: EventBus) extends OverlayPanel(plugin) {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.TOP_LEFT)
	setPreferredSize(new Dimension(200, 100))
//	setLayer(OverlayLayer.UNDER_WIDGETS)
//	setPriority(Overlay.PRIORITY_HIGHEST)
	object Cache {
		var cachedFont: Font = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, 	config.getFontSize)
		var countdownFont: Font = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, (config.getFontSize * 1.5).toInt)
	}


	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if(e.getGroup == FredsPyramidPlunderCounterConfig.GroupName) e.getKey match {
			case "fontSize" | "fontBold" => {
				Cache.cachedFont = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, config.getFontSize)
				Cache.countdownFont = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, (config.getFontSize * 1.5).toInt)
			}
			case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
		}
	}

	def renderOverlay(using graphics: Graphics2D, modelOutlineRenderer: ModelOutlineRenderer, client: Client): Unit = {
		if(client.getGameState == GameState.LOGGED_IN && PyramidPlunderHelper.isInPyramidPlunder) {
			TileObjects.search().gameObjects().asScala.toList
				.flatMap(go => UrnStates.getUrnState(go).map(x => x -> go))
				.foreach {
					//gameObject: GameObject, text: String)(outlineThickness:Int, feather: Int, borderColor: Color, dashed: Boolean)(
					case (state, obj) => renderGameObjectOverlay(obj, state.entryName)(2, 1, state.color, false)
				}
		}
	}

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics
		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client
		renderOverlay
//		def elems: collection.mutable.ArrayBuffer[LayoutableRenderableEntity]/*java.util.List[LayoutableRenderableEntity]*/ = panelComponent.getChildren.asScala

		plugin.currentRoom.collect(room => {
			LineComponent
				.builder
				.left(s"Room ${room}")
				.right(s"1/${room.baseRate}")
				.build
		}).foreach(panelComponent.getChildren.add(_))
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
