package com.fredplugins.superClickHelper

import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.TextExtensions.*
import com.fredplugins.common.extensions.WidgetExtensions.*
import com.fredplugins.common.utils.ShimUtils.Logging
import com.google.inject.Inject
import com.google.inject.Singleton
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
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.events.ExternalPluginsChanged
import net.runelite.client.events.PluginChanged
import net.runelite.client.plugins.PluginManager
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPosition
import net.runelite.client.util.ColorUtil
import net.runelite.client.util.Text as TextUtil

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.Random
import scala.util.Try
import scala.util.chaining.*


case class InventoryItem(slot: Int, id: Int, qty: Int) {}

class InventoryMonitorService(plugin: SuperClickerPlugin) extends MonitorService(plugin, "DEBUG") {
//	private def client: Client = plugin.client
//	private def eventBus: EventBus = plugin.eventBus
//	private def clientThread: ClientThread= plugin.clientThread
//	private def pluginManager: PluginManager = plugin.pluginManager
	private val itemDefMap: scala.collection.mutable.Map[Int, ItemComposition] = scala.collection.mutable.HashMap.empty[Int, ItemComposition]
	private var cachedItems: Map[Int, InventoryItem] = Map.empty[Int, InventoryItem]

	private def getItemDef(id: Int): ItemComposition = {
		itemDefMap.getOrElseUpdate(id, clientThread.runOnClientThread(() => {
			client.getItemDefinition(id)
		}))
	}

	extension (ii: InventoryItem) {
		def definition: ItemComposition = getItemDef(ii.id)
	}

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
			case (s, Some(o), Some(n)) if o.id == n.id => s"Count ${n} changed by ${n.qty - o.qty}"
			case (s, Some(o), Some(n)) => s"Replaced ${o} with ${n}"
			case (s, None, Some(n)) => s"Added ${n}"
			case (s, None, None) => s"Impossible case ${s}"
		}.pipe(seq => Option.when(seq.nonEmpty)(seq.map(s => s"  ${s}").prepended(s"Inventory changed: ${client.getTickCount}").appended(""))).map(_.mkString("\n"))

		report.foreach(r => log.debug(r))
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == SuperClickHelperConfig.GroupName) {
			e.getKey match {
				case "debugInventoryMonitorService" => {
					Option.when(config.isDebugInventoryMonitorService)(overlayManager.add(_)).getOrElse(overlayManager.remove(_))
						.apply(InventoryMonitorOverlay)
				}
2			}
		}
	}

//	@Subscribe(priority = 1000.0f)
//	def externalPluginsChanged(event: ExternalPluginsChanged): Unit = {
//		if (!pluginManager.getPlugins.asScala.toList.contains(plugin) && eventBus.isRegistered(this)) {
//			log.debug("[externalPluginsChanged] Unregistering InventoryMonitorService")
//			eventBus.unregister(this)
//		}
//	}
//	@Subscribe(priority = 1000.0f)
//	def onPluginChanged(event: PluginChanged): Unit = {
//		if(event.getPlugin == plugin && !event.isLoaded && eventBus.isRegistered(this)) {
//			log.debug("[onPluginChanged] Unregistering InventoryMonitorService")
//			eventBus.unregister(this)
//		}
//	}

	private object InventoryMonitorOverlay extends Overlay(plugin) {
		setPosition(OverlayPosition.DYNAMIC)
		setLayer(OverlayLayer.ABOVE_WIDGETS)
		setPriority(Overlay.PRIORITY_HIGHEST)
		override def render(graphics: Graphics2D): Dimension = {
			Option(client.getWidget(InterfaceID.INVENTORY, 0))
				.filterNot(_.isHidden)
				.map(inventoryWidget => {
					val fm         = graphics.getFontMetrics
					inventoryWidget.getDynamicChildren.toList
						.filterNot(_.getItemId == 6512)
						.map(item => {
							val idText     = s"${item.getItemId}"
							val textBounds = fm.getStringBounds(idText, graphics)
							val slotBounds = item.getBounds
							(idText, textBounds, slotBounds)
					})
				})
				.getOrElse(List.empty)
				.foreach{
					case (str, textBounds, slotBounds) => {
						val textX = (slotBounds.getX + (slotBounds.getWidth / 2) - (textBounds.getWidth / 2)).toInt
						val textY = (slotBounds.getY + (slotBounds.getHeight / 2) + (textBounds.getHeight / 2)).toInt
						graphics.setColor(new Color(255, 255, 255, 65))
						graphics.fill(slotBounds)
						graphics.setColor(Color.BLACK)
						graphics.drawString(str, textX + 1, textY + 1)
						graphics.setColor(Color.YELLOW)
						graphics.drawString(str, textX, textY)
					}
				}
			null
		}
	}

	override protected def startService(): Unit = {
		if(config.isDebugInventoryMonitorService) {
			overlayManager.add(InventoryMonitorOverlay)
		}
//		eventBus.register(InventoryMonitorOverlay)
	}
	override protected def stopService(): Unit = {
		overlayManager.remove(InventoryMonitorOverlay)
//		eventBus.unregister(InventoryMonitorOverlay)
	}
}
