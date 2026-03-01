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

object Icons {
	def load[S <: String & Singleton : ValueOf]: BufferedImage = {
		ImageUtil.loadImageResource(Icons.getClass, s"/com/fredplugins/dynamicHighlights/icons/${valueOf[S]}.png")
	}
	lazy val folder: BufferedImage = load["folder_icon"]
	lazy val overlay_disabled: BufferedImage = load["overlay_disabled"]
	lazy val panel: BufferedImage = load["panel"]
	lazy val paste: BufferedImage = load["paste_icon"]
	lazy val reload: BufferedImage = load["reload_icon"]
}

sealed trait DefaultFilter(val url: String) extends enumeratum.EnumEntry {
	def displayName: String = s"[default: ${this.entryName}'s filter]"
}

object DefaultFilters extends enumeratum.Enum[DefaultFilter] {
	case object Rikten extends DefaultFilter("https://raw.githubusercontent.com/riktenx/filterscape/refs/heads/main/default.rs2f")
	case object Joe extends DefaultFilter("https://raw.githubusercontent.com/typical-whack/loot-filters-modules/refs/heads/main/default-filter.rs2f")

	override def values: IndexedSeq[DefaultFilter] = findValues
	def valuesArray: Array[DefaultFilter] = values.toArray
}
