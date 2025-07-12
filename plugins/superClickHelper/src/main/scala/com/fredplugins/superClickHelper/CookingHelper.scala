package com.fredplugins.superClickHelper

import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getParentMenu, getTileObjectOpt, getWorldLocationOpt, isNpcAction, isRuneliteAction, isTileObjectAction, prettyString}
import com.fredplugins.common.extensions.TextExtensions.*
import com.fredplugins.common.extensions.WidgetExtensions.{getChildId, getChildIdx, getGroupId}
import com.fredplugins.common.queries.InventoryItemQuery
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.superClickHelper.CookingHelper.cookedFishIds
import com.fredplugins.superClickHelper.CookingHelper.rawFishIds
import com.fredplugins.superClickHelper.CookingHelper.stoveObjIds
import com.lucidplugins.api.item.SlottedItem
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.collections.TileItems
import ethanApiPlugin.collections.Widgets
import interactionApi.InventoryInteraction
import net.runelite.api.Client
import net.runelite.api.Item
import net.runelite.api.ItemContainer
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.events.GameTick
import net.runelite.api.events.ItemContainerChanged
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOpened
import net.runelite.api.events.PostMenuSort
import net.runelite.api.gameval.InventoryID
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.{ItemID, ObjectID}
import net.runelite.api.widgets.Widget
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.util.ColorUtil
import net.runelite.client.util.Text as TextUtil

import java.awt.Color
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
object CookingHelper {
	val rawFishIds: Seq[Int] = List(ItemID.RAW_MONKFISH, ItemID.RAW_SHARK, ItemID.TBWT_RAW_KARAMBWAN, ItemID.HUNTING_ANTELOPESUN_MEAT)
	val cookedFishIds: Seq[Int] = List(ItemID.MONKFISH, ItemID.SHARK, ItemID.TBWT_COOKED_KARAMBWAN, ItemID.ANTELOPESUN_COOKED)
	val stoveObjIds: Seq[Int] = List(ObjectID.DS2_GUILD_COOKING_RANGE, ObjectID.IZNOT_CLAY_RANGE)
//	val allFishIds: Seq[Int] = Seq(rawFishIds,cookedFishIds).flatten
}
class CookingHelper(plugin: SuperClickerPlugin, client: Client, clientThread: ClientThread) {
	private var rawFishCached: Seq[SlottedItem] = Seq.empty
	private var cookedFishCached: Seq[SlottedItem] = Seq.empty

	given Client = client
	private def updateInventoryCache(): Unit = {
		def internalUpdateCache(): Seq[SlottedItem] ={
			if(client.isClientThread) {
				client.getItemContainer(InventoryID.INV).pipe(c => {
					for {
						i <- 0 until c.size
						item <- Option(c.getItem(i))
					} yield new SlottedItem(item, i)
				})
			} else clientThread.runOnClientThread(() => internalUpdateCache())
		}
		val allItems = internalUpdateCache()
		rawFishCached = allItems.filter(si => rawFishIds.contains(si.getItem.getId))
		cookedFishCached = allItems.filter(si => cookedFishIds.contains(si.getItem.getId))
		dropFishIndex = 0
		didPickup = false
	}

//	@Subscribe()
//	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
//		if(event.getContainerId == InventoryID.INV) updateInventoryCache()
//	}

	@Subscribe()
	def onGameTick(event: GameTick): Unit = {
		updateInventoryCache()
	}

	var dropFishIndex: Int    = 0
	var didPickup    :Boolean = false

	@Subscribe(priority = -20)
	def onMenuEntryAdded(menuEntryAdded: MenuEntryAdded): Unit = {
		val entryToModify: Option[MenuEntry] = Option(menuEntryAdded.getMenuEntry).filter(me => me.getTileObjectOpt.exists(_.getId.pipe(stoveObjIds.contains)) && TextUtil.standardize(me.getOption).equalsIgnoreCase("Cook"))
		entryToModify.foreach(me => {
			val parent = me.getParentMenu
			if (rawFishCached.size - dropFishIndex > 1) {
//				parent.getMenuEntries.indexOf(me)
//				parent.removeMenuEntry(me)
//				val toChange = parent.getMenuEntries
//				val toChangeIdx = toChange.indexOf(me)
//				toChange(toChangeIdx) = parent.crea
				parent.createMenuEntry(-1).setType(MenuAction.RUNELITE).setOption("Drop".colored(Color.PINK)).onClick(event => {
					if(InventoryInteraction.useItem(w => new SlottedItem(w.getItemId, w.getItemQuantity, w.getChildIdx()).equals(rawFishCached.dropRight(dropFishIndex).last), "Drop")){
						dropFishIndex += 1
						plugin.sendChatMessage("Cooking Helper")(event.prettyString())
					}
				}).tap(plugin.priorityMenuEntries.addOne(_))
			} else if(rawFishCached.size - dropFishIndex == 1 || didPickup==true) {
				val oldOptionRecolored = me.getOption.pipe(TextUtil.removeTags(_)).colored(Color.ORANGE)
				val oldCallback = me.onClick()
				me.setOption(oldOptionRecolored).onClick(event => {
					if(oldCallback != null) oldCallback.accept(event)
					plugin.sendChatMessage("Cooking Helper")(event.prettyString())
				})
			} else {
				val groundItemRaw = TileItems.search().filter(t => rawFishIds.contains(t.getTileItem.getId)).nearestToPlayer().toScala
				if(groundItemRaw.nonEmpty && rawFishCached.isEmpty) {
					parent.createMenuEntry(-1).setType(MenuAction.RUNELITE).setOption("Pick up".colored(Color.PINK)).onClick(event => {
						groundItemRaw.get.interact(false)
						didPickup = true
						plugin.sendChatMessage("Cooking Helper")(event.prettyString())
					}).tap(plugin.priorityMenuEntries.addOne(_))
				}
			}
		})
	}
//
//	@Subscribe(priority = -20)
//	def onPostMenuSort(event: PostMenuSort): Unit = {
//		if(!client.isMenuOpen) {
//			val menu = client.getMenu
//
//			val (inventoryWidgetMenuEntriesByWidget: Array[MenuEntry], remainingStock: Array[MenuEntry]) = menu.getMenuEntries.partition(me => {
//				Option(me.getWidget).exists(mew => mew.getGroupId() == InterfaceID.INVENTORY && mew.getChildId() == 0 && rawFishCached.exists(xxx => xxx.getSlot == mew.getChildIdx() && xxx.getItem.getId == mew.getItemId && xxx.getItem.getQuantity == mew.getItemQuantity))
//			}).pipe((a, b) => a.groupBy(me => me.getItemId).map{(itemId, menuentries) => (itemId, menuentries.groupBy(me => me.getWidget.getChildIdx()))}
//				.toList.sortBy(_._1).map(aj => aj._1 -> aj._2.toList.sortBy(_._1)).flatMap(_._2.flatMap(_._2)).sortBy(me => me.getOption.equalsIgnoreCase(if(rawFishCached.size > 1) "Drop" else "Use")).toArray -> b
//			)
//
////				.map(u => u._1 -> u._2.groupBy(_.getWidget.getIndex))
//
////			val toModifyMenuEntries = inventoryWidgetMenuEntriesByWidget.toList.sortBy(_._1).map(aj => aj._1 -> aj._2.toList.sortBy(_._1)).flatMap(_._2.flatMap(_._2))
//			menu.setMenuEntries(inventoryWidgetMenuEntriesByWidget.zipWithIndex.map((me, idx) => if (idx == inventoryWidgetMenuEntriesByWidget.size - 1) {
//				s"${ColorUtil.colorTag(Color.PINK)}${me.getOption}${ColorUtil.CLOSING_COLOR_TAG}".pipe(opt => me.setOption(opt).pipe(mmme => {
//					mmme.setType(mmme.getType match {
//						case MenuAction.CC_OP | MenuAction.CC_OP_LOW_PRIORITY => MenuAction.CC_OP
//						case o => o
//					})
//				}))
//			} else me).prependedAll(remainingStock))
//		}
//	}

//	@Subscribe()
//	def onMenuEntryAdded(menuEntryAdded: MenuEntryAdded): Unit = {
//		val me: MenuEntry = menuEntryAdded.getMenuEntry
//		for{
//			widget <- Option(me.getWidget) if widget.getGroupId() == InterfaceID.INVENTORY
//			widget
//		}
//		if(menuEntryAdded.getMenuEntry.getType
//		if(rawFishCached.size == 1) {
//
//		}
//	}
}
