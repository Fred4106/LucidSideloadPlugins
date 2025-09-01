package com.fredplugins.common.services

import com.fredplugins.common.constants.magic.BasicRune
import com.fredplugins.common.constants.magic.SEquipable
import com.fredplugins.common.constants.magic.SEquipables
import com.fredplugins.common.constants.magic.SRune
import com.fredplugins.common.constants.magic.SRunes
import com.fredplugins.common.magic.RuneChanges
import com.fredplugins.common.services.CastSuppliesTrackerService.RUNE_POUCH_VARBITS
import com.fredplugins.common.services.CastSuppliesTrackerService.isRelevantItemContainer
import com.fredplugins.common.services.CastSuppliesTrackerService.isRelevantVarbit

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.Inject
import com.google.inject.Singleton
import net.runelite.api.Client
import net.runelite.api.EnumID
import net.runelite.api.GameState
import net.runelite.api.Item
import net.runelite.api.ItemContainer
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.ItemContainerChanged
import net.runelite.api.events.PostClientTick
import net.runelite.api.events.VarbitChanged
import net.runelite.api.gameval.InventoryID
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.VarbitID
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe

import scala.jdk.OptionConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object CastSuppliesTrackerService {
	private val relevantContainerIds = List(InventoryID.INV, InventoryID.WORN)
	inline def isRelevantItemContainer(container: ItemContainer)(using c: Client): Boolean = {
		Option(container).map(_.getId).exists(relevantContainerIds.contains)
	}
	private val RUNE_POUCH_VARBITS: List[(Int, Int)] = List(
		VarbitID.RUNE_POUCH_TYPE_1 -> VarbitID.RUNE_POUCH_QUANTITY_1,
		VarbitID.RUNE_POUCH_TYPE_2 -> VarbitID.RUNE_POUCH_QUANTITY_2,
		VarbitID.RUNE_POUCH_TYPE_3 -> VarbitID.RUNE_POUCH_QUANTITY_3,
		VarbitID.RUNE_POUCH_TYPE_4 -> VarbitID.RUNE_POUCH_QUANTITY_4,
	)

//	private val RUNE_POUCH_RUNE_VARBITS = List(VarbitID.RUNE_POUCH_TYPE_1, VarbitID.RUNE_POUCH_TYPE_2, VarbitID.RUNE_POUCH_TYPE_3, VarbitID.RUNE_POUCH_TYPE_4)
//	private val RUNE_POUCH_AMOUNT_VARBITS = List(VarbitID.RUNE_POUCH_QUANTITY_1, VarbitID.RUNE_POUCH_QUANTITY_2, VarbitID.RUNE_POUCH_QUANTITY_3, VarbitID.RUNE_POUCH_QUANTITY_4)
	inline def isRelevantVarbit(varbitId: Int)(using c: Client): Boolean = {
		RUNE_POUCH_VARBITS.flatMap {
			case (tpe, qty) => Seq(tpe, qty)
		}.appended(VarbitID.FOUNTAIN_OF_RUNE_ACTIVE).contains(varbitId)
	}
}

@Singleton
class CastSuppliesTrackerService @Inject()(val client: Client, val clientThread: ClientThread, val eventBus: EventBus) extends ShimUtils.Logging("DEBUG") {
	given Client = client
	private object State {
		var active: Boolean = false
		var runeCount: Map[SRune, Int]                = Map.empty[SRune, Int]
		var equipables: Set[SEquipable]           = Set.empty[SEquipable]

//		var lastChanges: RuneChanges = RuneChanges()
		var requiresPostUpdate       = false
	}

	def start(): Unit = {
		if (!State.active) {
			State.active = true
			State.requiresPostUpdate = true
			eventBus.register(this)
		}
	}

	def stop(): Unit = {
		if (State.active) {
			eventBus.unregister(this)
			State.requiresPostUpdate = false
			State.active = false
		}
	}

	private def readRunePouch(isDivine: Boolean): Seq[(SRune, Int)] = {
		val runepouchEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE)
		inline def pouchSize     = if (isDivine) 4 else 3
		RUNE_POUCH_VARBITS.take(pouchSize).map {
			case (idVarbit, qtyVarbit) => client.getVarbitValue(idVarbit) -> client.getVarbitValue(qtyVarbit)
		}.filter {
			case (id, qty) => id > 0 && qty > 0
		}.flatMap {
			case (0, _) => Option.empty[(SRune, Int)]
			case (_, 0) => Option.empty[(SRune, Int)]
			case (id, qty) => Try(SRunes.findForRunepouchType(id)).toOption.map(r => r -> qty)
		}
	}

	private def updateRuneCount() : Unit = {
		val lastCount = State.runeCount
		val lastEquipables = State.equipables

		Option(updateCurrentRuneCount().pipe(x =>
			State.runeCount = x._1
			State.equipables = x._2
			val delta = Seq(lastCount, x._1).flatMap(_.keySet).distinct.sortBy(SRunes.indexOf).map {
				case key => key -> (State.runeCount.getOrElse(key, 0) - lastCount.getOrElse(key, 0) )
			}.filterNot(_._2 == 0).toMap

			val sharedEquips = lastEquipables.intersect(State.equipables)
			val removedEquips = lastEquipables.filterNot(sharedEquips.contains)
			val addedEquips = State.equipables.filterNot(sharedEquips.contains)

			(delta, addedEquips, removedEquips)
//			lastCount.toList.map(u => u._1 -> u._2 * -1).map(u => {
//				State.runeCount.getOrElse(u._1, 0) + u._2
//			})//.appendedAll(x._1.toList)
		)).filter(x => x._1.nonEmpty || (x._2 ++ x._3).nonEmpty).foreach{
			case (delta, addedEquips, removedEquips) => {
				log.debug("castSuppliesChange(runeDelta={}, equipablesDelta={})", delta, addedEquips.map(a => s"+${a}").toList.appendedAll(removedEquips.map(b => s"-${b}").toList))
//				log.debug(s"castSuppliesState(runes={}, quipables={})", State.runeCount, State.equipables)
			}
		}
	}


	private def updateCurrentRuneCount(): (Map[SRune, Int], Set[SEquipable])  = {
//		val lastCount = State.runeCount
//		val lastEquipables = State.equipables

		val inventory = client.getItemContainer(InventoryID.INV)
		val inventoryItems = Option(inventory).map(_.getItems).getOrElse(Array.empty[Item])

		val inventoryReport: (Map[SRune, Int], Set[SEquipable]) = inventoryItems.map(i => (i.getId, i.getQuantity)).toList.map {
			case (ItemID.BH_RUNE_POUCH, _) => readRunePouch(false)         -> Set.empty[SEquipable]
			case (ItemID.BH_RUNE_POUCH_TROUVER, _) => readRunePouch(false)         -> Set.empty[SEquipable]
			case (ItemID.DIVINE_RUNE_POUCH, _) => readRunePouch(true)         -> Set.empty[SEquipable]
			case (ItemID.DIVINE_RUNE_POUCH_TROUVER, _) => readRunePouch(true)         -> Set.empty[SEquipable]
			case (ItemID.BR_RUNE_REPLACEMENT, _) => (Seq(SRunes.Water_Rune, SRunes.Death_Rune, SRunes.Blood_Rune, SRunes.Soul_Rune).map(br => br -> Int.MaxValue)) -> Set.empty[SEquipable]
			case (ItemID.PVPA_RUNE_REPLACEMENT, _) => SRunes.basicValues.map(br => br -> Int.MaxValue) -> Set.empty[SEquipable]
			case (itemId, itemQty) => {
				if (SRunes.isRuneItemId(itemId)) {
					val a1: (List[(SRune, Int)], Set[SEquipable]) = SRunes.values.filter(sr => sr.itemIds.contains(itemId)).map(sr => sr -> itemQty).toList -> Set.empty[SEquipable]
					a1
				} else if(SEquipables.isEquipmentItemId(itemId, true)) {
					val temp                                     = SEquipables.worksInInventoryValues.filter(equ => equ.itemIds.contains(itemId))
					val a2: (Seq[(SRune, Int)], Set[SEquipable]) = temp.flatMap(_.unlimitedRunes.map(sr => sr->Int.MaxValue)) -> temp.toSet
//						.map(seq=> {
//							seq.unlimitedRunes.toList.map(sr => sr -> Int.MaxValue) -> seq
//						})
					a2
				} else {
					val a3: (List[(SRune, Int)], Set[SEquipable]) = List.empty[(SRune, Int)] -> Set.empty[SEquipable]
					a3
				}
			}
		}.pipe(u => {
			val x1: Map[SRune, Int] =  u.flatMap(_._1).groupMap(x => x._1)(x => x._2).toList
				.map {
					case (rune, ints) => rune -> ints.map(_.toLong).reduce((a, b) => Math.min(Int.MaxValue.toLong, a + b)).toInt
				}
				.sortBy(_._1.runepouchId).toMap
			val x2: Set[SEquipable] = u.flatMap(_._2).toSet
			x1 -> x2
		})

		val equipment      = client.getItemContainer(InventoryID.WORN)
		val equipmentItems                                      = Option(equipment).map(_.getItems).getOrElse(Array.empty[Item])
		val equipmentReport: (Map[SRune, Int], Set[SEquipable]) = equipmentItems.map(i => (i.getId, i.getQuantity)).toList.collect {
			case (itemId, itemQty) if (SEquipables.isEquipmentItemId(itemId, false)) => {
					val temp                                     = SEquipables.values.filter(equ => equ.itemIds.contains(itemId))
					val a2: (Seq[(SRune, Int)], Set[SEquipable]) = temp.flatMap(_.unlimitedRunes.map(sr => sr -> Int.MaxValue)) -> temp.toSet
					//						.map(seq=> {
					//							seq.unlimitedRunes.toList.map(sr => sr -> Int.MaxValue) -> seq
					//						})
					a2
			}
		}.pipe(u => {
			val x1: Map[SRune, Int] =  u.flatMap(_._1).groupMap(x => x._1)(x => x._2).toList
				.map {
					case (rune, ints) => rune -> ints.map(_.toLong).reduce((a, b) => Math.min(Int.MaxValue.toLong, a + b)).toInt
				}
				.sortBy(_._1.runepouchId).toMap
			val x2: Set[SEquipable] = u.flatMap(_._2).toSet
			x1 -> x2
		})

		val fountainOfRuneReport: (Map[SRune, Int], Set[SEquipable]) = Option.when(client.getVarbitValue(VarbitID.FOUNTAIN_OF_RUNE_ACTIVE) == 1){
			SRunes.basicValues.map(br => br -> Int.MaxValue).toMap -> Set.empty[SEquipable]
		}.getOrElse(Map.empty[SRune, Int] -> Set.empty[SEquipable])

		val reports = Seq(inventoryReport, equipmentReport, fountainOfRuneReport)

		val nRuneCount =  reports.flatMap(_._1).groupMap(_._1)(_._2).map {
			case (rune, qtys) => rune -> qtys.map(_.toLong).reduce((a, b) => Math.min(Int.MaxValue.toLong, a + b)).toInt
		}

		val nEquipables = reports.flatMap(_._2).sortBy(equ => SEquipables.values.indexOf(equ)).toSet
		nRuneCount -> nEquipables
	}

	@Subscribe
	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
		if (!State.active || !isRelevantItemContainer(event.getItemContainer)) return
		updateRuneCount()
	}

	@Subscribe def onVarbitChanged(event: VarbitChanged): Unit = {
		if (!State.active || State.requiresPostUpdate || !isRelevantVarbit(event.getVarbitId)) return
		State.requiresPostUpdate = true
	}

	@Subscribe def onPostClientTick(event: PostClientTick): Unit = {
		if (State.requiresPostUpdate && (client.getGameState eq GameState.LOGGED_IN)) {
			updateRuneCount()
			State.requiresPostUpdate = false
		}
	}
}
