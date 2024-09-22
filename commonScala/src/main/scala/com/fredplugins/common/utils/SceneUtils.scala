package com.fredplugins.common.utils

import net.runelite.api.{Client, NPC, Player, TileObject}
import org.slf4j.Logger

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
			List(tile.getWallObject, tile.getDecorativeObject, tile.getGroundObject).prependedAll(tile.getGameObjects.toList).filter(_ != null).find(to => {
				val comp = client.getObjectDefinition(to.getId)
				Option(comp.getImpostorIds).map(_.toList.appended(to.getId)).getOrElse(List(to.getId)).contains(id)
			})
		}.flatten
	}
}
