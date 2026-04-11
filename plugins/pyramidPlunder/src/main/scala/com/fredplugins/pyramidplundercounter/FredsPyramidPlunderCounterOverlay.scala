package com.fredplugins.pyramidplundercounter

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.overlays.{getCanvasTextLocation, renderGameObjectOverlay, renderGameObjectOverlayBak, renderTileOverlay, renderWallObjectOverlay, withFont}
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

		if (client.getGameState == GameState.LOGGED_IN && PyramidPlunderHelper.isInPyramidPlunder) {
			withFont(plugin.Cache.cachedFont) {
				plugin.getGameObjects.filter(go => PyramidPlunderHelper.SpeartrapMultiIds.contains(go.getId))
					.foreach {
						case (obj) => {
							val color = config.spearTrapColor()
							if (color.getAlpha > 0) {
								renderGameObjectOverlay(obj, "")(config.borderThickness(), config.borderFeather(), color)
							}
						}
					}

				plugin.getGameObjects
					.flatMap(go => UrnStates.getState(go).map(x => x -> go))
					.foreach {
						case (state, obj) => {
							val color = state match {
								case UrnStates.Closed => config.closedUrnColor()
								case UrnStates.Snake => config.snakeUrnColor()
								case UrnStates.Charmed => config.charmedUrnColor()
								case UrnStates.Opened => config.openedUrnColor()
							}
							if(color.getAlpha > 0) {
								renderGameObjectOverlay(obj, state.entryName)(config.borderThickness(), config.borderFeather(), color)
							}
						}
					}

				plugin.getWallObjects
					.flatMap(go => TombDoorStates.getState(go).map(x => x -> go))
					.foreach {
						case (state, obj) => {
							val color = state match {
								case TombDoorStates.Locked => config.lockedDoorColor()
								case TombDoorStates.Opened => config.openedDoorColor()
							}
							if (color.getAlpha > 0) {
								renderWallObjectOverlay(obj, state.entryName)(config.borderThickness(), config.borderFeather(), color)
							}
						}
					}

				plugin.getGameObjects
					.flatMap(go => GrandChestStates.getState(go).map(x => x -> go))
					.foreach((state, obj) =>{
						val color = state match {
							case GrandChestStates.Closed => config.closedChestColor()
							case GrandChestStates.Opened => config.openedChestColor()
						}
						if(color.getAlpha > 0) {
							renderGameObjectOverlay(obj, state.entryName)(config.borderThickness(), config.borderFeather(), color)
						}
					})

				plugin.getGameObjects
					.flatMap(go => SarcophagusStates.getState(go).map(x => x -> go))
					.foreach((state, obj) =>{
						val color = state match {
							case SarcophagusStates.Closed => config.closedSarcColor()
							case SarcophagusStates.Opened => config.openedSarcColor()
							case SarcophagusStates.Opening => config.openingSarcColor()
						}
						if(color.getAlpha > 0) {
							renderGameObjectOverlay(obj, state.entryName)(config.borderThickness(), config.borderFeather(), color)
						}
					})
			}
		}
		null
	}
}
