package com.fredplugins.titheFarm2

import com.fredplugins.common.ShimUtils
import com.fredplugins.common.overlays.{renderGameObjectOverlay, renderTileOverlay, withFont}
import com.fredplugins.titheFarm2.SPlantInfo.EmptyPlantInfo
import com.google.inject.{Inject, Singleton}
import net.runelite.api.coords.LocalPoint
import net.runelite.api.{Client, Perspective}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.{Color, Dimension, Font, Graphics2D}
import scala.util.chaining.*
import scala.compiletime.uninitialized
@Singleton
class SFredsTitheFarmV2PlantOverlay @Inject()(val client: Client, val plugin: FredsTitheFarmV2Plugin, val config: FredsTitheFarmV2PluginConfig,val eventbus: EventBus, val modelOutlineRenderer: ModelOutlineRenderer) extends Overlay {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_WIDGETS)
	setPriority(Overlay.PRIORITY_HIGHEST)
	object Cache {
		var cachedFont: Font = uninitialized
	}
	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if(e.getGroup == FredsTitheFarmV2PluginConfig.GroupName) {
			e.getKey match {
				case "fontSize" | "fontBold" => Cache.cachedFont = FontManager.getRunescapeFont.deriveFont((if(config.getFontBold)  1 else 0), config.getFontSize)
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}

	override def render(graphics: Graphics2D): Dimension = {
		def stateToColor(s: SPlantInfo): Color = {
			s match {
				case SPlantInfo.EmptyPlantInfo => new Color(83, 45, 30)
				case SPlantInfo.DryPlantInfo(t, a) => config.getColorUnwatered
				case SPlantInfo.WateredPlantInfo(t, a) => config.getColorWatered
				case SPlantInfo.DeadPlantInfo(t, a) => config.getColorDead
				case SPlantInfo.GrownPlantInfo(t) => config.getColorGrown
			}
		}
		given Graphics2D = graphics
		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client

		withFont(Cache.cachedFont) {
			plugin.getPlants.foreach(p => {
				val localLocation = LocalPoint.fromWorld(client, p.getWorldLocation)
				val c             = stateToColor(p.info)

				renderGameObjectOverlay(p.go, s"${p.debugStr}", c, (p.getAge == 2))
				if (p.info != EmptyPlantInfo && p.getAge < 3) {
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
