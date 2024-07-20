package com.fredplugins.scurriushelper

import com.lucidplugins.api.utils.Reachable
import net.runelite.api.{Client, NPC, Tile}
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.client.RuneLite

import java.util.Optional
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
object SInteractionUtils {
	val client: Client = RuneLite.getInjector.getInstance(classOf[Client])

	def offset(toOffset: WorldArea, offset: Int): WorldArea = new WorldArea(toOffset.getX - offset, toOffset.getY - offset, toOffset.getWidth + 2 * offset, toOffset.getHeight + 2 * offset, toOffset.getPlane)
	def worldAreaCorners(wa: WorldArea): List[WorldPoint] = {
		List(
			new WorldPoint(wa.getX, wa.getY, wa.getPlane),
			new WorldPoint(wa.getX + wa.getWidth, wa.getY, wa.getPlane),
			new WorldPoint(wa.getX, wa.getY+wa.getHeight, wa.getPlane),
			new WorldPoint(wa.getX + wa.getWidth, wa.getY+wa.getHeight, wa.getPlane)
			)
	}

	def worldAreaTiles(wa: WorldArea): List[WorldPoint] = {
		(wa.getX to (wa.getX + wa.getWidth)).flatMap(x => {
			(wa.getY to (wa.getY + wa.getHeight)).map(y => {
				new WorldPoint(x, y, wa.getPlane)
			})
		}).toList
	}

	def getClosestSafeLocationInNPCMeleeDistance(list: java.util.List[LocalPoint], target: NPC): Optional[WorldPoint] = {
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
			}).asScala.toList.sortBy(t => com.lucidplugins.api.utils.InteractionUtils.distanceTo2DHypotenuse(t.getWorldLocation, client.getLocalPlayer.getWorldLocation))
		safeTiles.headOption.map(_.getWorldLocation).toJava
	}
}

