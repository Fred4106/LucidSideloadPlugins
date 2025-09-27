package com.fredplugins.common.extensions

import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.common.utils.TWorldPoint
import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.Client
import net.runelite.api.Projectile
import net.runelite.api.Tile
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint

import scala.util.chaining.*
object LocationExtensions {
	extension (e: WorldArea) {
		def offset(i: Int): WorldArea = {
			SInteractionUtils.offset(e, i)
		}
		def tiles: Seq[WorldPoint] = SInteractionUtils.worldAreaTiles(e)
		def corners: Seq[WorldPoint] = SInteractionUtils.worldAreaCorners(e)
	}

	extension(e: WorldPoint)(using client: Client) {
		def getTemplate: WorldPoint = {
			TWorldPoint.get(e)
		}
		def getLocalPoint: LocalPoint = {
			LocalPoint.fromWorld(client.getTopLevelWorldView, e.getTemplate)
		}
		def getTile: Option[Tile] = {
			e.getLocalPoint.getTile
		}
	}
	extension (e: LocalPoint)(using client: Client) {
		def getWorldPoint: WorldPoint = {
			WorldPoint.fromLocal(client, e)
		}
		def getTemplate: WorldPoint = {
			getWorldPoint.getTemplate
		}
		def getTile: Option[Tile] = {
			val wv = client.getTopLevelWorldView
			Option.when(e.isInScene){wv.getScene.getTiles.apply(wv.getPlane).apply(e.getSceneX).apply(e.getSceneY)}
		}
	}
}
