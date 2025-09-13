package com.fredplugins.common.extensions

import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.Client
import net.runelite.api.Projectile
import net.runelite.api.Tile
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldPoint
import scala.util.chaining.*
object LocationExtensions {
	extension(e: WorldPoint)(using client: Client) {
		def getTemplate: WorldPoint = {
			WorldPointUtils.toTemplate(e)
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
