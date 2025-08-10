package com.fredplugins.valeTotems

import net.runelite.api.gameval.ObjectID
import net.runelite.api.gameval.VarbitID

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import enumeratum._

sealed trait Totem(val baseObjId: Int, val offeringObjId: Int, val rootVarbitId: Int) extends enumeratum.EnumEntry {
	def isVarbitRelated(vb: Int): Option[(Totem, TotemVarbit)] =  TotemVarbits.values.find(tvb => tvb.getRealVarbitId(this) == vb).map(tvb => this -> tvb)
}

object Totems extends enumeratum.Enum[Totem] {
	case object Totem_1 extends Totem(57016, 57020, VarbitID.ENT_TOTEMS_SITE_1_BASE)
	case object Totem_2 extends Totem(57022, 57026, VarbitID.ENT_TOTEMS_SITE_2_BASE)
	case object Totem_3 extends Totem(57028, 57032, VarbitID.ENT_TOTEMS_SITE_3_BASE)
	case object Totem_4 extends Totem(57034, 57038, VarbitID.ENT_TOTEMS_SITE_4_BASE)
	case object Totem_5 extends Totem(57040, 57044, VarbitID.ENT_TOTEMS_SITE_5_BASE)
	case object Totem_6 extends Totem(57046, 57050, VarbitID.ENT_TOTEMS_SITE_6_BASE)
	case object Totem_7 extends Totem(57052, 57056, VarbitID.ENT_TOTEMS_SITE_7_BASE)
	case object Totem_8 extends Totem(57058, 57062, VarbitID.ENT_TOTEMS_SITE_8_BASE)
	val values: IndexedSeq[Totem] = findValues
}