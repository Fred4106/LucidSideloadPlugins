package com.fredplugins.kroovy.ui

import net.runelite.api.{Client, ItemID}
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.game.{ItemManager, SpriteManager}
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.components.{ComponentOrientation, ImageComponent, LayoutableRenderableEntity, LineComponent, SplitComponent, TitleComponent}

import java.awt.{Color, Font}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait LayoutRenderElement {
	def create: LayoutableRenderableEntity = LayoutRenderableHelper.create(this)
}
case class TextElement(text: String = "", color: Color = Color.WHITE, fontName: String = FontManager.getRunescapeFont.getName, fontStyle: Int = Font.PLAIN, fontSize: Int = 16) extends LayoutRenderElement {
	def withFontSize(s: Int): TextElement = this.copy(fontSize = s)
	def withFontName(n: String): TextElement = this.copy(fontName = n)
	def withFontStyle(s: Int): TextElement = this.copy(fontStyle = s)
	def withColor(c: Color): TextElement = this.copy(color = c)
	def withText(t: String): TextElement = this.copy(text = t)
	def withFont(f: Font): TextElement = this.withFontName(f.getName).withFontStyle(f.getStyle).withFontSize(f.getSize)
	def font: Font = new Font(fontName, fontStyle, fontSize);
}
case class LineElement(left: TextElement = TextElement(), right: TextElement = TextElement()) extends LayoutRenderElement {
	def withLeft(op: TextElement => TextElement): LineElement = LineElement(op(left), right)
	def withRight(op: TextElement => TextElement): LineElement = LineElement(left, op(right))
}
case class ImageElement(source: IconSource = ItemIconSource(ItemID.SANTA_HAT)) extends LayoutRenderElement {
//	def withLeft(op: TextElement => TextElement): ImageElement = ImageEleme(op(left), right)
	def withSource(iconSource: IconSource): ImageElement = this.copy(source = iconSource)
}
case class SplitElement(left: LayoutRenderElement = TextElement(), right: LayoutRenderElement = TextElement(), orientation: ComponentOrientation = ComponentOrientation.HORIZONTAL) extends LayoutRenderElement {
	def withLeft(l: LayoutRenderElement): SplitElement = this.copy(left= l)
	def withRight(r: LayoutRenderElement): SplitElement = this.copy(right= r)
}
object LayoutRenderableHelper {
	given Client = RuneLite.getInjector.getInstance(classOf[Client])
	given ClientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])
	given SpriteManager = RuneLite.getInjector.getInstance(classOf[SpriteManager])
	given ItemManager = RuneLite.getInjector.getInstance(classOf[ItemManager])

	def create(e: LayoutRenderElement): LayoutableRenderableEntity = {
		e match {
			case te@TextElement(text, color, fontName, fontStyle, fontSize) => TitleComponent.builder().text(text).color(color).font(te.font).build()
			case LineElement(left, right) => {
				LineComponent.builder()
					.left(left.text)
					.leftColor(left.color)
					.leftFont(left.font)
					.right(right.text)
					.rightColor(right.color)
					.rightFont(right.font)
					.build()
			}
			case ImageElement(source) => {
				ImageComponent(IconSource.toBufferedImage(source))
			}
			case SplitElement(left, right,orientation) => {
				SplitComponent.builder().first(left.create).second(right.create).orientation(orientation).build()
			}
		}
	}
}
