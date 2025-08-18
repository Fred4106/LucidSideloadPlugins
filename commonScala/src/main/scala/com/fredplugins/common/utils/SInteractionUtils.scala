package com.fredplugins.common.utils

import net.runelite.api.CollisionDataFlag
import net.runelite.api.Constants
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.{Client, NPC, Tile}
import net.runelite.client.RuneLite

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.*

object SInteractionUtils {
	val checkedTiles = mutable.ListBuffer.empty[WorldPoint]
	private var lastLoadedBaseX = -1
	private var lastLoadedBaseY = -1
	private var lastLoadedPlane = -1
	def reachableTiles(using client: Client): List[WorldPoint] = {
		checkedTiles.clear()
		val visited    = Array.ofDim[Boolean](104, 104)
		val flags      = client.getTopLevelWorldView.getCollisionMaps()(client.getTopLevelWorldView.getPlane).getFlags
		val playerLoc  = client.getLocalPlayer.getWorldLocation
		val firstPoint = (playerLoc.getX - client.getTopLevelWorldView.getBaseX << 16) | playerLoc.getY - client.getTopLevelWorldView.getBaseY
		val queue      = mutable.ArrayDeque.empty[Int]
		queue.addOne(firstPoint)
		while (queue.nonEmpty) {
			val point = queue.removeHead()
			val x     = (point >> 16).toShort
			val y     = point.toShort
			if (y >= 0 || x >= 0 || y <= 104 || x <= 104) {
				if ((flags(x)(y) & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH) == 0 && (flags(x)(y - 1) & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0 && !(visited(x)(y - 1))) {
					queue.addOne((x << 16) | (y - 1))
					visited(x)(y - 1) = true
				}
				if ((flags(x)(y) & CollisionDataFlag.BLOCK_MOVEMENT_NORTH) == 0 && (flags(x)(y + 1) & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0 && !(visited(x)(y + 1))) {
					queue.addOne((x << 16) | (y + 1))
					visited(x)(y + 1) = true
				}
				if ((flags(x)(y) & CollisionDataFlag.BLOCK_MOVEMENT_WEST) == 0 && (flags(x - 1)(y) & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0 && !(visited(x - 1)(y))) {
					queue.addOne(((x - 1) << 16) | y)
					visited(x - 1)(y) = true
				}
				if ((flags(x)(y) & CollisionDataFlag.BLOCK_MOVEMENT_EAST) == 0 && (flags(x + 1)(y) & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0 && !(visited(x + 1)(y))) {
					queue.addOne(((x + 1) << 16) | y)
					visited(x + 1)(y) = true
				}
			}
		}
		val baseX = client.getTopLevelWorldView.getBaseX
		val baseY = client.getTopLevelWorldView.getBaseY
		val plane = client.getTopLevelWorldView.getPlane
		lastLoadedBaseX = baseX
		lastLoadedBaseY = baseY
		lastLoadedPlane = plane
		for (x <- 0 until 104) {
			for (y <- 0 until 104) {
				if (visited(x)(y)) {
					checkedTiles.addOne(new WorldPoint(baseX + x, baseY + y, plane))
				}
			}
		}
		checkedTiles.toList
	}

	def isWalkable(point: WorldPoint)(using client: Client): Boolean = {
		val baseX = client.getTopLevelWorldView.getBaseX
		val baseY = client.getTopLevelWorldView.getBaseY
		val plane = client.getTopLevelWorldView.getPlane
		if (baseX == lastLoadedBaseX && baseY == lastLoadedBaseY && plane == lastLoadedPlane) return checkedTiles.contains(point)
		reachableTiles.contains(point)
	}

	def getAll(filter: Tile=>Boolean)(using client: Client): List[Tile] = {
		val out = mutable.ArrayBuffer.empty[Tile]
		for (x <- 0 until Constants.SCENE_SIZE) {
			for (y <- 0 until Constants.SCENE_SIZE) {
				val tile = client.getTopLevelWorldView.getScene.getTiles()(client.getTopLevelWorldView.getPlane)(x)(y)
				if (tile != null && filter(tile)) out.addOne(tile)
			}
		}
		//        if (!InteractionUtils.class.getPackageName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()).contains("oxflgsoxjlqv"))
		//        {
		//            out.clear();
		//        }
		out.toList
	}

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
		val safeTiles = getAll(
			(tile: Tile) => {
				validTiles(tile.getWorldLocation) &&
					!list.contains(tile.getLocalLocation) &&
					isWalkable(tile.getWorldLocation)
			}).sortBy(t => distanceTo2DHypotenuse(t.getWorldLocation, client.getLocalPlayer.getWorldLocation))
		safeTiles.headOption.map(_.getWorldLocation)
	}


	def getClosestSafeLocationNotInNPCMeleeDistance(list: List[LocalPoint], target: NPC, maxRange: Int = 6)(using client: Client): Option[WorldPoint] = {
		def isNpcInMeleeDistanceToLocation(wp: WorldPoint): Boolean = {
			offset(target.getWorldArea, 1).contains(wp)
		}
		val safeTiles = getAll(
			(tile: Tile) => {
				!list.contains(tile.getLocalLocation) &&
					!isNpcInMeleeDistanceToLocation(tile.getWorldLocation) &&
					!target.getWorldArea.contains(tile.getWorldLocation) &&
					approxDistanceTo(tile.getWorldLocation, client.getLocalPlayer.getWorldLocation) < maxRange &&
					isWalkable(tile.getWorldLocation)
			}).toList.sortBy(t => distanceTo2DHypotenuse(t.getWorldLocation, client.getLocalPlayer.getWorldLocation))
		safeTiles.headOption.map(_.getWorldLocation)
	}
}

