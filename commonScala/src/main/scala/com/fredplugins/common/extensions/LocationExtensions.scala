package com.fredplugins.common.extensions

import com.fredplugins.common.OldOverlayUtil
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.utils.TWorldPoint
import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.Client
import net.runelite.api.Constants
import net.runelite.api.Constants.MAX_Z
import net.runelite.api.Constants.TILE_FLAG_BRIDGE
import net.runelite.api.Perspective
import net.runelite.api.Perspective.LOCAL_HALF_TILE_SIZE
import net.runelite.api.Perspective.LOCAL_TILE_SIZE
import net.runelite.api.Projectile
import net.runelite.api.Tile
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.*
object LocationExtensions extends  ShimUtils.Logging() {
	extension (e: WorldArea) {
		def offset(i: Int): WorldArea = {
			SInteractionUtils.offset(e, i)
		}
		def tiles: Seq[WorldPoint] = SInteractionUtils.worldAreaTiles(e)
		def corners: Seq[WorldPoint] = SInteractionUtils.worldAreaCorners(e)
	}

	extension(e: WorldPoint)(using client: Client) {
		def getLocalPoint: LocalPoint = {
			LocalPoint.fromWorld(client.getTopLevelWorldView, e)
		}
		def getTile: Option[Tile] = {
			e.getLocalPoint.getTile
		}
		def edges: List[((Int, Int), (Int, Int))] = {
			val (wv, msx, msy) = e.getLocalPoint.pipe(lp => (client.getWorldView(lp.getWorldView), lp.getSceneX + 40, lp.getSceneY + 40))

			Option.when(msx >= 0 && msy >= 0 && msx < 184 && msy < 184){
				val scene = wv.getScene
				val settings = scene.getExtendedTileSettings

				def lpToTuple(lp: LocalPoint): (Int, Int) = {
					(lp.getX, lp.getY)
				}
				val sw       = e.getLocalPoint.dx(-LOCAL_HALF_TILE_SIZE).dy(-LOCAL_HALF_TILE_SIZE).pipe(lpToTuple)
				val ne       = e.getLocalPoint.dx(LOCAL_HALF_TILE_SIZE).dy(LOCAL_HALF_TILE_SIZE).pipe(lpToTuple)
				val se       = e.getLocalPoint.dx(LOCAL_HALF_TILE_SIZE).dy(-LOCAL_HALF_TILE_SIZE).pipe(lpToTuple)
				val nw       = e.getLocalPoint.dx(-LOCAL_HALF_TILE_SIZE).dy(LOCAL_HALF_TILE_SIZE).pipe(lpToTuple)
				List((sw, nw), (nw, ne), (ne, se), (se, sw))
				//				val isBridge =(settings(1)(msx)(msy) & TILE_FLAG_BRIDGE) == TILE_FLAG_BRIDGE && wv.getPlane < MAX_Z - 1
//				val tilePlane = Option.when(isBridge)(Math.min(MAX_Z - 1, wv.getPlane + 1
			}.getOrElse(List.empty).tap(edgeList => {
				log.debug("Edges: {}", edgeList)
			})
		}
	}
	extension (e: WorldPoint) {
		def getTemplate: WorldPoint = {
			TWorldPoint.get(e)
		}
		def getInstanced: List[WorldPoint] = {
			TWorldPoint.translate(e).toList
		}
		def packed: Int = {
			((e.getX & 16383) << 14) | (e.getY & 16383) | ((e.getPlane & 0x3) << 28)
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
