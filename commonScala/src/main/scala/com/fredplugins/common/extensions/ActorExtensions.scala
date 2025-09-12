package com.fredplugins.common.extensions

import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.Actor
import net.runelite.api.Animation
import net.runelite.api.Client
import net.runelite.api.coords.WorldPoint

object ActorExtensions {
//	given Conversion[Actor, `

	extension (e: Actor)(using client: Client) {
		def templateLocation: WorldPoint = {
			WorldPointUtils.toTemplate(e.getWorldLocation)
		}
	}
}
