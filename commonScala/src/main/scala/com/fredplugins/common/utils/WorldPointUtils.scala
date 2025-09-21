package com.fredplugins.common.utils

import net.runelite.api.{Client, Scene, WorldView}
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldPoint
import net.runelite.api.Constants.CHUNK_SIZE
import scala.util.chaining.*
object WorldPointUtils {
	private val log = ShimUtils.getLogger(getClass.getName, "DEBUG")

	def toTemplate(wp: WorldPoint)(using client:Client): WorldPoint = {
		Option.when[WorldPoint => WorldPoint](client.getTopLevelWorldView.isInstance)(fromInstance(_)).getOrElse(identity[WorldPoint]).apply(wp)
	}

	def toInstance(worldPoint: WorldPoint)(using client:Client): Seq[WorldPoint] = {
		inline def range(tx: Int) = (tx until CHUNK_SIZE + tx)
		inline def isBound(tx: Int, ty: Int, tz: Int): Boolean = range(tx).contains(worldPoint.getX) && range(ty).contains(worldPoint.getY) && worldPoint.getPlane == tz
		val wv = client.getTopLevelWorldView
		Option.when(wv.isInstance){
			val templateChunks = wv.getInstanceTemplateChunks()
			for {
				z <- 0 until templateChunks.length
				x <- templateChunks(z).indices
				y <- templateChunks(z)(x).indices
				cd = templateChunks(z)(x)(y)
				template = this.ChunkData(cd)//((cd >> 1 & 0x3), (cd >> 3 & 0x7FF * CHUNK_SIZE), ((cd >> 14 & 0x3FF) * CHUNK_SIZE), (cd >> 24 & 0x3))
				if isBound(template.x, template.y, template.p)// >= tx && worldPoint.getX < tx + CHUNK_SIZE && (tx until (tx + CHUNK_SIZE)).contains(worldPoint.getX)
			} yield rotate(
					WorldPoint(
						wv.getBaseX + x * CHUNK_SIZE + (worldPoint.getX & (CHUNK_SIZE - 1)),
						wv.getBaseY + y * CHUNK_SIZE + (worldPoint.getY & (CHUNK_SIZE - 1)),
						z
					), template.r)

		} .filter(_ != null).getOrElse(List.empty)
	}

	case class ChunkData(r: Int, x: Int, y: Int, p: Int)
	object ChunkData {
		def apply(templateChunk: Int): ChunkData = ChunkData(
			templateChunk >> 1 & 0x3,
			(templateChunk >> 14 & 0x3FF) * CHUNK_SIZE,
			(templateChunk >> 3 & 0x7FF) * CHUNK_SIZE,
			templateChunk >> 24 & 0x3
		)
	}
	def fromInstance(worldPoint: WorldPoint)(using client: Client): WorldPoint = {
		val wv = client.getTopLevelWorldView
		val localPoint = LocalPoint.fromWorld(wv, worldPoint);
		Option.when(localPoint != null && wv.isInstance)({
			extension (c: (Int, Int)) {
				def x: Int = c._1
				def y: Int = c._2
			}
			val scene: (Int, Int) = (localPoint.getSceneX, localPoint.getSceneY)
			val chunk = ChunkData(wv.getInstanceTemplateChunks()(worldPoint.getPlane)(scene.x / CHUNK_SIZE)(scene.y / CHUNK_SIZE))

			// calculate world point of the template// calculate world point of the template
			rotate(WorldPoint(chunk.x + (scene.x & (CHUNK_SIZE - 1)), chunk.y + (scene.y & (CHUNK_SIZE - 1)), chunk.p), 3 - chunk.r)
		}).get
	}

	/**
		 * Rotate the coordinates in the chunk according to chunk rotation
		 *
		 * @param point    point
		 * @param rotation rotation
		 * @return world point
		 */
	private def rotate(point: WorldPoint, rotation: Int): WorldPoint = {
		val chunkX = point.getX & -CHUNK_SIZE
		val chunkY = point.getY & -CHUNK_SIZE
		val x = point.getX & (CHUNK_SIZE - 1)
		val y = point.getY & (CHUNK_SIZE - 1)
		Option(rotation).collect {
			case 1 => new WorldPoint(chunkX + y, chunkY + (CHUNK_SIZE - 1 - x), point.getPlane)
			case 2 => new WorldPoint(chunkX + (CHUNK_SIZE - 1 - x), chunkY + (CHUNK_SIZE - 1 - y), point.getPlane)
			case 3 => new WorldPoint(chunkX + (CHUNK_SIZE - 1 - y), chunkY + x, point.getPlane)
		}.getOrElse(point)
	}
}