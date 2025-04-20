package com.fredplugins.demonicgorillaV2

import net.runelite.api.Client
import net.runelite.api.Constants
import net.runelite.api.Point
import net.runelite.api.WorldView
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import java.util.function.Predicate

object WorldAreaExtended {
	def getComparisonPoint(area1: WorldArea, area2: WorldArea): Point = {
		val x1 = area1.getX
		val y1 = area1.getY
		val x2 = area2.getX
		val y2 = area2.getY
		val w1 = area1.getWidth
		val h1 = area1.getHeight
		var x  = 0
		var y  = 0
		if (x2 <= x1) {
			x = x1
		} else {
			x = Math.min(x2, x1 + w1 - 1)
		}
		if (y2 <= y1) {
			y = y1
		} else {
			y = Math.min(y2, y1 + h1 - 1)
		}
		new Point(x, y)
	}
	def getAxisDistances(area1: WorldArea, area2: WorldArea): Point = {
		val p1 = getComparisonPoint(area1, area2)
		val p2 = getComparisonPoint(area2, area1)
		new Point(Math.abs(p1.getX - p2.getX), Math.abs(p1.getY - p2.getY))
	}
	def calculateNextTravellingPoint(client: Client, original: WorldArea, target: WorldArea, stopAtMeleeDistance: Boolean, extraCondition: Predicate[_ >: WorldPoint]): WorldArea = {
		val topWorldView = client.getTopLevelWorldView
		val z1           = original.getPlane
		val z2           = target.getPlane
		if (z1 != z2) return null
		if (original.intersectsWith(target)) {
			if (stopAtMeleeDistance) {
				// Movement is unpredictable when the NPC and actor stand on top of each other
				return null
			}
			else {
				return original
			}
		}
		val x1            = original.getX
		val y1            = original.getY
		val x2            = target.getX
		val y2            = target.getY
		val dx            = x2 - x1
		val dy            = y2 - y1
		val axisDistances = getAxisDistances(original, target)
		val axisX         = axisDistances.getX
		val axisY         = axisDistances.getY
		if (stopAtMeleeDistance && axisX + axisY == 1) {
			// NPC is in melee distance of target, so no movement is done
			return original
		}
		val lp = LocalPoint.fromWorld(topWorldView, x1, y1)
		if (lp == null) return null
		val lpSceneX = lp.getSceneX
		val lpSceneY = lp.getSceneY
		if (lpSceneX + dx < 0 || lpSceneX + dy >= Constants.SCENE_SIZE || lpSceneY + dx < 0 || lpSceneY + dy >= Constants
			.SCENE_SIZE) {
			// NPC is travelling out of the scene, so collision data isn't available
			return null
		}
		val w1    = original.getWidth
		val h1    = original.getHeight
		val dxSig = Integer.signum(dx)
		val dySig = Integer.signum(dy)
		if (stopAtMeleeDistance && axisX == 1 && axisY == 1) {
			// When it needs to stop at melee distance, it will only attempt
			// to travel along the x-axis when it is standing diagonally
			// from the target
			if (original.canTravelInDirection(topWorldView, dxSig, 0, extraCondition)) return new WorldArea(x1 + dxSig, y1, w1, h1, z1)
		}
		else if (original.canTravelInDirection(topWorldView, dxSig, dySig, extraCondition)) {
			return new WorldArea(x1 + dxSig, y1 + dySig, w1, h1, z1)
		} else if (dx != 0 && original.canTravelInDirection(topWorldView, dxSig, 0, extraCondition)) {
			return new WorldArea(x1 + dxSig, y1, w1, h1, z1)
		} else if (dy != 0 && Math.max(Math.abs(dx), Math.abs(dy)) > 1 && original.canTravelInDirection(topWorldView, 0, dy, extraCondition)) {
			// Note that NPCs don't attempt to travel along the y-axis
			// if the target is <= 1 tile distance away
			return new WorldArea(x1, y1 + dySig, w1, h1, z1)
		}
		// The NPC is stuck
		original
	}
	def calculateNextTravellingPoint(client: Client, original: WorldArea, target: WorldArea, stopAtMeleeDistance: Boolean): WorldArea = calculateNextTravellingPoint(client, original, target, stopAtMeleeDistance, (x: WorldPoint) => true)
}