package com.fredplugins.common.extensions

import com.fredplugins.common.utils.SceneUtils
import net.runelite.api.*
import net.runelite.api.MenuAction.*
import net.runelite.api.coords.WorldPoint
import net.runelite.api.widgets.Widget
import net.runelite.client.util.ColorUtil
import net.runelite.client.util.Text

import java.awt.Color
import scala.util.Try
import scala.util.chaining.*

object TextExtensions {
	extension (s: Any) {
		def colored(c: Color): String = s"${ColorUtil.colorTag(c)}${s.toString}${ColorUtil.CLOSING_COLOR_TAG}"
		//		def icon(iconId:Int): String = s"<img=${iconId}>"
		//		def emoji(e: ): String = s"<${}>"
	}
}
