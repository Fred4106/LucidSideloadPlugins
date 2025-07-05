package com.fredplugins.common.extensions

import com.fredplugins.common.utils.SceneUtils
import net.runelite.api.*
import net.runelite.api.MenuAction.*
import net.runelite.api.coords.WorldPoint
import net.runelite.api.widgets.Widget
import net.runelite.client.util.Text

import scala.util.Try
import scala.util.chaining.*

object WidgetExtensions {
	extension (w: Widget) {
		def getGroupId(): Int = w.getId >>> 16
		def getChildId(): Int = w.getId & 0xFFFF
		def getChildIdx(): Int = w.getIndex
	}
}
