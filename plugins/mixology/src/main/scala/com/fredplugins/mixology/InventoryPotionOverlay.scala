package com.fredplugins.mixology

import com.google.inject.Inject
import net.runelite.api.widgets.WidgetItem
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.WidgetItemOverlay

import java.awt.Color
import java.awt.Graphics2D
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.Color

class InventoryPotionOverlay @Inject()(val plugin: FredsMixologyPlugin, val config: FredsMixologyConfig) extends WidgetItemOverlay {
	showOnInventory()

	def renderItemOverlay(graphics2D: Graphics2D, itemId: Int, widgetItem: WidgetItem): Unit = {
		if (plugin.inLab && config.inventoryPotionTagType() != InventoryPotionTagType.NONE) {
			val potionOpt = SBrew.fromItemId(itemId)
			potionOpt.foreach(potion => {
				val bounds = widgetItem.getCanvasBounds
				val x      = bounds.x + 5
				val y      = bounds.y + 30
				drawRecipe(graphics2D, potion, x + 1, y + 1, Color.BLACK) //	 Drop shadow

				if (config.inventoryPotionTagType() == InventoryPotionTagType.COLORED) {
					drawRecipe(graphics2D, potion, x, y, null)
				} else {
					drawRecipe(graphics2D, potion, x, y, Color.WHITE)
				}
			})
		}
	}

	private def drawRecipe(graphics2D: Graphics2D, potion: SBrew, x: Int, y: Int, color: Color): Unit = {
		graphics2D.setFont(FontManager.getRunescapeSmallFont)
		if (color != null) {
			graphics2D.setColor(color)
			graphics2D.drawString(potion.entryName, x, y)
		} else {
			var xOffset = x
			for (component <- potion.components) {
				graphics2D.setColor(component.color)
				graphics2D.drawString(String.valueOf(component.character()), xOffset, y)
				xOffset += graphics2D.getFontMetrics.charWidth(component.character())
			}
		}
	}
}
