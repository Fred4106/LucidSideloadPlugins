package com.fredplugins.tempoross

import com.fredplugins.common.overlays.{renderTileOverlay, withFont}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.tempoross.Constants.{DAMAGED_TETHER_GAMEOBJECTS, DOUBLE_SPOT_MOVE_MILLIS, FIRE_GAMEOBJECTS, MAX_DISTANCE, PIE_DIAMETER, TETHER_GAMEOBJECTS}
import com.fredplugins.tempoross.FredsTemporossConfig.TimerModes
import com.google.inject.{Inject, Singleton}
import net.runelite.api.coords.LocalPoint
import net.runelite.api.{Client, GameObject, NullObjectID, ObjectID, Perspective}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.TextComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.Point
//import net.runelite.api
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPosition
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.components.TextComponent

import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Polygon
import java.time.Instant
import java.util
import java.awt.{Color, Dimension, Font, Graphics2D}
@Singleton
class FredsTemporossOverlay @Inject()(val client: Client, val plugin: FredsTemporossPlugin, val config: FredsTemporossConfig, val modelOutlineRenderer: ModelOutlineRenderer) extends Overlay {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_SCENE)
	private val textComponent: TextComponent = new TextComponent

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics
		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client

		val localPlayer = client.getLocalPlayer
		if (localPlayer != null) {

			val playerLocation = localPlayer.getLocalLocation
			val now            = Instant.now

			highlightGameObjects(graphics, playerLocation, now)
			highlightNpcs(graphics, playerLocation, now)
		}
		null
	}
	private def highlightGameObjects(graphics: Graphics2D, playerLocation: LocalPoint, now: Instant): Unit = {
		val plane          = client.getPlane
		val highlightFires = config.highlightFires
		val waveTimer      = config.useWaveTimer
		FredsTemporossLogic.gameObjects.toList.foreach((`object`, drawObject) => {
			val tile = drawObject.getTile
			if ((tile.getPlane == plane) && tile.getLocalLocation.distanceTo(playerLocation) < MAX_DISTANCE) {
				val poly = `object`.getCanvasTilePoly
				if (poly != null) OverlayUtil.renderPolygon(graphics, poly, drawObject.getColor)
			}
			if (drawObject.getDuration <= 0 || `object`.getCanvasLocation == null) {
			} else if ((highlightFires != TimerModes.OFF) && FIRE_GAMEOBJECTS.contains(`object`.getId)) {
				if (tile.getLocalLocation.distanceTo(playerLocation) >= MAX_DISTANCE) {
				} else {
					if ((highlightFires == TimerModes.SECONDS) || (highlightFires == TimerModes.TICKS)) {
						var waveTimerMillis = (drawObject.getStartTime.toEpochMilli + drawObject.getDuration) - now.toEpochMilli
						//modulo to recalculate fires timer after they spread
						waveTimerMillis = ((waveTimerMillis % drawObject.getDuration) + drawObject.getDuration) % drawObject.getDuration
						renderTextElement(`object`, drawObject, waveTimerMillis, graphics, highlightFires)
					}
					else if (highlightFires == TimerModes.PIE) renderPieElement(`object`, drawObject, now, graphics)
				}
			} else if (TETHER_GAMEOBJECTS.contains(`object`.getId) || DAMAGED_TETHER_GAMEOBJECTS.contains(`object`.getId)) {
				if (tile.getLocalLocation.distanceTo(playerLocation) < MAX_DISTANCE) {
					if ((waveTimer == TimerModes.SECONDS) || (waveTimer == TimerModes.TICKS)) {
						val waveTimerMillis = (drawObject.getStartTime.toEpochMilli + drawObject.getDuration) - now.toEpochMilli
						renderTextElement(`object`, drawObject, waveTimerMillis, graphics, waveTimer)
					} else if (waveTimer == TimerModes.PIE) renderPieElement(`object`, drawObject, now, graphics)
				}
			}
			//Wave and is not OFF
		})
	}
	private def renderTextElement(gameObject: GameObject, drawObject: DrawObject, timerMillis: Long, graphics: Graphics2D, timerMode: TimerModes): Unit = {
		val timerText = if (timerMode.equals(TimerModes.SECONDS)) String.format("%.1f", timerMillis / 1000f) else String.format("%d", timerMillis / 600)
		// TICKS

		textComponent.setText(timerText)
		textComponent.setColor(drawObject.getColor)
		textComponent.setPosition(new Point(gameObject.getCanvasLocation.getX, gameObject.getCanvasLocation.getY))
		textComponent.render(graphics)
	}
	private def renderPieElement(gameObject: GameObject, drawObject: DrawObject, now: Instant, graphics: Graphics2D): Unit = {
		//modulo as the fire spreads every 24 seconds
		val percent = ((now.toEpochMilli - drawObject.getStartTime.toEpochMilli) % drawObject.getDuration) / drawObject.getDuration.asInstanceOf[Float]
		val ppc     = new ProgressPieComponent
		ppc.setBorderColor(drawObject.getColor)
		ppc.setFill(drawObject.getColor)
		ppc.setProgress(percent)
		ppc.setDiameter(PIE_DIAMETER)
		ppc.setPosition(gameObject.getCanvasLocation)
		ppc.render(graphics)
	}
	private def highlightNpcs(graphics: Graphics2D, playerLocation: LocalPoint, now: Instant): Unit = {
		FredsTemporossLogic.npcs.toList.foreach((npc, startTime) => {
			val npcComposition = npc.getComposition
			val size           = npcComposition.getSize
			val lp             = npc.getLocalLocation
			val tilePoly       = Perspective.getCanvasTileAreaPoly(client, lp, size)
			if (tilePoly != null && lp.distanceTo(playerLocation) < MAX_DISTANCE) OverlayUtil.renderPolygon(graphics, tilePoly, config.doubleSpotColor)
			if (lp.distanceTo(playerLocation) < MAX_DISTANCE) {
				//testing shows a time between 20 and 27 seconds. even though it isn't fully accurate, it is still better than nothing
				val percent = (now.toEpochMilli - startTime) / DOUBLE_SPOT_MOVE_MILLIS
				val ppc     = new ProgressPieComponent
				ppc.setBorderColor(config.doubleSpotColor)
				ppc.setFill(config.doubleSpotColor)
				ppc.setProgress(percent)
				ppc.setDiameter(PIE_DIAMETER)
				ppc.setPosition(Perspective.localToCanvas(client, lp, client.getPlane))
				ppc.render(graphics)
			}
		})
	}
}
