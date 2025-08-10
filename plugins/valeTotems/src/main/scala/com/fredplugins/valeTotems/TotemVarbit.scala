package com.fredplugins.valeTotems

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import enumeratum._

sealed trait TotemVarbit(val offset: Int) extends enumeratum.EnumEntry {
	def getRealVarbitId(totem: Totem): Int = totem.rootVarbitId + offset
}

object TotemVarbits extends enumeratum.Enum[TotemVarbit] {
	case object BASE extends TotemVarbit(0)
	case object BASE_CARVED extends TotemVarbit(1)
	case object BASE_MULTILOC extends TotemVarbit(2)
	case object LOW extends TotemVarbit(3)
	case object MID extends TotemVarbit(4)
	case object TOP extends TotemVarbit(5)
	case object DECORATIONS extends TotemVarbit(6)
	case object ANIMAL_1 extends TotemVarbit(7)
	case object ANIMAL_2 extends TotemVarbit(8)
	case object ANIMAL_3 extends TotemVarbit(9)
	case object DECAY extends TotemVarbit(10)
	case object POINTS extends TotemVarbit(11)
	case object MULTIANIMAL_A_1 extends TotemVarbit(12)
	case object MULTIANIMAL_B_1 extends TotemVarbit(13)
	case object MULTIANIMAL_C_1 extends TotemVarbit(14)
	case object MULTIANIMAL_D_1 extends TotemVarbit(15)
	case object MULTIANIMAL_E_1 extends TotemVarbit(16)
	case object ALL_MULTIANIMALS extends TotemVarbit(17)

	val values: IndexedSeq[TotemVarbit] = findValues
//	def isValid(regionId: Int): Boolean = values.exists(_.regionId == regionId)
}