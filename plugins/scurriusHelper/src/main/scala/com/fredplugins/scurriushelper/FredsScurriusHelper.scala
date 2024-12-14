package com.fredplugins.scurriushelper

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.scurriushelper.helpers.ScurriusLogic
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.{ChatMessageType, Client, GameState, GraphicsObject, InventoryID, Item, ItemContainer, NPC, Prayer, Projectile, TileObject}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger

import java.awt.Font
import scala.jdk.StreamConverters.StreamHasToScala
import java.util
import java.util.stream.Collectors
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.util.{Random, Try}
import scala.util.chaining.*

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Scurrius Helper V2</html>",
	description = "Dodges Scurrius' falling ceiling attack and re-attacks",
	tags =  Array("pvm", "scurrius", "prayer", "helper", "maps"),
	conflicts = Array("<html><font color=\"#32CD32\">Lucid </font>Scurrius Helper</html>")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsScurriusHelper() extends Plugin {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsScurriusHelperConfig = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val panel: FredsScurriusPanel = null
	@Inject private val overlay: FredsScurriusOverlay = null

	given Client = client
	given FredsScurriusHelperConfig = config
	lazy val bossLogics: Seq[BossToolTrait] = Seq(
		ScurriusLogic()
	)

	@Provides
	def getConfig(configManager: ConfigManager): FredsScurriusHelperConfig = {
		configManager.getConfig[FredsScurriusHelperConfig](classOf[FredsScurriusHelperConfig])
	}


	private def resetState(): Unit = {
		bossLogics.foreach(_.resetState())
	}

	override protected def startUp(): Unit = {
		resetState()
		bossLogics.foreach(bl => {
			eventBus.register(bl)
		})
		overlayManager.add(panel)
//		overlayManager.add(overlay)
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		bossLogics.foreach(bl => {
			eventBus.unregister(bl)
		})
//		overlayManager.remove(overlay)
		resetState()
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if(e.getGroup == FredsScurriusHelperConfig.GroupName) {
			e.getKey match {
				case "fontSize" | "fontBold" => {
					overlay.Cache.cachedFont = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), config.getFontSize)
					overlay.Cache.countdownFont = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), (config.getFontSize * 1.5).toInt)
				}
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}
}