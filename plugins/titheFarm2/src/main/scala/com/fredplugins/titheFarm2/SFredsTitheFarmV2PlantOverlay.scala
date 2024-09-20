package com.fredplugins.titheFarm2

import com.fredplugins.common.{OldOverlayUtil, overlays}
import com.fredplugins.common.overlays.{getCanvasTextLocation, renderGameObjectOverlay, renderTileOverlay, withFont}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.titheFarm2.SPlantInfo.{EmptyPlantInfo, NonEmptyPlantInfo}
import com.fredplugins.titheFarm2.TitheFarmLookup.PlantData
import com.google.inject.{Inject, Singleton}
import net.runelite.api.Perspective.localToCanvas
import net.runelite.api.coords.LocalPoint
import net.runelite.api.{Client, Perspective, Point}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.geom.Rectangle2D
import java.awt.{Color, Dimension, Font, FontMetrics, Graphics2D, Rectangle}
import scala.util.chaining.*
import scala.compiletime.uninitialized
@Singleton
class SFredsTitheFarmV2PlantOverlay @Inject()(val client: Client, val plugin: FredsTitheFarmV2Plugin, val config: FredsTitheFarmV2PluginConfig,val eventbus: EventBus, val modelOutlineRenderer: ModelOutlineRenderer) extends Overlay {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_WIDGETS)
	setPriority(Overlay.PRIORITY_HIGHEST)
	object Cache {
		var cachedFont: Font = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), config.getFontSize)
		var countdownFont: Font = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), (config.getFontSize * 1.5).toInt)
	}
	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if(e.getGroup == FredsTitheFarmV2PluginConfig.GroupName) {
			e.getKey match {
				case "fontSize" | "fontBold" => {
					Cache.cachedFont = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), config.getFontSize)
					Cache.countdownFont = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), (config.getFontSize * 1.5).toInt)
				}
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}

	override def render(graphics: Graphics2D): Dimension = {
		inline def stateToColor(s: SPlantInfo): Color = {
			Option(s).collect {
				case SPlantInfo.DryPlantInfo(t, a) => config.getColorUnwatered
				case SPlantInfo.WateredPlantInfo(t, a) => config.getColorWatered
				case SPlantInfo.DeadPlantInfo(t, a) => config.getColorDead
				case SPlantInfo.GrownPlantInfo(t) => config.getColorGrown
				case SPlantInfo.EmptyPlantInfo => config.getColorEmpty
			}.getOrElse(Color.MAGENTA)
		}
		given Graphics2D = graphics
		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client

		withFont(Cache.cachedFont) {

//			TitheFarmPatchLoc.values.map(loc => (loc, loc.getGameObject, plugin.getPlants))

			plugin.getFarmLookup.getData.tapEach {
				case (loc, PlantData(pInfo, _, _, _)) => {
					val localPoint = LocalPoint.fromWorld(client.getTopLevelWorldView, loc)
					val poly = OldOverlayUtil.getCanvasTileAreaPoly(client, localPoint, 1, 1, .5d, 0)
					if (poly != null) {
						val c = stateToColor(pInfo)
						OverlayUtil.renderPolygon(summon[Graphics2D], poly, c, ColorUtil.colorWithAlpha(c, 64), overlays.getStroke(2, true))
					}
				}
			}.tapEach {
				case (loc, data@PlantData(cachedInfo: NonEmptyPlantInfo, go, composted, countdown)) => {
//					log.debug("Trying to render game overlay at loc {} with gameObject {} and data {}", loc, go.getId, data)
					val c = stateToColor(cachedInfo)
					renderGameObjectOverlay(go, s"${cachedInfo}", c, data.cachedInfo.age == 2)
					if (countdown > 0) {
						withFont(Cache.countdownFont) {
							val localLocation = LocalPoint.fromWorld(client, go.getWorldLocation)
							val textToShow    = s"${(localLocation.getSceneX, localLocation.getSceneY)} => ${countdown}"
							ProgressPieComponent().tap(pieComponent => {
								val countdownPos = localToCanvas(client, localLocation, client.getPlane, 128)
								pieComponent.setPosition(countdownPos)
								pieComponent.setProgress(countdown.toDouble / 100d)
								pieComponent.setBorderColor(Color.black)
								pieComponent.setFill(ColorUtil.colorWithAlpha(if(composted) Color.PINK else c, (c.getAlpha / 1.5).toInt))
							}).render(summon[Graphics2D])
							val textLocation@(x, y, b) = getCanvasTextLocation(localLocation, textToShow, 160)
//							graphics.fillOval(x - 10, y - 10, 20, 20)
							val textBackground            = new Rectangle(x + b.getX.toInt, y + b.getY.toInt, (b.getWidth).toInt, (b.getHeight).toInt)
//							log.debug("area for text {} = {} @ {}   {}", textToShow, b, (x, y), textBackground)
							OverlayUtil.renderPolygon(summon[Graphics2D], textBackground, ColorUtil.colorWithAlpha(Color.BLACK, 0), ColorUtil.colorWithAlpha(Color.WHITE, 64), overlays.getStroke(2, true))
//							OverlayUtil.renderPolygon(summon[Graphics2D], );
							OverlayUtil.renderTextLocation(summon[Graphics2D], new Point(x, y), textToShow, Color.black)
						}
					}
				}
				case (loc, PlantData(cachedInfo, go, _, _)) => {}
			}
//			plugin.getPlants.flatMap(x => x._1.getGameObject.map (go => (x._1,go, x._2))).foreach {
//				case (loc, go, dta@PlantData(cachedState, isComposted, countdown)) =>
//					val localLocation = LocalPoint.fromWorld(client, go.getWorldLocation)//.worldLocation.map(wp => LocalPoint.fromWorld(client.getTopLevelWorldView, wp))
//					val c             = stateToColor(cachedState)
//
//					renderGameObjectOverlay(go, s"${dta}", c, (cachedState.age == 2))
//					val textToShow = s"${loc} => ${dta}"
//					if (countdown > 0) {
//						ProgressPieComponent().tap(pieComponent => {
//							val countdownPos = Perspective.localToCanvas(client, localLocation, client.getPlane, 128)
//							pieComponent.setPosition(countdownPos)
//							pieComponent.setProgress(countdown.toDouble / 100d)
//							pieComponent.setBorderColor(Color.black)
//							pieComponent.setFill(ColorUtil.colorWithAlpha(if(isComposted) Color.PINK else c, (c.getAlpha / 1.5).toInt))
//						}).render(summon[Graphics2D])
//					}
//
//					val textLocation@(x, y, w, h) = getCanvasTextLocation(localLocation, textToShow, 256)
//					val padding = 5
//					val textBackground = new Rectangle(x-padding, y-padding, w+padding*2, h+padding*2)
//					OverlayUtil.renderPolygon(summon[Graphics2D], textBackground, Color.BLACK, ColorUtil.colorWithAlpha(Color.WHITE, 64), overlays.getStroke(2, true))
//					OverlayUtil.renderTextLocation(summon[Graphics2D], new Point(x, y), textToShow, Color.black)
//			}
//			plugin.getPlants.foreach(p => {
//
//			})

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
