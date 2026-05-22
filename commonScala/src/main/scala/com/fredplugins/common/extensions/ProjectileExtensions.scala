package com.fredplugins.common.extensions

import com.fredplugins.common.utils.TWorldPoint
import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.Animation
import net.runelite.api.DynamicObject
import net.runelite.api.GameObject
import net.runelite.api.GraphicsObject
import net.runelite.api.coords.WorldPoint
import net.runelite.api.Client
import net.runelite.api.ObjectComposition
import net.runelite.api.Projectile
import net.runelite.api.Scene
import net.runelite.api.TileObject
import net.runelite.api.coords.LocalPoint

object ProjectileExtensions {
	extension(e: Projectile) {
		def templateSourceLocation(using client: Client) : WorldPoint = {
			TWorldPoint.get(e.getSourcePoint)//, e.getSourceActor.getWorldView)
		}
		def templateTargetLocation(using client: Client) : WorldPoint = {
			TWorldPoint.get(e.getTargetPoint)
		}
		def ticksRemaining: Int = {
			(e.getRemainingCycles.toDouble / 30.0d).toInt
		}
		def justSpawned: Boolean = {
			e.getRemainingCycles == (e.getEndCycle - e.getStartCycle)
		}
		def hasHit: Boolean = {
			e.getRemainingCycles <= 0
		}
		def localLocation(using client: Client) : LocalPoint = {
			val x: Int = e.getX.toInt
			val y: Int = e.getY.toInt
			new LocalPoint(x, y, client.getScene.getWorldViewId)
		}
		def worldLocation(using client: Client): WorldPoint = {
			WorldPoint.fromLocal(client, localLocation)
		}
	}
}
