package com.fredplugins.pvmHelper

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper.helpers.ScurriusLogic
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.{ChatMessageType, Client, GameState, GraphicsObject, InventoryID, Item, ItemContainer, NPC, Prayer, Projectile, TileObject}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent}
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
class FredsPvmHelper() extends Plugin with BossToolTrait {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsPvmHelperConfig = null
	@Inject val notifier: Notifier = null

	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val configManager: ConfigManager = null
	//	@Inject private val overlay: FredsPvmHelperOverlay = null
	given Client = client

	private val panel: FredsPvmHelperPanel[FredsPvmHelper] = new FredsPvmHelperPanel(this){}

	@Provides
	def getConfig(configManager: ConfigManager): FredsPvmHelperConfig = {
		configManager.getConfig[FredsPvmHelperConfig](classOf[FredsPvmHelperConfig])
	}

	override def resetState(): Unit = {
	}


	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		resetState()
	}

	inline def getLocalPlayerWorldPoint: WorldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation)
	inline def getRegionId: Int = Try(getLocalPlayerWorldPoint.getRegionID).getOrElse(-1)

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {
		Seq(
			LineComponent.builder
				.left("RegionId")
				.right(s"${getRegionId}")
				.build
//			LineComponent.builder
//				.left("justDodged")
//				.right(s"${client}")
//				.build,
//			LineComponent.builder
//				.left("lastDodgeTick")
//				.right(s"${lastDodgeTick}")
//				.build,
//			LineComponent.builder
//				.left("lastRatTick")
//				.right(s"${lastRatTick}")
//				.build,
//			LineComponent.builder
//				.left("lastActivateTick")
//				.right(s"${lastActivateTick}")
//				.build
		)
	}
	override def inArea(): Boolean = true
}