package com.fredplugins.superClickHelper

import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.TextExtensions.*
import com.fredplugins.common.extensions.WidgetExtensions.*
import com.fredplugins.common.utils.ShimUtils.Logging
import com.google.inject.Inject
import net.runelite.api.Client
import net.runelite.api.Item
import net.runelite.api.ItemComposition
import net.runelite.api.ItemContainer
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.events.GameTick
import net.runelite.api.events.ItemContainerChanged
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOpened
import net.runelite.api.events.PostMenuSort
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.InventoryID
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.ObjectID
import net.runelite.api.widgets.Widget
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ExternalPluginsChanged
import net.runelite.client.events.PluginChanged
import net.runelite.client.util.ColorUtil
import net.runelite.client.util.Text as TextUtil

import java.awt.Color
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.Random
import scala.util.Try
import scala.util.chaining.*


case class InventoryItem(slot: Int, id: Int, qty: Int) {}


class InventoryMonitorService(plugin: SuperClickerPlugin) extends Logging("DEBUG") {
	import plugin.given

	private val itemDefMap: scala.collection.mutable.Map[Int, ItemComposition] = scala.collection.mutable.HashMap.empty[Int, ItemComposition]
	def getItemDef(id: Int): ItemComposition = {
		itemDefMap.getOrElseUpdate(id, plugin.clientThread.runOnClientThread(() => {
			given_Client.getItemDefinition(id)
		}))
	}

	extension (ii: InventoryItem) {
		def definition: ItemComposition = getItemDef(ii.id)
	}

	plugin.eventBus.register(this)

	private var cachedItems: Map[Int, InventoryItem] = plugin.clientThread.runOnClientThread(() => itemContainerToInventoryItems(given_Client.getItemContainer(InventoryID.INV)))
//	val sub = plugin.eventBus.register[PluginChanged](
//		classOf[PluginChanged], (e: PluginChanged) => {
//			if (e.getPlugin == plugin) {
//				if (e.isLoaded) plugin.eventBus.register(this)
//				else plugin.eventBus.unregister(this)
//			}
//		}, 0)

//	val sub = plugin.eventBus.register[ExternalPluginsChanged](
//		classOf[ExternalPluginsChanged], (e: ExternalPluginsChanged) => {
//			plugin.pluginManager.getSideloadedPlugins
//			if (e.getPlugin == plugin) {
//				if (e.isLoaded) plugin.eventBus.register(this)
//				else plugin.eventBus.unregister(this)
//			}
//		}, 0
//	)
//	plugin.eventBus.register[PluginChanged](classOf[PluginChanged], )

	private def itemContainerToInventoryItems(ic: ItemContainer): Map[Int, InventoryItem] = {
		assert(ic.getId == InventoryID.INV)
		(0 until ic.size()).flatMap(i => Option(ic.getItem(i)).map(item => {
			InventoryItem(i, item.getId, item.getQuantity)
		})).tap(xxx => xxx.map(_.id).distinct.foreach(getItemDef)).map(i => i.slot -> i).toMap
	}

	@Subscribe(priority = 1000.0f)
	def itemContainerChanged(event: ItemContainerChanged): Unit = {
		if(event.getContainerId != InventoryID.INV) return
		val copyOfCached = cachedItems
		cachedItems = itemContainerToInventoryItems(event.getItemContainer)
//		val itemsMap = items.map(i => i.slot -> i).toMap

		val slotsToCheck = (cachedItems.keySet ++ copyOfCached.keySet).toSeq.sorted.filterNot(slot => {
			cachedItems.get(slot) == copyOfCached.get(slot)
		})

		val report = slotsToCheck.map(s => (s, copyOfCached.get(s), cachedItems.get(s))).map{
			case (s, Some(o), None) => s"Removed ${o}"
			case (s, Some(o), Some(n)) if o.id == n.id => s"Count ${n} changed by ${o.qty - n.qty}"
			case (s, Some(o), Some(n)) => s"Replaced ${o} with ${n}"
			case (s, None, Some(n)) => s"Added ${n}"
			case (s, None, None) => s"Impossible case ${s}"
		}.pipe(seq => Option.when(seq.nonEmpty)(seq.map(s => s"  ${s}").prepended(s"Inventory changed: ${given_Client.getTickCount}").appended(""))).map(_.mkString("\n"))

		report.foreach(r => log.debug(r))
	}

	@Subscribe(priority = 1000.0f)
	def externalPluginsChanged(event: ExternalPluginsChanged): Unit = {
		if(!plugin.pluginManager.getPlugins.asScala.toList.contains(plugin)) {
			plugin.eventBus.unregister(this)
		}
	}
}
