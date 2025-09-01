package com.fredplugins.common.constants.magic

import com.fredplugins.common.utils.ShimUtils

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed class SSpellInfo(val level: Int)(runesSeq: (SRune, Int) *)  extends enumeratum.EnumEntry {
	val runes: Map[SRune, Int] = runesSeq.toMap
}

//sealed abstract class BasicRune(val itemIds: Int *) extends SRune {
//	override def basicRunes: Seq[BasicRune] = Seq(this)
//}
//
//sealed abstract class ComboRune(val basicRunes: BasicRune *)(val itemIds: Int *) extends SRune{}

object ArcturusSpellInfo extends enumeratum.Enum[SSpellInfo] with ShimUtils.Logging() {
	case object BasicReanimation extends SSpellInfo(16)(SRunes.Body_Rune -> 4, SRunes.Nature_Rune -> 2)
	case object AdeptReanimation extends SSpellInfo(41)(SRunes.Body_Rune -> 4, SRunes.Nature_Rune -> 3, SRunes.Soul_Rune -> 1)
	case object ExpertReanimation extends SSpellInfo(72)(SRunes.Blood_Rune -> 1, SRunes.Nature_Rune -> 3, SRunes.Soul_Rune -> 2)  
	case object MasterReanimation extends SSpellInfo(90)(SRunes.Blood_Rune -> 2, SRunes.Nature_Rune -> 4, SRunes.Soul_Rune -> 4)

	override def values: IndexedSeq[SSpellInfo] = findValues
}


case class SSpellCost(runes: Map[SRune, Int], otherConsumables: Map[SConsumable, (Int, Int)]) {}
