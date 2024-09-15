package com.fredplugins.common

import net.runelite.api.{Client, NPC, Player, TileObject}
import org.slf4j.Logger

import scala.collection.mutable
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Try

object SceneUtils {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	def findNpc(identifier: Int)(using client: Client): Option[NPC] = {
		val possibleMatch = Try {
			client.getTopLevelWorldView.npcs().byIndex(identifier)
		}.toOption
		possibleMatch
	}
	def findPlayer(identifier: Int)(using client: Client): Option[Player] = {
		val possibleMatch = Try {
			client.getTopLevelWorldView.players().byIndex(identifier)
		}.toOption
		possibleMatch
	}
	def findTileObject(x: Int, y: Int, id: Int)(using client: Client): Option[TileObject] = {
		val scene = client.getTopLevelWorldView.getScene
		val tiles = scene.getTiles
		val tile  = tiles(client.getTopLevelWorldView.getPlane)(x)(y)
		Option.when(tile != null) {
//			val gameObjectOpt = tile.getGameObjects.toList.find(go => go != null && go.getId == id)
			//			val wallObjectOpt = Option(tile.getWallObject).filter(_.getId == id)
			//			val decorativeObjectOpt = Option(tile.getDecorativeObject).filter(_.getId == id)
			//			val groundObjectOpt = Option(tile.getGroundObject).filter(_.getId == id)
			List(tile.getWallObject, tile.getDecorativeObject, tile.getGroundObject).prependedAll(tile.getGameObjects.toList).find(to => to != null && to.getId == id)
		}.flatten
	}
}
