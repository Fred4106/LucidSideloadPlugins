package com.fredplugins.titheFarm

import net.runelite.client.ui.FontManager
import com.fredplugins.common.ShimUtils
import com.fredplugins.titheFarm.SPlantState.Grown
import com.fredplugins.titheFarm.SPlantType.Empty
import com.google.inject.{Binder, Inject, Provides, Singleton}
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Client, GameObject, Perspective, Point, TileObject}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.{ProgressBarComponent, ProgressPieComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayPriority, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt
import java.awt.{BasicStroke, Color, Dimension, Font, Graphics2D, Polygon, Rectangle, Shape}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.*
import java.time.Duration
import java.time.Instant

@Singleton
class SFredsTitheFarmPlantOverlay @Inject()(val client: Client, val plugin: FredsTitheFarmPlugin, val config: FredsTitheFarmPluginConfig,val eventbus: EventBus, val modelOutlineRenderer:ModelOutlineRenderer) extends Overlay {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_WIDGETS)
	setPriority(Overlay.PRIORITY_HIGHEST)
	object Cache {
		var cachedFont: Font = _
	}
	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if(e.getGroup == FredsTitheFarmPluginConfig.GroupName) {
			e.getKey match {
				case "fontSize" | "fontBold" => Cache.cachedFont = FontManager.getRunescapeFont.deriveFont((if(config.getFontBold)  1 else 0), config.getFontSize)
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}

	override def render(graphics: Graphics2D): Dimension = {
		def stateToColor(s: SPlantState): Color = {
			s match {
				case SPlantState.Unwatered(_) => config.getColorUnwatered
				case SPlantState.Watered(_) => config.getColorWatered
				case SPlantState.Dead(_) => config.getColorDead
				case SPlantState.Grown => config.getColorGrown
			}
		}
		given Graphics2D = graphics
		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client

		import com.fredplugins.common.overlays.{renderGameObjectOverlay, renderMinimapArea, renderTileOverlay, withFont}

		withFont(Cache.cachedFont) {
			plugin.getPlants.foreach(p => {
				val localLocation = LocalPoint.fromWorld(client, p.getWorldLocation)
				val c             = stateToColor(p.state)

				renderGameObjectOverlay(p.go, s"${p.debugStr}", c, (p.getAge == 2))
				if (p.tpe != Empty && p.state != Grown) {
					ProgressPieComponent().tap(pieComponent => {
						val countdownPos = Perspective.localToCanvas(client, localLocation, client.getPlane, 128)
						pieComponent.setPosition(countdownPos)
						pieComponent.setProgress(1 - p.getRelative)
						pieComponent.setBorderColor(Color.black)
						pieComponent.setFill(ColorUtil.colorWithAlpha(c, (c.getAlpha / 1.5).toInt))
					}).render(summon[Graphics2D])
					val textToShow = s"${p.ticksSincePlanted(client)} | ${p.millisSincePlanted}"
					OverlayUtil.renderTextLocation(summon[Graphics2D], Perspective.getCanvasTextLocation(client, summon[Graphics2D], localLocation, textToShow, 256), textToShow, Color.black)
				}
			})

			if(config.isDebugObjectClicked) {
				plugin.getClickedTiles.foreach {
					case (i, point) => {
						val x   = point.getX - client.getTopLevelWorldView.getBaseX
						val y   = point.getY - client.getTopLevelWorldView.getBaseY
						val txt = s"${i.toString.padTo(4, ' ')}($x,$y)"
						renderTileOverlay(point, txt, ColorUtil.colorWithAlpha(Color.BLUE, 128 - ((112d / 100) * i).toInt), true)
					}
				}
			}
		}
		null
	}
}
