package com.fredplugins.common.utils

import com.fredplugins.common.constants.Rune
import net.runelite.api.Client
import net.runelite.api.gameval.InventoryID
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.VarbitID
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.game.ItemVariationMapping

import java.util.stream.IntStream
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object Runes {
	private val client       = RuneLite.getInjector.getInstance(classOf[Client])
	private val clientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])

	def getRunePouchContents: Map[Rune, Int] = {
		List(VarbitID.RUNE_POUCH_TYPE_1 -> VarbitID.RUNE_POUCH_QUANTITY_1,
			VarbitID.RUNE_POUCH_TYPE_2 -> VarbitID.RUNE_POUCH_QUANTITY_2,
			VarbitID.RUNE_POUCH_TYPE_3 -> VarbitID.RUNE_POUCH_QUANTITY_3,
			VarbitID.RUNE_POUCH_TYPE_4 -> VarbitID.RUNE_POUCH_QUANTITY_4
		).flatMap {
			case (t, q) => Rune.getRuneFromIndex(client.getVarbitValue(t)).toScala.zip(Option(client.getVarbitValue(q)).filter(_>0))
		}.toMap
	}

	val equipableUnlimitedRunes: Map[Int, Rune] = List.apply[(Rune, List[Int])](
		Rune.FIRE -> List(ItemID.STAFF_OF_FIRE, ItemID.FIRE_BATTLESTAFF, ItemID.MYSTIC_FIRE_STAFF, ItemID.TOME_OF_FIRE),
		Rune.WATER -> List(ItemID.STAFF_OF_WATER, ItemID.WATER_BATTLESTAFF, ItemID.MYSTIC_WATER_STAFF, ItemID.TOME_OF_WATER),
		Rune.EARTH -> List(ItemID.STAFF_OF_EARTH, ItemID.EARTH_BATTLESTAFF, ItemID.MYSTIC_EARTH_STAFF, ItemID.TOME_OF_EARTH),
		Rune.AIR -> List(ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF),
		Rune.DUST -> List(ItemID.DUST_BATTLESTAFF, ItemID.MYSTIC_DUST_BATTLESTAFF),
		Rune.STEAM -> List(ItemID.STEAM_BATTLESTAFF, ItemID.MYSTIC_STEAM_BATTLESTAFF),
		Rune.LAVA -> List(ItemID.LAVA_BATTLESTAFF, ItemID.MYSTIC_LAVA_STAFF),
		Rune.MIST -> List(ItemID.MIST_BATTLESTAFF, ItemID.MYSTIC_MIST_BATTLESTAFF),
		Rune.MUD -> List(ItemID.MUD_BATTLESTAFF, ItemID.MYSTIC_MUD_STAFF),
		Rune.SMOKE -> List(ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_SMOKE_BATTLESTAFF)
	).flatMap(e => e._2.map(_ -> e._1)).flatMap{
		case (itemId, rune) => ItemVariationMapping.getVariations(itemId).asScala.map(i => i.toInt -> rune)
	}.toMap

	def getInventoryRuneContents: Map[Rune, Int] = {
//		val y = Inventory.search.withId(Rune.values.toList.map(r=>r.getItemId) *).result().asScala.toList
//		y.flatMap(w => {
//			Rune.getRuneFromItemId(w.getItemId).toScala.map(r =>r -> w.getItemQuantity)
//		}).toMap
		client.getItemContainer(InventoryID.INV).getItems.toList
			.flatMap(i => Rune.getRuneFromItemId(i.getId).toScala.map(a => a-> i.getQuantity)).toMap
	}

	def getEquippedUnlimitedRunes: Set[Rune] = {
//		Equipment.search().filter(eiqw => equipableUnlimitedRunes.contains(eiqw.getItemId)).result().asScala.toList
//			.map(eiqw => equipableUnlimitedRunes(eiqw.getItemId)).toSet
		Set.empty
	}

	def getRunes: Map[Rune, Int] = {
		val runepouch = getRunePouchContents
		val inventory = getInventoryRuneContents
		val equiped = getEquippedUnlimitedRunes
		Rune.values.flatMap(r => Option(runepouch.getOrElse(r, 0) + inventory.getOrElse(r, 0)).map(q => if(equiped.contains(r)) Integer.MAX_VALUE else q).filter(_ > 0).map(q => r -> q)).toMap
	}
}
