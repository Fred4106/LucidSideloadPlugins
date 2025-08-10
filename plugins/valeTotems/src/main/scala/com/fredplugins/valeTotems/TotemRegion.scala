package com.fredplugins.valeTotems

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait TotemRegion(val regionId: Int) extends enumeratum.EnumEntry {}
object TotemRegions extends enumeratum.Enum[TotemRegion] {
	case object North extends TotemRegion(5684)
	case object NorthEast extends TotemRegion(5940)
	case object NorthWest extends TotemRegion(5928)
	case object South extends TotemRegion(5683)
	case object SouthWest extends TotemRegion(5427)
	case object SouthEast extends TotemRegion(5939)

	val values: IndexedSeq[TotemRegion] = findValues

	def isValid(regionId: Int): Boolean = values.exists(_.regionId == regionId)
}