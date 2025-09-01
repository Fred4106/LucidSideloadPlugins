package com.fredplugins.common.constants.magic

import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.gameval.ItemID.{
	STAFF_OF_AIR, AIR_BATTLESTAFF, MYSTIC_AIR_STAFF,
	STAFF_OF_WATER, WATER_BATTLESTAFF, MYSTIC_WATER_STAFF,
	STAFF_OF_EARTH, EARTH_BATTLESTAFF, MYSTIC_EARTH_STAFF,
	STAFF_OF_FIRE, FIRE_BATTLESTAFF, MYSTIC_FIRE_STAFF,

	MIST_BATTLESTAFF, MYSTIC_MIST_BATTLESTAFF,
	DUST_BATTLESTAFF, MYSTIC_DUST_BATTLESTAFF,
	MUD_BATTLESTAFF, MYSTIC_MUD_STAFF,
	SMOKE_BATTLESTAFF, MYSTIC_SMOKE_BATTLESTAFF,
	STEAM_BATTLESTAFF, MYSTIC_STEAM_BATTLESTAFF,
	LAVA_BATTLESTAFF, MYSTIC_LAVA_STAFF,
	NATURE_STAFF_CHARGED,
	KODAI_WAND, BR_KODAI_WAND,
	TOME_OF_WATER,
	TOME_OF_FIRE,
	BOOK_OF_THE_DEAD,
	IBANSTAFF, IBANSTAFF_UPGRADED,
	SLAYER_STAFF, SLAYER_STAFF_ENCHANTED,
	ZAMORAK_STAFF,
	GUTHIX_STAFF, PEST_VOID_KNIGHT_MACE, PEST_VOID_KNIGHT_MACE_TROUVER,
	SARADOMIN_STAFF,
	SOTD, BR_SOTD,
	TOXIC_SOTD, TOXIC_SOTD_CHARGED, STAFF_OF_LIGHT, STAFF_OF_BALANCE
}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait SEquipable extends enumeratum.EnumEntry {
	def itemIds: Seq[Int]// = itemIdsSeq.toSet
	def worksInInventory: Boolean
	def unlimitedRunes: Set[SRune]
}

sealed abstract class SRuneSource(val rune: SRune)(val itemIds: Int *) extends SEquipable {
	override def unlimitedRunes: Set[SRune] = Set(rune)
	override def worksInInventory: Boolean = false
}

sealed abstract class SReqItem(val worksInInventory: Boolean)(val itemIds: Int *) extends SEquipable {
	override def unlimitedRunes: Set[SRune] = Set.empty[SRune]
}

object SEquipables extends enumeratum.Enum[SEquipable] with ShimUtils.Logging() {
	case object Air_Staff extends SRuneSource(SRunes.Air_Rune)(STAFF_OF_AIR)
	case object Water_Staff extends SRuneSource(SRunes.Water_Rune)(STAFF_OF_WATER)
	case object Earth_Staff extends SRuneSource(SRunes.Earth_Rune)(STAFF_OF_EARTH)
	case object Fire_Staff extends SRuneSource(SRunes.Fire_Rune)(STAFF_OF_FIRE)
	case object Air_BattleStaff extends SRuneSource(SRunes.Air_Rune)(AIR_BATTLESTAFF)
	case object Water_BattleStaff extends SRuneSource(SRunes.Water_Rune)(WATER_BATTLESTAFF)
	case object Earth_BattleStaff extends SRuneSource(SRunes.Earth_Rune)(EARTH_BATTLESTAFF)
	case object Fire_BattleStaff extends SRuneSource(SRunes.Fire_Rune)(FIRE_BATTLESTAFF)
	case object Mystic_Air_Staff extends SRuneSource(SRunes.Air_Rune)(MYSTIC_AIR_STAFF)
	case object Mystic_Water_Staff extends SRuneSource(SRunes.Water_Rune)(MYSTIC_WATER_STAFF)
	case object Mystic_Earth_Staff extends SRuneSource(SRunes.Earth_Rune)(MYSTIC_EARTH_STAFF)
	case object Mystic_Fire_Staff extends SRuneSource(SRunes.Fire_Rune)(MYSTIC_FIRE_STAFF)

	case object Mist_BattleStaff extends SRuneSource(SRunes.Mist_Rune)(MIST_BATTLESTAFF)
	case object Mystic_Mist_Staff extends SRuneSource(SRunes.Mist_Rune)(MYSTIC_MIST_BATTLESTAFF)
	case object Dust_BattleStaff extends SRuneSource(SRunes.Dust_Rune)(DUST_BATTLESTAFF)
	case object Mystic_Dust_Staff extends SRuneSource(SRunes.Dust_Rune)(MYSTIC_DUST_BATTLESTAFF)
	case object Mud_BattleStaff extends SRuneSource(SRunes.Mud_Rune)(MUD_BATTLESTAFF)
	case object Mystic_Mud_Staff extends SRuneSource(SRunes.Mud_Rune)(MYSTIC_MUD_STAFF)
	case object Smoke_BattleStaff extends SRuneSource(SRunes.Smoke_Rune)(SMOKE_BATTLESTAFF)
	case object Mystic_Smoke_Staff extends SRuneSource(SRunes.Smoke_Rune)(MYSTIC_SMOKE_BATTLESTAFF)
	case object Steam_BattleStaff extends SRuneSource(SRunes.Steam_Rune)(STEAM_BATTLESTAFF)
	case object Mystic_Steam_Staff extends SRuneSource(SRunes.Steam_Rune)(MYSTIC_STEAM_BATTLESTAFF)
	case object Lava_BattleStaff extends SRuneSource(SRunes.Lava_Rune)(LAVA_BATTLESTAFF)
	case object Mystic_Lava_Staff extends SRuneSource(SRunes.Lava_Rune)(MYSTIC_LAVA_STAFF)

	case object Nature_Staff extends SRuneSource(SRunes.Nature_Rune)(NATURE_STAFF_CHARGED)
	case object Kodai_Wand extends SRuneSource(SRunes.Water_Rune)(KODAI_WAND, BR_KODAI_WAND)

	case object Tome_of_Water extends SRuneSource(SRunes.Water_Rune)(TOME_OF_WATER)
	case object Tome_of_Fire extends SRuneSource(SRunes.Fire_Rune)(TOME_OF_FIRE)
	case object Book_of_the_Dead extends SReqItem(true)(BOOK_OF_THE_DEAD)

	case object Ibans_Staff extends SReqItem(false)(IBANSTAFF_UPGRADED, IBANSTAFF)
	case object Slayers_Staff extends SReqItem(false)(SLAYER_STAFF, SLAYER_STAFF_ENCHANTED, SOTD, BR_SOTD, TOXIC_SOTD, TOXIC_SOTD_CHARGED, STAFF_OF_LIGHT, STAFF_OF_BALANCE)
	case object Saradomin_Staff extends SReqItem(false)(SARADOMIN_STAFF, STAFF_OF_LIGHT)
	case object Zamorak_Staff extends SReqItem(false)(ZAMORAK_STAFF, SOTD, BR_SOTD, TOXIC_SOTD, TOXIC_SOTD_CHARGED)
	case object Guthix_Staff extends SReqItem(false)(GUTHIX_STAFF, PEST_VOID_KNIGHT_MACE, PEST_VOID_KNIGHT_MACE_TROUVER, STAFF_OF_BALANCE)

	override def values: IndexedSeq[SEquipable] = findValues


	inline def runeSourceValues: Seq[SRuneSource] = values.collect{
		case r: SRuneSource => r
	}

	inline def reqItemValues: Seq[SReqItem] = values.collect {
		case r: SReqItem => r
	}

	inline def wornOnlyValues: Seq[SEquipable] = values.collect {
		case u if !u.worksInInventory => u
	}
	inline def worksInInventoryValues: Seq[SEquipable] = values.collect {
		case u if u.worksInInventory => u
	}
//	inline def wornOnlyValues: Seq[SEquipable] = values.filterNot(_.worksInInventory)
//	inline def wornOrInventoryValues: Seq[SEquipable] = values.filter(_.worksInInventory)

	def isEquipmentItemId(id: Int, inInventory: Boolean = false): Boolean = {
		values.exists(x => x.itemIds.contains(id) && (if (inInventory) x.worksInInventory else true))
	}

//	def isWorksInInventoryItemId(id: Int): Boolean = {
//		values.filter(x => x.itemIds.contains(id)).exists(_.worksInInventory)
//	}
}