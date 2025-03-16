package com.fredplugins.kroovy.ui

import net.runelite.client.ui.overlay.RenderableEntity
import java.awt._

object Rendering {
	private val INFOBOX_COLOR_OFFSET = 0.2f
	private val INFOBOX_OUTER_COLOR_OFFSET = 1 - INFOBOX_COLOR_OFFSET
	private val INFOBOX_INNER_COLOR_OFFSET = 1 + INFOBOX_COLOR_OFFSET
	private val INFOBOX_ALPHA_COLOR_OFFSET = 1 + 2 * INFOBOX_COLOR_OFFSET
	private val MAX_BYTE = 255
	def outsideStrokeColor(backgroundColor: Color): Color = return new Color(backgroundColor.getRed *
		INFOBOX_OUTER_COLOR_OFFSET.round, backgroundColor.getGreen * INFOBOX_OUTER_COLOR_OFFSET.round, backgroundColor
		.getBlue * INFOBOX_OUTER_COLOR_OFFSET.round, Math.min(MAX_BYTE, backgroundColor.getAlpha *
		INFOBOX_ALPHA_COLOR_OFFSET.round))
	def insideStrokeColor(backgroundColor: Color): Color = return new Color(Math.min(MAX_BYTE, backgroundColor.getRed
		* INFOBOX_INNER_COLOR_OFFSET.round), Math.min(MAX_BYTE, backgroundColor.getGreen * INFOBOX_INNER_COLOR_OFFSET
		.round), Math.min(MAX_BYTE, backgroundColor.getBlue * INFOBOX_INNER_COLOR_OFFSET.round), Math.min(MAX_BYTE,
		backgroundColor.getAlpha * INFOBOX_ALPHA_COLOR_OFFSET.round))
	def renderEntityRelative(gfx: Graphics2D, entity: RenderableEntity, x: Int, y: Int): Unit = {
		val g = gfx.create.asInstanceOf[Graphics2D]
		try {
			g.translate(x, y)
			entity.render(g)
		} finally g.dispose()
	}
	private def getCenteredTextY(fm: FontMetrics, boundaryHeight: Int): Int = return ((boundaryHeight - fm.getHeight) /
		2) + (fm.getAscent)
	def drawText(
		g: Graphics2D, bounds: Rectangle, color: Color,
		alignment: Alignment, text: String, isVertical: Boolean
	): Unit = {
		val currentFont = g.getFont
		val newFont = currentFont.deriveFont(currentFont.getStyle, if (isVertical) {
			13
		} else {
			16
		})
		g.setFont(newFont)
		val fm = g.getFontMetrics
		var x = bounds.x
		alignment.getAlignmentX match {
			case AlignmentParam.Min =>
				x = bounds.x
			case AlignmentParam.Mid =>
				x = (if (isVertical) {
					bounds.getCenterX + bounds.x - (fm.getHeight / 2)
				} else {
					bounds.getCenterX - (fm.stringWidth(text).toDouble / 2)
				}).round.toInt
			case AlignmentParam.Max =>
				x = (if (isVertical) {
					bounds.getMaxX + bounds.x - fm.getHeight
				} else {
					bounds.getMaxX - fm.stringWidth(text)
				}).round.toInt
		}
		var y = bounds.y
		alignment.getAlignmentY match {
			case AlignmentParam.Min =>
				y = if (isVertical) {
					bounds.y + fm.getHeight
				} else {
					bounds.y
				}
			case AlignmentParam.Mid =>
				y = if (isVertical) {
					bounds.getCenterY.round.toInt - (text.length * 10 / 2) + fm.getHeight
				} else {
					bounds.y + getCenteredTextY(fm, bounds.height)
				}
			case AlignmentParam.Max =>
				y = if (isVertical) {
					bounds.getMaxY.round.toInt - ((text.length - 1) * 10)
				} else {
					bounds.getMaxY.round.toInt - fm.getHeight
				}
		}

		val fX = x
		val fY = y
		if (isVertical) {
			val textChar = text.toCharArray
			for (i <- 0 until textChar.length) {
				g.setColor(Color.BLACK)
				g.drawChars(textChar, i, 1, fX + 1, fY + 1 + i * 10)
				g.setColor(color)
				g.drawChars(textChar, i, 1, fX, fY + i * 10)
			}
		}
		else {
			g.setColor(Color.BLACK)
			g.drawString(text, fX + 1, fY + 1)
			g.setColor(color)
			g.drawString(text, fX, fY)
		}
	}
	def drawProgressBar(g: Graphics2D, bounds: Rectangle, borderColor: Color, progressLeftColor: Color,
		progressDoneColor: Color, min: Long, max: Long, value: Long,
		isVertical: Boolean): Unit = {
		var progress = (value.toDouble - min) / (max.toDouble - min)
		progress = Math.min(1.0, progress)
		val progressDone = new Rectangle(bounds)
		val progressLeft = new Rectangle(bounds)
		if (isVertical) {
			progressDone.height = (progress * progressDone.height).round.toInt
			progressLeft.y += progressDone.height
			progressLeft.height -= progressDone.height
		}
		else {
			progressDone.width = (progress * progressDone.width).round.toInt
			progressLeft.x += progressDone.width
			progressLeft.width -= progressDone.width
		}
		g.setColor(progressLeftColor)
		g.fill(progressLeft)
		g.setColor(progressDoneColor)
		g.fill(progressDone)
		g.setColor(borderColor)
		g.draw(bounds)
	}
}
