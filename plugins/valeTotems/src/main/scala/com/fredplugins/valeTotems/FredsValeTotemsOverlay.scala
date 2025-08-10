package com.fredplugins.valeTotems

import com.fredplugins.common.overlays.{getCanvasTextLocation, renderGameObjectOverlayBak, renderTileOverlay, withFont}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.{OldOverlayUtil, overlays}
import com.google.inject.{Inject, Singleton}
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
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import scala.jdk.CollectionConverters.{IteratorHasAsScala, ListHasAsScala}
import java.awt.{Color, Dimension, Font, Graphics2D, Rectangle}
import scala.util.chaining.*
@Singleton
class FredsValeTotemsOverlay @Inject()(val client: Client, val plugin: FredsValeTotemsPlugin, val config: FredsValeTotemsConfig, val eventbus: EventBus, 	val totemService: TotemService) extends OverlayPanel(plugin) {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.TOP_LEFT)
	setPreferredSize(new Dimension(200, 100))
//	setLayer(OverlayLayer.UNDER_WIDGETS)
//	setPriority(Overlay.PRIORITY_HIGHEST)
	object Cache {
		var cachedFont: Font = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, config.getFontSize)
		var countdownFont: Font = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, (config.getFontSize * 1.5).toInt)
	}


	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if(e.getGroup == FredsValeTotemsConfig.GroupName) e.getKey match {
			case "fontSize" | "fontBold" => {
				Cache.cachedFont = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, config.getFontSize)
				Cache.countdownFont = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, (config.getFontSize * 1.5).toInt)
			}
			case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
		}
	}

	override def render(graphics: Graphics2D): Dimension = {
		given Graphics2D = graphics
//		given ModelOutlineRenderer = modelOutlineRenderer
		given Client = client
		withFont(Cache.cachedFont) {
//			if(config.isDebugClicks) {
//				val (npcs, tiles)  = plugin.getClickedState
//
//				tiles.foreach {
//					case (i, point) => {
//						val x   = point.getX - client.getTopLevelWorldView.getBaseX
//						val y   = point.getY - client.getTopLevelWorldView.getBaseY
//						val txt = s"${i.toString.padTo(4, ' ')}($x,$y)"
//						renderTileOverlay(point, txt, ColorUtil.colorWithAlpha(Color.BLUE, 128 - ((112d / 100) * i).toInt), true)
//					}
//				}
//
//				npcs.foreach {
//					case (i, n) => {
////						val x   = point.getX - client.getTopLevelWorldView.getBaseX
////						val y   = point.getY - client.getTopLevelWorldView.getBaseY
//						val txt = s"${i.toString.padTo(4, ' ')}(${n.getLocalLocation.getSceneX},${n.getLocalLocation.getSceneY}) = ${n.getName}"
//						OverlayUtil.renderActorOverlay(summon[Graphics2D], n, txt, ColorUtil.colorWithAlpha(Color.BLUE, 128 - ((112d / 100) * i).toInt))
//					}
//				}
//			}
		}
		def elems: collection.mutable.Buffer[LayoutableRenderableEntity]/*java.util.List[LayoutableRenderableEntity]*/ = panelComponent.getChildren.asScala//.toljava.util.ArrayList[LayoutableRenderableEntity]].asScala

//		if(PyramidPlunderHelper.isInPyramidPlunder) {
//			if (config.showChestsLooted) elems.addOne(LineComponent
//																							 .builder
//																							 .left("Total Chests Looted:")
//																							 .right(String.format("%d", plugin.chestLooted))
//																							 .build)
//
//			if (config.showSarcoLooted) elems.addOne(LineComponent
//																							.builder
//																							.left("Total Sarcophagi Looted:")
//																							.right(String.format("%d", plugin.sarcoLooted))
//																							.build)
//
//			if (config.showChance) elems.addOne(LineComponent
//																				 .builder
//																				 .left("% Chance of at least one Sceptre:")
//																				 .right(String.format("%f", plugin.dryChance * 100))
//																				 .build)
		elems.addOne(
			TitleComponent.builder().text(FredsValeTotemsConfig.GroupName).color(Color.GREEN).build()
		)
			elems.addOne(LineComponent
										 .builder
										 .left("Nearest")
										 .right(s"${totemService.getNearest}")
										 .build)
		
		totemService.getNearest.map(totemService.getState).map(ts => {
			(0 until ts.productArity).map(n => {
				val key = ts.productElementName(n)
				val value = ts.productElement(n)
				val valueColor: Color = //LineComponent.LineComponentBuilder => LineComponent.LineComponentBuilder = 
					value match {
						case b: Boolean => if(b) Color.GREEN else Color.RED
						case i: Int => Color.BLUE
						case s: String => Color.ORANGE
						case other => Color.GRAY
					}
				LineComponent
					.builder
					.left(s"${key}")
					.right(s"${value}")
					.rightColor(valueColor)
					.build
			})
		}).getOrElse(Seq.empty).foreach(lc => {
			elems.addOne(lc)
		})
//			elems.addOne(LineComponent
//										 .builder
//										 .left("UsingSpearTrap = ")
//										 .right(s"${plugin.usingSpearTrap}")
//										 .build)


//		plugin.currentRoom.collect(room => {
//			LineComponent
//				.builder
//				.left(s"Room ${room}")
//				.right(s"${room.percentageOds}")
//				.build
//		}).foreach(panelComponent.getChildren.add(_))
//		plugin.stateLines.map{
//			case (str, (vid, vvalue)) => {
//				LineComponent
//					.builder
//					.left(s"${str}[${vid}]")
//					.right(s"${vvalue}")
//					.rightColor(
//						if(vvalue == 0) Color.RED
//						else if(vvalue == 1) Color.GREEN
//						else Color.YELLOW)
//					.leftColor(
//						if(str.contains("URN")) new Color(0, 255, 255, 255)
//						else if(str.contains("DOOR")) new Color(0, 255, 0, 255)
//						else if (str.contains("SARCOPHAGUS") || str.contains("GOLDEN_CHEST")) new Color(255, 255, 0, 255)
//						else new Color(255, 150, 150, 255))
//					.build
//			}
//		}.foreach {
//			case x => panelComponent.getChildren.add(x)
//		}
		super.render(graphics)
	}
}
