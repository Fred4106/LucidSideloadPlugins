package com.fredplugins.dynamicHighlights
import com.fredplugins.dynamicHighlights.model.BufferedImageProvider
import com.fredplugins.dynamicHighlights.model.FontType
import com.fredplugins.dynamicHighlights.model.SoundProvider
import com.fredplugins.dynamicHighlights.model.TextAccent
import net.runelite.client.ui.FontManager
import net.runelite.client.util.ImageUtil

import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.Color


case class DisplayConfig(
	showValue:Option[Boolean],
	showDespawn: Option[Boolean],
	textColor: Option[Color],
	backgroundColor: Option[Color],
	borderColor: Option[Color],
	menuTextColor: Option[Color],
	tileStrokeColor: Option[Color],
	tileFillColor: Option[Color],
	highlightTile: Option[Boolean],
	lootbeamColor: Option[Color],
	showLootbeam: Option[Boolean],
	accent: Option[TextAccent],
	accentColor: Option[Color],
	fontStyle: Option[FontType],
	icon: Option[BufferedImageProvider],
	sound: Option[SoundProvider],
	menuSort: Option[Int],
	hideOverlay: Option[Boolean],
	hideAll: Option[Boolean],
	notifyPlayer: Option[Boolean],
	trace: Seq[Int]
) {
	def getTrace(): Array[Int] = trace.toArray
	def getSound: SoundProvider = if(!isHidden) sound.orNull else null
	def getLootbeamColor: Color = lootbeamColor.getOrElse(getTextColor)
	def getTextColor: Color = textColor.getOrElse(Color.WHITE)
	def getIcon: BufferedImageProvider = icon.orNull

	def getMenuTextColor: Color = {
		Option.when(!isHidden)(
			menuTextColor.orElse(textColor)
		).flatten.getOrElse(Color.decode("#ff9040"))
	}

	def getMenuSort: Int = menuSort.getOrElse(0)

	def getFont: Font = {
		fontStyle.collect {
			case FontType.LARGER => FontManager.getRunescapeFont
			case FontType.BOLD => FontManager.getRunescapeBoldFont
		}.getOrElse(FontManager.getRunescapeSmallFont)
	}

	def getTileStrokeColor: Color =tileStrokeColor.getOrElse(getTextColor)

	def isHidden: Boolean = hideAll.isDefined && hideAll.get
	def isShowLootbeam: Boolean = Option.when(!isHidden)(showLootbeam).flatten.exists(_ == true)
	def isShowValue: Boolean = showValue.isDefined && showValue.get
	def isShowDespawn: Boolean = showDespawn.exists(_== true)
	def isNotify: Boolean = Option.when(!isHidden)(notifyPlayer).flatten.exists(_ == true)

	def isHighlightTile: Boolean = Option.when(!isHidden)(highlightTile).flatten.exists(_ == true)
	def isHideOverlay: Boolean = {
		if(hideOverlay.isDefined && hideOverlay.get) {
			return true
		}
		if(hideAll.isDefined && hideAll.get) {
			return true
		}
		return false
//		val b1 = isHidden || hideOverlay.contains(true)
//		val b2 = Option.when(!isHidden)(hideOverlay).flatten.exists(_ == true)
//		if(b1 != b2) {
//			println(s"isHidden=${isHidden}, hideOverlay=${hideOverlay}, b1=${b1}, b2=${b2}")
//		}
//		b1
	}
}

object DisplayConfig {

	class DisplayConfigBuilder(initial: DisplayConfig) {
		private var data: DisplayConfig = initial
		def build: DisplayConfig = data.copy()

		def withTextColor(c: Color): this.type = {
			data = data.copy(textColor = Option(c))
			this
		}
		def withBackgroundColor(c: Color): this.type = {
			data = data.copy(backgroundColor = Option(c))
			this
		}
		def withBorderColor(c: Color): this.type = {
			data = data.copy(borderColor = Option(c))
			this
		}
		def withHidden(c: Boolean): this.type = {
			data = data.copy(hideAll = Option(c))
			this
		}

		def withShowLootbeam(c: Boolean): this.type = {
			data = data.copy(showLootbeam = Option(c))
			this
		}

		def withShowValue(c: Boolean): this.type = {
			data = data.copy(showValue = Option(c))
			this
		}
		def withShowDespawn(c: Boolean): this.type = {
			data = data.copy(showDespawn = Option(c))
			this
		}
		def withNotify(c: Boolean): this.type = {
			data = data.copy(notifyPlayer = Option(c))
			this
		}

		def withTextAccent(c: TextAccent): this.type = {
			data = data.copy(accent = Option(c))
			this
		}
		def withAccentColor(c: Color): this.type = {
			data = data.copy(accentColor = Option(c))
			this
		}

		def withLootbeamColor(c: Color): this.type = {
			data = data.copy(lootbeamColor = Option(c))
			this
		}
		def withFontType(c: FontType): this.type = {
			data = data.copy(fontStyle = Option(c))
			this
		}
		def withMenuTextColor(c: Color): this.type = {
			data = data.copy(menuTextColor = Option(c))
			this
		}
		def withHighlightTile(c: Boolean): this.type = {
			data = data.copy(highlightTile = Option(c))
			this
		}
		def withTileStrokeColor(c: Color): this.type = {
			data = data.copy(tileStrokeColor = Option(c))
			this
		}

		def withTileFillColor(c: Color): this.type = {
			data = data.copy(tileFillColor = Option(c))
			this
		}
		def withHideOverlay(c: Boolean): this.type = {
			data = data.copy(hideOverlay = Option(c))
			this
		}
		def withSound(c: SoundProvider): this.type = {
			data = data.copy(sound = Option(c))
			this
		}
		def withIcon(c: BufferedImageProvider): this.type = {
			data = data.copy(icon = Option(c))
			this
		}
		def withMenuSort(i: Int): this.type = {
			data = data.copy(menuSort = Option(i))
			this
		}
		def withTrace(i: Int): this.type  = {
			data = data.copy(trace = data.trace.appended(i))
			this
		}
	}

	val empty: DisplayConfig = DisplayConfig(
		Option.empty, Option.empty, Option.empty, Option.empty,
		Option.empty, Option.empty, Option.empty, Option.empty,
		Option.empty, Option.empty, Option.empty, Option.empty,
		Option.empty, Option.empty, Option.empty, Option.empty,
		Option.empty, Option.empty, Option.empty, Option.empty,
		Seq.empty
	)
	def emptyBuilder: DisplayConfigBuilder = empty.builder

	extension (a:DisplayConfig) {
		def builder: DisplayConfigBuilder = DisplayConfigBuilder(a)
		def merge(b: DisplayConfig): DisplayConfig = {
			var mut = a.builder
			b.showValue.foreach(mut.withShowValue(_))
			b.showDespawn.foreach(mut.withShowDespawn(_))
			b.textColor.foreach(mut.withTextColor(_))
			b.backgroundColor.foreach(mut.withBackgroundColor(_))
			b.borderColor.foreach(mut.withBorderColor(_))
			b.menuTextColor.foreach(mut.withMenuTextColor(_))
			b.tileStrokeColor.foreach(mut.withTileStrokeColor(_))
			b.tileFillColor.foreach(mut.withTileFillColor(_))
			b.highlightTile.foreach(mut.withHighlightTile(_))
			b.lootbeamColor.foreach(mut.withLootbeamColor(_))
			b.showLootbeam.foreach(mut.withShowLootbeam(_))
			b.accent.foreach(mut.withTextAccent(_))
			b.accentColor.foreach(mut.withAccentColor(_))
			b.fontStyle.foreach(mut.withFontType(_))
			b.icon.foreach(mut.withIcon(_))
			b.sound.foreach(mut.withSound(_))
			b.menuSort.foreach(mut.withMenuSort(_))
			b.hideOverlay.foreach(mut.withHideOverlay(_))
			b.hideAll.foreach(mut.withHidden(_))
			b.notifyPlayer.foreach(mut.withNotify(_))
			mut.build
		}
	}

	def apply(txtColor: Color): DisplayConfig = {
		empty.builder.withTextColor(txtColor).withHidden(false).withShowLootbeam(false).withShowValue(false).withShowDespawn(false).withNotify(false).build
	}
}
