package com.fredplugins.common.utils


//import com.fredplugins.common.constants.magic
//import com.fredplugins.common.constants.magic.*

//import com.fredplugins.common.constants.magic._
//import com.fredplugins.common.magic.OldRune
import com.fredplugins.common.magic.OldRune
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

object RunesUtil {
	private val client       = RuneLite.getInjector.getInstance(classOf[Client])
	private val clientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])

	def getRunePouchContents: Map[OldRune, Int] = {
		List(VarbitID.RUNE_POUCH_TYPE_1 -> VarbitID.RUNE_POUCH_QUANTITY_1,
			VarbitID.RUNE_POUCH_TYPE_2 -> VarbitID.RUNE_POUCH_QUANTITY_2,
			VarbitID.RUNE_POUCH_TYPE_3 -> VarbitID.RUNE_POUCH_QUANTITY_3,
			VarbitID.RUNE_POUCH_TYPE_4 -> VarbitID.RUNE_POUCH_QUANTITY_4,
			VarbitID.RUNE_POUCH_TYPE_5 -> VarbitID.RUNE_POUCH_QUANTITY_5,
		  VarbitID.RUNE_POUCH_TYPE_6 -> VarbitID.RUNE_POUCH_QUANTITY_6
		)
			.map{
				case (tid, qid) => client.getVarbitValue(tid) -> client.getVarbitValue(qid)
			}
			.flatMap {
				case (t, q) => OldRune.getRuneFromIndex(client.getVarbitValue(t)).toScala.zip(Option(client.getVarbitValue(q)).filter(_>0))
			}
			.toMap
	}

	val equipableUnlimitedRunes: Map[Int, OldRune] = List.apply[(OldRune, List[Int])](
		OldRune.FIRE -> List(ItemID.STAFF_OF_FIRE, ItemID.FIRE_BATTLESTAFF, ItemID.MYSTIC_FIRE_STAFF, ItemID.TOME_OF_FIRE),
		OldRune.WATER -> List(ItemID.STAFF_OF_WATER, ItemID.WATER_BATTLESTAFF, ItemID.MYSTIC_WATER_STAFF, ItemID.TOME_OF_WATER),
		OldRune.EARTH -> List(ItemID.STAFF_OF_EARTH, ItemID.EARTH_BATTLESTAFF, ItemID.MYSTIC_EARTH_STAFF, ItemID.TOME_OF_EARTH),
		OldRune.AIR -> List(ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF),
		OldRune.DUST -> List(ItemID.DUST_BATTLESTAFF, ItemID.MYSTIC_DUST_BATTLESTAFF),
		OldRune.STEAM -> List(ItemID.STEAM_BATTLESTAFF, ItemID.MYSTIC_STEAM_BATTLESTAFF),
		OldRune.LAVA -> List(ItemID.LAVA_BATTLESTAFF, ItemID.MYSTIC_LAVA_STAFF),
		OldRune.MIST -> List(ItemID.MIST_BATTLESTAFF, ItemID.MYSTIC_MIST_BATTLESTAFF),
		OldRune.MUD -> List(ItemID.MUD_BATTLESTAFF, ItemID.MYSTIC_MUD_STAFF),
		OldRune.SMOKE -> List(ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_SMOKE_BATTLESTAFF)
	).flatMap(e => e._2.map(_ -> e._1)).flatMap{
		case (itemId, rune) => ItemVariationMapping.getVariations(itemId).asScala.map(i => i.toInt -> rune)
	}.toMap

	def getInventoryRuneContents: Map[OldRune, Int] = {
//		val y = Inventory.search.withId(Rune.values.toList.map(r=>r.getItemId) *).result().asScala.toList
//		y.flatMap(w => {
//			Rune.getRuneFromItemId(w.getItemId).toScala.map(r =>r -> w.getItemQuantity)
//		}).toMap
		client.getItemContainer(InventoryID.INV).getItems.toList
			.flatMap(i => OldRune.getRuneFromItemId(i.getId).toScala.map(a => a-> i.getQuantity)).toMap
	}

	def getEquippedUnlimitedRunes: Set[OldRune] = {
//		Equipment.search().filter(eiqw => equipableUnlimitedRunes.contains(eiqw.getItemId)).result().asScala.toList
//			.map(eiqw => equipableUnlimitedRunes(eiqw.getItemId)).toSet
		Set.empty
	}

	def getRunes: Map[OldRune, Int] = {
		val runepouch = getRunePouchContents
		val inventory = getInventoryRuneContents
		val equiped = getEquippedUnlimitedRunes
		OldRune.values.flatMap(r => Option(runepouch.getOrElse(r, 0) + inventory.getOrElse(r, 0)).map(q => if(equiped.contains(r)) Integer.MAX_VALUE else q).filter(_ > 0).map(q => r -> q)).toMap
	}
}
