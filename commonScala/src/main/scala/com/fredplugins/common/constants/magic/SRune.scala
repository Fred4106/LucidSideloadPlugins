package com.fredplugins.common.constants.magic

import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.gameval.ItemID

import scala.util.chaining.*

sealed trait SRune extends enumeratum.EnumEntry {
	def itemIds: Seq[Int]
	def runepouchId: Int = SRunes.values.indexOf(this)+1
	def basicRunes: Seq[BasicRune]
}

sealed abstract class BasicRune(val itemIds: Int *) extends SRune {
	override def basicRunes: Seq[BasicRune] = Seq(this)
}

sealed abstract class ComboRune(val basicRunes: BasicRune *)(val itemIds: Int *) extends SRune{}

object SRunes extends enumeratum.Enum[SRune] with ShimUtils.Logging() {
	case object Air_Rune extends BasicRune(556, 6422, 7558, 9693, 11688, 11715)
	case object Water_Rune extends BasicRune(555, 6424, 7556, 9691, 11687 , 11716)
	case object Earth_Rune extends BasicRune(557, 6426, 9695, 11689, 11717)
	case object Fire_Rune extends BasicRune(554, 6428, 7554, 9699, 11686, 11718)
	case object Mind_Rune extends BasicRune(558, 6436, 9697, 11690)
	case object Chaos_Rune extends BasicRune(562, 6430, 7560, 11694, ItemID.NZONE_CHAOSRUNE)
	case object Death_Rune extends BasicRune(560, 6432, 11692, ItemID.NZONE_DEATHRUNE)
	case object Blood_Rune extends BasicRune(565, 11697, ItemID.NZONE_BLOODRUNE)
	case object Cosmic_Rune extends BasicRune(564, ItemID.FAKE_COSMICRUNE)
	case object Nature_Rune extends BasicRune(561, 11693)
	case object Law_Rune extends BasicRune(563, 6434, 11695)
	case object Body_Rune extends BasicRune(559, 6438, 11691)
	case object Soul_Rune extends BasicRune(566, 11698)
	case object Astral_Rune extends BasicRune(9075, 11699)
	case object Mist_Rune extends ComboRune(Water_Rune, Air_Rune)(4695)
	case object Mud_Rune extends ComboRune(Water_Rune, Earth_Rune)(4698)
	case object Dust_Rune extends ComboRune(Air_Rune, Earth_Rune)(4696)
	case object Lava_Rune extends ComboRune(Fire_Rune, Earth_Rune)(4699)
	case object Steam_Rune extends ComboRune(Fire_Rune, Water_Rune)(4694)
	case object Smoke_Rune extends ComboRune(Fire_Rune, Air_Rune)(4697)
	case object Wrath_Rune extends BasicRune(21880, 22208)
	case object Sunfire_Rune extends ComboRune(Fire_Rune)(28929)
	case object Aether_Rune extends ComboRune(Cosmic_Rune, Soul_Rune)(30843)

	override def values: IndexedSeq[SRune] = findValues

	inline def basicValues: Seq[BasicRune] = values.collect{
		case b: BasicRune => b
	}

	inline def comboValues: Seq[ComboRune] = values.collect {
		case b: ComboRune => b
	}

	private lazy val findByIndex: Map[Int, SRune] = values.map(sr => sr.runepouchId -> sr).toMap

	def findForRunepouchType(tpe: Int): SRune = {
		assert(findByIndex.contains(tpe))
		findByIndex(tpe)
	}
}
