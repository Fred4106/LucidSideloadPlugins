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
import net.runelite.api.coords.{LocalPoint, WorldArea}
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
class FredsPyramidPlunderCounterOverlay @Inject()(val client: Client, val plugin: FredsPyramidPlunderCounterPlugin, val config: FredsPyramidPlunderCounterConfig, val modelOutlineRenderer: ModelOutlineRenderer) extends Overlay(plugin) {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_SCENE)

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics
		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client

		val currentAreas: Seq[WorldArea] = plugin.currentRoom.map(_.areas).toList.flatten

		if (client.getGameState == GameState.LOGGED_IN && PyramidPlunderHelper.isInPyramidPlunder && currentAreas.nonEmpty) {
			withFont(plugin.Cache.cachedFont) {
				TileObjects.search().withinArea(currentAreas *).gameObjects().asScala.toList
					.flatMap(go => UrnStates.getUrnState(go).map(x => x -> go))
					.foreach {
						case (state, obj) => renderGameObjectOverlay(obj, state.entryName)(2, 1, state.color, false)
					}
			}
		}
		null
	}
}
