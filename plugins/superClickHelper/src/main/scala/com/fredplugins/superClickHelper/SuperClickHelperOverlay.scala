package com.fredplugins.superClickHelper

import com.fredplugins.common.overlays.{getCanvasTextLocation, renderGameObjectOverlay, renderTileOverlay, withFont}
import com.fredplugins.common.{OldOverlayUtil, ShimUtils, overlays}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.Perspective.localToCanvas
import net.runelite.api.coords.LocalPoint
import net.runelite.api.{Client, Point}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.{Color, Dimension, Font, Graphics2D, Rectangle}
import scala.util.chaining.*
@Singleton
class SuperClickHelperOverlay @Inject()(val client: Client, val plugin: SuperClickerPlugin, val config: SuperClickHelperConfig,val eventbus: EventBus, val modelOutlineRenderer: ModelOutlineRenderer) extends Overlay {
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
		if(e.getGroup == SuperClickHelperConfig.GroupName) {
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
		given Graphics2D = graphics
		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client

		withFont(Cache.cachedFont) {
			if(config.isDebugClicks) {
				val (npcs, tiles)  = plugin.getClickedState

				tiles.foreach {
					case (i, point) => {
						val x   = point.getX - client.getTopLevelWorldView.getBaseX
						val y   = point.getY - client.getTopLevelWorldView.getBaseY
						val txt = s"${i.toString.padTo(4, ' ')}($x,$y)"
						renderTileOverlay(point, txt, ColorUtil.colorWithAlpha(Color.BLUE, 128 - ((112d / 100) * i).toInt), true)
					}
				}

				npcs.foreach {
					case (i, n) => {
//						val x   = point.getX - client.getTopLevelWorldView.getBaseX
//						val y   = point.getY - client.getTopLevelWorldView.getBaseY
						val txt = s"${i.toString.padTo(4, ' ')}(${n.getLocalLocation.getSceneX},${n.getLocalLocation.getSceneY}) = ${n.getName}"
						OverlayUtil.renderActorOverlay(summon[Graphics2D], n, txt, ColorUtil.colorWithAlpha(Color.BLUE, 128 - ((112d / 100) * i).toInt))
					}
				}
			}
		}
		null
	}
}
