package com.fredplugins.common.constants.magic

import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.gameval.ItemID

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait SConsumable(val consumedToProductMap: (Int, Int) *) extends enumeratum.EnumEntry {

}

object SConsumables extends enumeratum.Enum[SConsumable] with ShimUtils.Logging() {
	override def values: IndexedSeq[SConsumable] = findValues
	case object Lvl1Jewelry extends SConsumable(
		ItemID.OPAL_RING -> ItemID.RING_OF_PURSUIT,
		ItemID.OPAL_BRACELET -> ItemID.EXPEDITIOUS_BRACELET,
		ItemID.OPAL_NECKLACE -> ItemID.NECKLACE_OF_PASSAGE_5,
		ItemID.STRUNG_OPAL_AMULET -> ItemID.AMULET_OF_BOUNTY,
		ItemID.SAPPHIRE_RING -> ItemID.RING_OF_RECOIL,
		ItemID.JEWL_SAPPHIRE_BRACELET -> ItemID.JEWL_BRACELET_OF_CLAY,
		ItemID.SAPPHIRE_NECKLACE -> ItemID.NECKLACE_OF_MINIGAMES_8,
		ItemID.STRUNG_SAPPHIRE_AMULET -> ItemID.AMULET_OF_MAGIC
	)

	case object AnimalHide extends SConsumable(
		ItemID.DRAGONHIDE_GREEN -> ItemID.DRAGON_LEATHER,
		ItemID.DRAGONHIDE_BLUE -> ItemID.DRAGON_LEATHER_BLUE,
		ItemID.DRAGONHIDE_RED -> ItemID.DRAGON_LEATHER_RED,
		ItemID.DRAGONHIDE_BLACK -> ItemID.DRAGON_LEATHER_BLACK
	)
	
	case object UnstrungAmulet extends SConsumable(
		ItemID.UNSTRUNG_SAPPHIRE_AMULET -> ItemID.STRUNG_SAPPHIRE_AMULET,
		ItemID.UNSTRUNG_RUBY_AMULET -> ItemID.STRUNG_RUBY_AMULET,
		ItemID.UNSTRUNG_EMERALD_AMULET -> ItemID.STRUNG_EMERALD_AMULET,
		ItemID.UNSTRUNG_DIAMOND_AMULET -> ItemID.STRUNG_DIAMOND_AMULET
	)
}
