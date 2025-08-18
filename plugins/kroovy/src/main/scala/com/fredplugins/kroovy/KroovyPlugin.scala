package com.fredplugins.kroovy

import com.fredplugins.common.{Locatable, PrayerExtended}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.events.EventManager
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.coords.WorldPoint
import net.runelite.client.{Notifier, RuneLite}
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.game.{ItemManager, SpriteManager, WorldService}
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.{ClientToolbar, NavigationButton}
import net.runelite.client.ui.overlay.OverlayManager

import java.awt.image.BufferedImage
import java.util.concurrent.{Executors, ScheduledExecutorService}
import javax.swing.{ImageIcon, JFrame, JPanel, SwingUtilities, WindowConstants}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.existentials
import scala.reflect.{TypeTest, Typeable}
import scala.util.chaining.given
import net.runelite.api.{ChatMessageType, Client, InventoryID, Item, ItemContainer, MenuAction, NPC, NPCComposition, NpcID, NullNpcID, Prayer}
import net.runelite.api.events.{GameTick, GraphicsObjectCreated, ItemContainerChanged, MenuOptionClicked, NpcSpawned, VarbitChanged}

import scala.swing.Frame

@PluginDescriptor(
	name = "<html><font color=\"#20CD00\">Freds</font> Kroovy</html>",
	description = "Provides a scripting environment for runtime loadable mini-plugins",
	tags = Array(
		"scripting", "helper"
		, "utility", "scala"
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class KroovyPlugin extends Plugin with ShimUtils.Logging("DEBUG") {

	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: KroovyConfig = null
	@Inject val notifier: Notifier = null
	@Inject val configManager: ConfigManager = null
	@Inject private val eventBus: EventBus           = null
	@Inject private val eventManager: EventManager       = null
	@Inject private val spriteManager: SpriteManager = null
	@Inject private val itemManager: ItemManager = null
	@Inject private val overlayManager: OverlayManager = null

//	@Inject private val sBus: SEventBus = null
	@Inject private val worldService: WorldService = null
	@Inject private val clientToolbar: ClientToolbar= null

	given Client = RuneLite.getInjector.getInstance(classOf[Client])
	given ClientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])
	given EventBus = RuneLite.getInjector.getInstance(classOf[EventBus])
	given SpriteManager = RuneLite.getInjector.getInstance(classOf[SpriteManager])
	given ItemManager = RuneLite.getInjector.getInstance(classOf[ItemManager])
//	given SEventBus = RuneLite.getInjector.getInstance(classOf[SEventBus])
	given EventManager = RuneLite.getInjector.getInstance(classOf[EventManager])
//	val kFrame = RuneLite.getInjector.getInstance(classOf[KroovyEventBusFrame])

	var inventorySnapshot: List[(Int, Int, Int)] = List.empty

	@Provides
	def getConfig(configManager: ConfigManager): KroovyConfig = {
		configManager.getConfig[KroovyConfig](classOf[KroovyConfig])
	}
	@Subscribe
	def onConfigChanged(event: ConfigChanged): Unit = {
	}

	@Subscribe
	def onGameTick(event: GameTick): Unit = {
//		sBus.post(event)
	}
	@Subscribe
	def onNpcSpawned(event: NpcSpawned): Unit = {
//		sBus.post(event)
	}
	@Subscribe
	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
		def parseInventory(container: ItemContainer): List[(Int, Int, Int)] = {
			container.getItems.zipWithIndex.collect {
				case (i: Item, idx: Int) if i.getId != -1 && i.getQuantity != -1 => (idx, i.getId, i.getQuantity)
			}.toList
		}
		if (event.getContainerId == InventoryID.INVENTORY.getId) {
			val (qtyElements, addedElements, removedElements) = parseInventory(event.getItemContainer).pipe {
				cur => {
					(cur.diff(inventorySnapshot) -> inventorySnapshot.diff(cur)).pipe {
						case (addedElements, removedElements) => {
							addedElements.partition(added => removedElements.exists(removed => removed._1 == added._1 && removed._2
								== added._2)).pipe {
								case (qtyElements, realAddedElements) => {
									val (qtyMinusElements: List[(Int, Int, Int)], realRemovedElements: List[(Int, Int, Int)]) =
										removedElements.partition(r => qtyElements.exists(q => q._1 == r._1 && q._2 == r._2))
									//sharedElements1.contains(r))
									(qtyElements.map(q => (q._1, q._2, q._3 - qtyMinusElements.find(r => r._1 == q._1 && r._2 == q._2)
										.map(_._3).getOrElse(0))), realAddedElements, realRemovedElements)
								}
							}.tap(_ => inventorySnapshot = cur)
						}
					}
				}
			}

//			val str = List(
//				"qtyChanged" -> qtyElements,
//				"added" -> addedElements,
//				"removed" -> removedElements,
//			)
//				.filter(_._2.nonEmpty)
//				.map(u => s"${u._1}=${u._2}")
//				.mkString("\n\t", "\n\t", "\n")
//
//			log.debug(s"logStr: ${str}")
		}
	}

	@Subscribe
	def onVarbitChanged(event: VarbitChanged): Unit = {
		val varbitId = event.getVarbitId
		val value = event.getValue
	}

	@Subscribe
	def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		val spotAnimId = event.getGraphicsObject.getId
	}

	@Subscribe
	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked):Unit = {
		import com.fredplugins.common.extensions.MenuExtensions.{given, *}
		import Locatable.{*, given}

		val npcClickedOpt = menuOptionClicked.getMenuEntry.pipe(me => Option.when(me.isNpcAction)(me)).filter(me => me.getNpcOpt.exists(_.distanceTo(client.getLocalPlayer) < 5))
		npcClickedOpt.map(me => me.getType -> me.getNpc).foreach {
//			case (MenuAction.NPC_FIRST_OPTION, npc) => sBus.unregisterByOwner(npcService)
//			case (MenuAction.NPC_SECOND_OPTION, npc) => (_: SEventBus).unregisterAll[GameTick](npcService)
//			case (MenuAction.EXAMINE_NPC, npc) => {
//				gauntletNpcListener = if(npcService.forget(gauntletNpcListener)) null else gauntletNpcListener
//				sBus.unregisterByEvent[GameTick]()
//			}
			case (a, npc) =>
		}

		val examineClickedOpt = menuOptionClicked.getMenuEntry.pipe(me => Option.when(me.isExamineAction && !me.isNpcAction)(me))
//		examineClickedOpt.foreach(me => sBus.debug(s => log.debug("{}", s)))
	}
	//
//	private val navButton = NavigationButton.builder()
//			.icon(Icons.LOGO_ICON)
//			.priority(-100)
//			.tooltip("Kroovy")
//			.onClick(() => {
//				log.debug(s"toggling window ${if (debugFrame.visible) "visible" else "hidden"}")
//				debugFrame.visible = (!debugFrame.visible)
//				Thread.sleep(120)
//				log.debug(s"after click: Window is ${if (debugFrame.visible) "visible" else "hidden"}")
//			}).build()



//	private var fxButton: Option[NavigationButton] = None

//	private var frame: Option[Frame] = Option.empty

//	SEventBusFrame.get.open()
	private val StrongNpcs = Set(NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN)
	override protected def startUp(): Unit = {
		eventManager.register(KGauntlet)
	}

	override protected def shutDown(): Unit = {
		eventManager.unregister(KGauntlet)
	}
}