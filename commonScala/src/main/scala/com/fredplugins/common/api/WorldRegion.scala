package com.fredplugins.common.api
import com.fredplugins.common.OldOverlayUtil
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import net.runelite.api.Client
import net.runelite.api.Perspective
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint

import java.awt.Polygon
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait WorldRegion(val worldPoints: Seq[WorldPoint]) {
	assert(worldPoints.distinctBy(_.getPlane).size == 1)
	assert(worldPoints.distinctBy(_.packed).size == worldPoints.size)
	def packedMin: WorldPoint = worldPoints.minBy(_.packed)
	def minX: Int = worldPoints.minBy(_.getX).getX
	def minY: Int = worldPoints.minBy(_.getY).getY
	def plane: Int = worldPoints.head.getPlane
	def origin: WorldPoint = new WorldPoint(minX, minY, plane)
	def contains(wp: WorldPoint): Boolean = worldPoints.contains(wp)

	def polygons(using client: Client): Array[Polygon] = {
		log.debug("worldPoints: {}", worldPoints)
		var borderEdges =  worldPoints.flatMap(_.edges).pipe(alledges => alledges.filter(e => alledges.count(_ == e) == 1))
		log.debug(borderEdges.mkString("borderEdges: [\n\t", ", \n\t", "\n\t]\n"))
		val polygons = scala.collection.mutable.ListBuffer.empty[Polygon]
		var touchedEdges: List[((Int, Int), (Int, Int))] = List.empty
		val ordered = scala.collection.mutable.ListBuffer.empty[(Int,Int)]
		var failedCount = 0
		while(borderEdges.nonEmpty && failedCount < 5) {
			touchedEdges = List.empty
			ordered.clear()
			var edge = borderEdges.filterNot(touchedEdges.contains(_)).headOption.orNull
			if(edge != null) ordered.addOne(edge._1)
			while(edge != null) {
				ordered.addOne(edge._2)
				touchedEdges = touchedEdges.appended(edge)
				edge = borderEdges.filterNot(touchedEdges.contains(_)).find(_._1 == edge._2).orElse({
					val temp = borderEdges.filterNot(touchedEdges.contains(_)).find(_._2 == edge._2)
					temp.foreach(t => borderEdges = borderEdges.updated(borderEdges.indexOf(t), t.swap))
					temp.map(_.swap)
				}).orNull
			}

			borderEdges = borderEdges.filterNot(touchedEdges.contains(_))
			if(ordered.toList.size > 2) {
				val polygon: Polygon = new Polygon()
				ordered.toList.map(p => Perspective.localToCanvas(client, p._1, p._2,
					OldOverlayUtil.getHeight(client.getScene, p._1, p._2, client.getPlane)
				)).foreach(p =>
					polygon.addPoint(p.getX, p.getY)
				)
				polygons.addOne(polygon)
				failedCount = 0
			} else {
				borderEdges = borderEdges.appendedAll(touchedEdges)
				failedCount += 1
			}
		}
		polygons.toArray
	}
}

object WorldRegion {
	case class SimpleRegion(swPoint: WorldPoint, width: Int, height: Int) extends WorldRegion(
		for {
			x <- 0 until width
			y <- 0 until height
		} yield WorldPoint(swPoint.getX + x, swPoint.getY + y, swPoint.getPlane)
	)
	case class ComplexRegion(tiles: Seq[WorldPoint]) extends WorldRegion(tiles) {}

	def union(regions: WorldRegion *): WorldRegion = {
		ComplexRegion(regions.flatMap(_.worldPoints).distinctBy(_.packed))
	}

	def apply(x: Int, y: Int, z: Int, width: Int, height: Int): WorldRegion =  {
		SimpleRegion(new WorldPoint(x, y, z), width, height)
	}

	given Conversion[WorldArea, WorldRegion] = (a) => apply(a.getX, a.getY, a.getPlane, a.getWidth, a.getHeight)
}
