package com.fredplugins.common.utils

import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.{Client, NPC, Tile}
import net.runelite.client.RuneLite

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.*

object SInteractionUtils {
	def approxDistanceTo(wp1: WorldPoint, wp2: WorldPoint): Int = {
		math.max(math.abs(wp1.getX - wp2.getX), math.abs(wp1.getY - wp2.getY))
	}

	def distanceTo2DHypotenuse(main: WorldPoint, other: WorldPoint): Double = {
		math.hypot(main.getX-other.getX, main.getY-other.getY)
	}

	def offset(toOffset: WorldArea, offset: Int): WorldArea = new WorldArea(toOffset.getX - offset, toOffset.getY - offset, toOffset.getWidth + 2 * offset, toOffset.getHeight + 2 * offset, toOffset.getPlane)

	def worldAreaCorners(wa: WorldArea): List[WorldPoint] = {
		List(
			new WorldPoint(wa.getX, wa.getY, wa.getPlane),
			new WorldPoint(wa.getX + wa.getWidth, wa.getY, wa.getPlane),
			new WorldPoint(wa.getX, wa.getY + wa.getHeight, wa.getPlane),
			new WorldPoint(wa.getX + wa.getWidth, wa.getY + wa.getHeight, wa.getPlane)
		)
	}

	def worldAreaTiles(wa: WorldArea): List[WorldPoint] = {
		(wa.getX to (wa.getX + wa.getWidth)).flatMap(x => {
			(wa.getY to (wa.getY + wa.getHeight)).map(y => {
				new WorldPoint(x, y, wa.getPlane)
			})
		}).toList
	}
	def getClosestSafeLocationInNPCMeleeDistance(list: List[LocalPoint], target: NPC)(using client: Client): Option[WorldPoint] = {
		val validTiles: WorldPoint => Boolean = target.getWorldArea.pipe(ta => {
			val offArea = offset(ta, 1)
			val corners = worldAreaCorners(offArea)
			(wp: WorldPoint) => offArea.contains(wp) && !ta.contains(wp) && !corners.exists(wwp => wp.distanceTo(wwp) == 0)
		})
		val safeTiles = com.lucidplugins.api.utils.InteractionUtils.getAll(
			(tile: Tile) => {
				validTiles(tile.getWorldLocation) &&
					!list.contains(tile.getLocalLocation) &&
					com.lucidplugins.api.utils.InteractionUtils.isWalkable(tile.getWorldLocation)
			}).asScala.toList.sortBy(t => distanceTo2DHypotenuse(t.getWorldLocation, client.getLocalPlayer.getWorldLocation))
		safeTiles.headOption.map(_.getWorldLocation)
	}


	def getClosestSafeLocationNotInNPCMeleeDistance(list: List[LocalPoint], target: NPC, maxRange: Int = 6)(using client: Client): Option[WorldPoint] = {
		def isNpcInMeleeDistanceToLocation(wp: WorldPoint): Boolean = {
			offset(target.getWorldArea, 1).contains(wp)
		}
		val safeTiles = com.lucidplugins.api.utils.InteractionUtils.getAll(
			(tile: Tile) => {
				!list.contains(tile.getLocalLocation) &&
					!isNpcInMeleeDistanceToLocation(tile.getWorldLocation) &&
					!target.getWorldArea.contains(tile.getWorldLocation) &&
					approxDistanceTo(tile.getWorldLocation, client.getLocalPlayer.getWorldLocation) < maxRange &&
					com.lucidplugins.api.utils.InteractionUtils.isWalkable(tile.getWorldLocation)
			}).asScala.toList.sortBy(t => distanceTo2DHypotenuse(t.getWorldLocation, client.getLocalPlayer.getWorldLocation))
		safeTiles.headOption.map(_.getWorldLocation)
	}
}

