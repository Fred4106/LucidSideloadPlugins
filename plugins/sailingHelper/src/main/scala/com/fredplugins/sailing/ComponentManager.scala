package com.fredplugins.sailing

import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.inject.Binder
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.Client
import net.runelite.api.coords.WorldPoint
import net.runelite.client.RuneLite
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDependency
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.infobox.InfoBox
import net.runelite.client.ui.overlay.infobox.InfoBoxManager
import net.runelite.client.util.GameEventManager
import org.slf4j.Logger

import java.io.IOException
import java.io.InputStreamReader
import scala.collection.mutable
import scala.compiletime.constValue
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import scala.util.Failure
import scala.util.Random
import scala.util.Success
import scala.util.Try

class ComponentManager @Inject()(val client: Client, val eventBus: EventBus, val overlayManager: OverlayManager, val infoBoxManager: InfoBoxManager, val gameEventManager: GameEventManager, val config: FredsSailingConfig, val components: Set[PluginLifecycleComponent]) {

	private val states: mutable.HashMap[PluginLifecycleComponent, Boolean] = mutable.HashMap.empty[PluginLifecycleComponent, Boolean]

	def onPluginStart(): Unit = {
		eventBus.register(this)
		components.foreach(c => states.put(c, false))
		revalidateComponentStates()
	}

	def onPluginStop(): Unit = {
		eventBus.unregister(this)
		components.filter(c => states.getOrElse(c, false)).foreach(this.tryShutDown)
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if (!FredsSailingConfig.GROUP.equals(e.getGroup)) return
		revalidateComponentStates()
	}

	private def revalidateComponentStates(): Unit = {
		components
			.filter(c => {
				c.isEnabled != states.getOrElse(c, false)
			})
			.foreach(c => {
				val m: PluginLifecycleComponent => Unit = if(c.isEnabled) tryStartUp else tryShutDown
				m(c)
			})
	}

	private def tryStartUp(component: PluginLifecycleComponent): Unit = {
		if (states(component)) return
		if (log.isDebugEnabled) log.debug("Enabling Sailing component [{}]", component.getClass.getName)
		try {
			component.startUp()
			eventBus.register(component)
			if (component.isInstanceOf[Overlay]) overlayManager.add(component.asInstanceOf[Overlay])
			if (component.isInstanceOf[InfoBox]) infoBoxManager.addInfoBox(component.asInstanceOf[InfoBox])
			gameEventManager.simulateGameEvents(component)
			states.put(component, true)
		} catch {
			case e: Throwable =>
				log.error("Failed to start Sailing component [{}]", component.getClass.getName, e)
		}
	}

	private def tryShutDown(component: PluginLifecycleComponent): Unit = {
		eventBus.unregister(component)
		if (component.isInstanceOf[Overlay]) overlayManager.remove(component.asInstanceOf[Overlay])
		if (component.isInstanceOf[InfoBox]) infoBoxManager.removeInfoBox(component.asInstanceOf[InfoBox])
		if (!states(component)) return
		if (log.isDebugEnabled) log.debug("Disabling Sailing component [{}]", component.getClass.getName)
		try component.shutDown()
		catch {
			case e: Throwable =>
				log.error("Failed to cleanly shut down Sailing component [{}]", component.getClass.getName)
		}
		finally states.put(component, false)
	}
}
