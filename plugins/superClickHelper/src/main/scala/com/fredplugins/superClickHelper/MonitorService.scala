package com.fredplugins.superClickHelper

import com.fredplugins.common.utils.ShimUtils.Logging
import net.runelite.api.Client
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ExternalPluginsChanged
import net.runelite.client.events.PluginChanged
import net.runelite.client.plugins.PluginManager
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

abstract class MonitorService(val plugin: SuperClickerPlugin, level: String = "DEBUG") extends Logging {
	protected final def client: Client = plugin.client
	protected final def eventBus: EventBus = plugin.eventBus
	protected final def clientThread: ClientThread= plugin.clientThread
	protected final def pluginManager: PluginManager = plugin.pluginManager
	protected final def overlayManager: OverlayManager = plugin.overlayManager
	protected final def config: SuperClickHelperConfig = plugin.config
	given ModelOutlineRenderer = plugin.getInjector.getInstance(classOf[ModelOutlineRenderer])
	given Client = client
	def name: String = this.getClass.getSimpleName

	protected def startService(): Unit
	protected def stopService(): Unit

	final def init(): Unit = {
		if(!eventBus.isRegistered(this)) eventBus.register(this)
		startService()
	}
	private final def teardown(): Unit = {
		eventBus.unregister(this)
		stopService()
	}

	@Subscribe(priority = 1000.0f)
	final def externalPluginsChanged(event: ExternalPluginsChanged): Unit = {
		if (!pluginManager.getPlugins.asScala.toList.contains(plugin) && eventBus.isRegistered(this)) {
			log.debug("[externalPluginsChanged] Unregistering {}", name)
			teardown()
		}
	}

	@Subscribe(priority = 1000.0f)
	final def onPluginChanged(event: PluginChanged): Unit = {
		if (event.getPlugin == plugin && !event.isLoaded && eventBus.isRegistered(this)) {
			log.debug("[onPluginChanged] Unregistering {}", name)
			teardown()
		}
	}
}