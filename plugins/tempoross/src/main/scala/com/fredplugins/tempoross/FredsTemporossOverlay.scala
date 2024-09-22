package com.fredplugins.tempoross

import com.fredplugins.common.overlays.{renderTileOverlay, withFont}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.tempoross.Constants.{DAMAGED_TETHER_GAMEOBJECTS, DOUBLE_SPOT_MOVE_MILLIS, DOUBLE_SPOT_MOVE_TICKS, FIRE_DURATIONS, FIRE_GAMEOBJECTS, FISH_SPOTS, MAX_DISTANCE, PIE_DIAMETER, TETHER_GAMEOBJECTS, VARB_IS_TETHERED, WAVE_IMPACT_TICKS}
import com.fredplugins.tempoross.FredsTemporossConfig.TimerModes
import com.fredplugins.tempoross.FredsTemporossLogic.{inMinigame, plugin, waveIncomingStartTick, waveIncomingStartTime}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.coords.LocalPoint
import net.runelite.api.{Client, GameObject, NullObjectID, ObjectID, Perspective, Point}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.TextComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt

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
class FredsTemporossOverlay @Inject()(val client: Client, val plugin: FredsTemporossPlugin, val config: FredsTemporossConfig, val modelOutlineRenderer: ModelOutlineRenderer) extends Overlay(plugin) {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_SCENE)
	private val textComponent: TextComponent = new TextComponent

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics
		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client

		if (inMinigame) {
			val playerLocation = client.getLocalPlayer.getLocalLocation
			highlightGameObjects(graphics, playerLocation)
			highlightNpcs(graphics, playerLocation)
		}
		null
	}

	private def highlightGameObjects(graphics: Graphics2D, playerLocation: LocalPoint): Unit = {
		val plane = client.getTopLevelWorldView.getPlane
		FredsTemporossLogic.fireObjects.toList.foreach((obj, startTick) => {
			if ((obj.getPlane == plane) && obj.getLocalLocation.distanceTo(playerLocation) < MAX_DISTANCE) {
				val poly = obj.getCanvasTilePoly
				if (poly != null) OverlayUtil.renderPolygon(graphics, poly, config.fireColor())
				val durationTicks = Option(FIRE_GAMEOBJECTS.indexOf(obj.getId)).filter(_ != -1).map(FIRE_DURATIONS(_)).get
				config.highlightFires match {
					case TimerModes.OFF =>
					case TimerModes.PIE => renderPieElement(obj.getCanvasLocation, startTick, durationTicks, config.fireColor(), graphics, true)
					case TimerModes.TICKS => renderTextElement(obj.getCanvasLocation, startTick, durationTicks, config.fireColor(), graphics, TimerModes.TICKS, true)
					case TimerModes.SECONDS => renderTextElement(obj.getCanvasLocation, startTick, durationTicks, config.fireColor(), graphics, TimerModes.SECONDS, true)
				}
			}
		})

		FredsTemporossLogic.tetherObjects.toList.foreach(obj => {
			if ((obj.getPlane == plane) && obj.getLocalLocation.distanceTo(playerLocation) < MAX_DISTANCE) {
				val poly  = obj.getCanvasTilePoly
				val color = obj.getId match {
					case x if TETHER_GAMEOBJECTS.contains(x) && client.getVarbitValue(VARB_IS_TETHERED) == 0 => config.waveTimerColor()
					case x if TETHER_GAMEOBJECTS.contains(x) && client.getVarbitValue(VARB_IS_TETHERED) > 0 => config.tetheredColor()
					case x if DAMAGED_TETHER_GAMEOBJECTS.contains(x) => config.poleBrokenColor()
				}
				if (poly != null) OverlayUtil.renderPolygon(graphics, poly, color)
				if (waveIncomingStartTick != -1) {
					config.useWaveTimer() match {
						case TimerModes.OFF =>
						case TimerModes.PIE => renderPieElement(obj.getCanvasLocation, waveIncomingStartTick, WAVE_IMPACT_TICKS, color, graphics)
						case TimerModes.TICKS => renderTextElement(obj.getCanvasLocation, waveIncomingStartTick, WAVE_IMPACT_TICKS, color, graphics, TimerModes.TICKS)
						case TimerModes.SECONDS => renderTextElement(obj.getCanvasLocation, waveIncomingStartTick, WAVE_IMPACT_TICKS, color, graphics, TimerModes.SECONDS)
					}
				}
			}
		})
	}
	private def renderTextElement(pt: Point, startTick: Int, durationTicks: Int, color: Color, graphics: Graphics2D, timerMode: TimerModes, wrap: Boolean = false): Unit = {
		if(pt == null) return
		textComponent.setText(
			Option.when(startTick != -1) {
				val ticks = {
					val ticksCount = (client.getTickCount - startTick)
					if (durationTicks > 0) durationTicks - (if (wrap) (ticksCount % durationTicks) else ticksCount) else ticksCount
				}
				timerMode -> ticks
			}.collect {
				case (TimerModes.TICKS, ticks) => String.format("%d", ticks)
				case (TimerModes.SECONDS, ticks) => String.format("%.1f", ticks * .6f)
			}.getOrElse("???")
		)
		textComponent.setColor(color)
		textComponent.setPosition(new awt.Point(pt.getX, pt.getY))
		textComponent.render(graphics)
	}
	private def renderPieElement(pt: Point, startTick: Int, durationTicks: Int, color: Color, graphics: Graphics2D, wrap: Boolean = false): Unit = {
		if (durationTicks > 0 && pt != null) {
			//modulo as the fire spreads every 24 seconds
			val (percent, c) = if (startTick != -1) {
				val ticksCount = (client.getTickCount - startTick)
				val ticks      = durationTicks - (if (wrap) (ticksCount % durationTicks) else ticksCount)
				ticks.toDouble / durationTicks.toDouble -> color
			} else {
				1d -> Color.RED
			}
			val ppc          = new ProgressPieComponent
			ppc.setBorderColor(c)
			ppc.setFill(c)
			ppc.setProgress(percent)
			ppc.setDiameter(PIE_DIAMETER)
			ppc.setPosition(pt)
			ppc.render(graphics)
		}
	}
	private def highlightNpcs(graphics: Graphics2D, playerLocation: LocalPoint): Unit = {
		FredsTemporossLogic.npcs.toList.foreach((npc, _, startTick) => {
			val npcComposition = npc.getComposition
			val size           = npcComposition.getSize
			val lp             = npc.getLocalLocation
			val tilePoly       = Perspective.getCanvasTileAreaPoly(client, lp, size)

			val color = if (FISH_SPOTS.head == npc.getId) config.doubleSpotColor() else config.normalSpotColor()
			if (tilePoly != null && lp.distanceTo(playerLocation) < MAX_DISTANCE) {
				OverlayUtil.renderPolygon(graphics, tilePoly, color)
			}
			if (lp.distanceTo(playerLocation) < MAX_DISTANCE) {
				val pt       = Perspective.localToCanvas(client, lp, client.getTopLevelWorldView.getPlane)
				val mode     = if (FISH_SPOTS.head == npc.getId) config.highlightDoubleSpot() else config.highlightNormalSpot()
				val duration = if (FISH_SPOTS.head == npc.getId) DOUBLE_SPOT_MOVE_TICKS else 0
				mode match {
					case TimerModes.OFF =>
					case TimerModes.PIE => renderPieElement(pt, startTick, duration, color, graphics)
					case TimerModes.TICKS => renderTextElement(pt, startTick, duration, color, graphics, TimerModes.TICKS)
					case TimerModes.SECONDS => renderTextElement(pt, startTick, duration, color, graphics, TimerModes.SECONDS)
				}
				//testing shows a time between 20 and 27 seconds. even though it isn't fully accurate, it is still better than nothing
				//				val percent = (client.getTickCount - startTick).doubleValue / DOUBLE_SPOT_MOVE_TICKS.doubleValue
				//				val ppc     = new ProgressPieComponent
				//				ppc.setBorderColor(config.doubleSpotColor)
				//				ppc.setFill(config.doubleSpotColor)
				//				ppc.setProgress(percent)
				//				ppc.setDiameter(PIE_DIAMETER)
				//				ppc.setPosition(Perspective.localToCanvas(client, lp, client.getPlane))
				//				ppc.render(graphics)
			}
		})
	}
}
