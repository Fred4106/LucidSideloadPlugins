package com.fredplugins.common.extensions

import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.NPCComposition
import net.runelite.api.coords.WorldPoint
import net.runelite.client.RuneLite
import net.runelite.client.game.NPCManager
import net.runelite.client.util.ColorUtil

import java.awt.Color
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized


object GeneralExtensions {
//	given Conversion[Actor, `

	extension [D](d: D)(using num: Numeric[D]) {
		def bounded(min: D, max: D): D = num.min(max, num.max(min, d))
	}
	extension (c: Color) {
		def interpolate(c2: Color, scale: Double): Color = {
			ColorUtil.colorLerp(c, c2, scale.bounded(0, 1))
		}
		def withOpacity(scale: Double): Color = {
			withAlpha((255 * scale.bounded(0, 1)).toInt)
		}
		def withAlpha(alpha: Int): Color = {
			ColorUtil.colorWithAlpha(c, alpha.bounded(0, 255))
		}
	}
}
