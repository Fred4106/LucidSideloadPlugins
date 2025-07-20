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
import net.runelite.client.events.ExternalPluginsChanged
import net.runelite.client.events.PluginChanged
import net.runelite.client.plugins.PluginManager
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


class HallowedSceptureHelper(val plugin: SuperClickerPlugin) extends Logging("DEBUG") {
	private def client: Client = plugin.client
	private def eventBus: EventBus = plugin.eventBus
	private def clientThread: ClientThread= plugin.clientThread
	private def pluginManager: PluginManager = plugin.pluginManager

	@Subscribe(priority = 1000.0f)
	def externalPluginsChanged(event: ExternalPluginsChanged): Unit = {
//		if(!pluginManager.getPlugins.asScala.toList.exists(p => p.getName.equalsIgnoreCase("Freds Super Clicker"))) {
		if (!pluginManager.getPlugins.asScala.toList.contains(plugin) && eventBus.isRegistered(this)) {
			log.debug("[externalPluginsChanged] Unregistering HallowedSceptureHelper")
			eventBus.unregister(this)
		}
	}
	@Subscribe(priority = 1000.0f)
	def onPluginChanged(event: PluginChanged): Unit = {
		if(event.getPlugin == plugin && !event.isLoaded && eventBus.isRegistered(this)) {
			log.debug("[onPluginChanged] Unregistering HallowedSceptureHelper")
			eventBus.unregister(this)
		}
	}
}
