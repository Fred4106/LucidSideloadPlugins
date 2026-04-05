package com.fredplugins.mixology

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.mixology.PotionComponent.{AGA, LYE, MOX}
import net.runelite.api.{Client, FontID}
import net.runelite.api.gameval.VarPlayerID.{MIXOLOGY_LYE_POINTS, MIXOLOGY_AGA_POINTS, MIXOLOGY_MOX_POINTS}
import net.runelite.api.widgets.{Widget, WidgetPositionMode, WidgetTextAlignment, WidgetType}
import net.runelite.client.callback.ClientThread

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class MixologyWidgetTool(using client: Client, clientThread: ClientThread, config: FredsMixologyConfig) extends ShimUtils.Logging("DEBUG") {
	private def addResinText(widget: Widget, x: Int, component: PotionComponent): Unit = {
		val amount = client.getVarpValue(component.resinVarpId())
		val color = component.color().getRGB()
		widget.setText(amount + "")
			.setTextShadowed(true)
			.setTextColor(color)
			.setOriginalWidth(20)
			.setOriginalHeight(15)
			.setFontId(FontID.QUILL_8)
			.setOriginalY(0)
			.setOriginalX(x)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)

		widget.revalidate()
		log.debug("adding resin text {} at {} with color {}", amount, x, color)
	}

	def appendResins(baseWidget: Widget): Unit = {
		if (!config.displayResin) return
		val parentWidth = baseWidget.getWidth
		val dx = parentWidth / 3
		val x = dx / 2
		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x, MOX)
		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x + dx, AGA)
		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x + dx * 2, LYE)
	}


	def updatePotionOrdersComponent(baseWidget: Widget, potionOrders: java.util.List[PotionOrder]): Unit = {
		// https://github.com/Joshua-F/cs2-scripts/blob/7cc261be62a40a6390de3e1f770259038660af10/scripts/%5Bproc%2Cscript7063%5D.cs2#L26
		val children = selectChildren(baseWidget, _.getType.pipe(wt => wt == WidgetType.GRAPHIC || wt == WidgetType.TEXT))
		if (children.isEmpty) return
		/*
						 * Filtered children layout:
						 * TEXT - Potion Orders
						 * GRAPHIC - 5673
						 * TEXT - Mammoth-might mix
						 * GRAPHIC - 5672
						 * TEXT - <str>Mixalot</str>
						 * GRAPHIC - 5673
						 * TEXT - Marley's moonlight
						 */
		potionOrders.asScala.toList.zipWithIndex.foreach {(order, i) =>
			log.debug("Updating component for order {}", order)
			val orderGraphic = children(order.idx * 2 + 1)
			val orderText = children(order.idx * 2 + 2)
			assert(orderGraphic.getType == WidgetType.GRAPHIC && orderText.getType == WidgetType.TEXT)
			orderText.setText(s"${orderText.getText} ${
				if (order.fulfilled) "(<col=00ff00>done!</col>)"
				else                s"(${order.potionType.recipe})"
			}")

			if (i != order.idx) {
				// update component position
				val calculatedY = 20 + (i * 26) + 3
				if(
					List(orderGraphic, orderText)
						.filter(w => w.getOriginalY != calculatedY)
						.tapEach(_.setOriginalY(calculatedY))
						.tapEach(_.revalidate()).nonEmpty
				) {
					log.debug("Updating order {} position from {} to {}", order, order.idx, i)
				}
			}
		}
	}

	//
//	val color = alchemyObject match {
//		case MOX_LEVER => MOX.color()
//		case AGA_LEVER => AGA.color()
//		case LYE_LEVER => LYE.color()
//		case ALEMBIC | AGITATOR | RETORT | MIXING_VESSEL => ???
//		case CONVEYOR_BELT =>
//		case HOPPER =>
//		case DIGWEED_NORTH_EAST | DIGWEED_SOUTH_EAST | DIGWEED_SOUTH_WEST | DIGWEED_NORTH_WEST => config.digweedHighlightColor(
//	}

	def selectChildren(parent: Widget, filter: Widget => Boolean): List[Widget] = {
		(for{
			child <- Option(parent).map(_.getChildren).map(_.toList).getOrElse(List.empty)
			if filter(child)
		} yield child).toList
	}
}
