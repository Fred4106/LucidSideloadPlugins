package com.fredplugins.pvmHelper

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper.helpers.ScurriusLogic
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
	name = "<html><font color=\"#32C8CD\">Freds</font> Pvm Helper</html>",
	description = "Provides some auto movement and prayer help for limited set of bosses",
	tags =  Array("pvm", "scurrius", "prayer", "helper", "maps")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsPvmHelper() extends Plugin {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsPvmHelperConfig = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val configManager: ConfigManager = null
	@Inject private val panel: FredsPvmHelperPanel = null
	@Inject private val overlay: FredsPvmHelperOverlay = null

	given Client = client
	given ConfigManager = configManager
	given FredsPvmHelperConfig = config
	lazy val bossLogics: Seq[BossToolTrait] = Seq(
		ScurriusLogic()
	)

	@Provides
	def getConfig(configManager: ConfigManager): FredsPvmHelperConfig = {
		configManager.getConfig[FredsPvmHelperConfig](classOf[FredsPvmHelperConfig])
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
		if(e.getGroup == FredsPvmHelperConfig.GroupName) {
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