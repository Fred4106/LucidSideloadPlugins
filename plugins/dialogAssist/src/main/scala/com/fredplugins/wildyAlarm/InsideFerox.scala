package com.fredplugins.wildyAlarm

import net.runelite.api.Player
import net.runelite.api.coords.WorldPoint

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object InsideFerox {
	private val FerexEnclave: Seq[Edge] = Seq(
		Edge(3125, 3639, 3138, 3639),
		Edge(3138, 3639, 3138, 3647),
		Edge(3138, 3647, 3156, 3647),
		Edge(3156, 3647, 3156, 3636),
		Edge(3156, 3636, 3154, 3636),
		Edge(3154, 3636, 3154, 3626),
		Edge(3154, 3626, 3151, 3622),
		Edge(3151, 3622, 3144, 3620),
		Edge(3144, 3620, 3142, 3618),
		Edge(3142, 3618, 3138, 3618),
		Edge(3138, 3618, 3138, 3617),
		Edge(3138, 3617, 3125, 3617),
		Edge(3125, 3617, 3125, 3627),
		Edge(3125, 3627, 3123, 3627),
		Edge(3123, 3627, 3123, 3633),
		Edge(3123, 3633, 3125, 3633),
		Edge(3125, 3633, 3125, 3639)
	)
	def apply(test: WorldPoint): Boolean =
		FerexEnclave.count(hasIntersection(Edge(test.getX, test.getY, 0, 0), _)) % 2 == 1
	def apply(p: Player): Boolean = apply(p.getWorldLocation)

	private def hasIntersection(lhs: Edge, rhs: Edge): Boolean = {
		val v1 = ccw(lhs.x1, lhs.y1, rhs.x1, rhs.y1, rhs.x2, rhs.y2) != ccw(lhs.x2, lhs.y2, rhs.x1, rhs.y1, rhs.x2, rhs.y2)
		val v2 = ccw(lhs.x1, lhs.y1, lhs.x2, lhs.y2, rhs.x1, rhs.y1) != ccw(lhs.x1, lhs.y1, lhs.x2, lhs.y2, rhs.x2, rhs.y2)
		v1 && v2
	}
	private def ccw(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int) = (y3 - y1) * (x2 - x1) > (y2 - y1) * (x3 - x1)


	private case class Edge(x1: Int, y1: Int, x2: Int, y2: Int)
}
