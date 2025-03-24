package com.fredplugins.kroovy

import com.fredplugins.common.Locatable
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.services.{NpcFilter, NpcService}
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
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
import net.runelite.api.{Client, InventoryID, Item, ItemContainer, MenuAction, NPC, NpcID}
import net.runelite.api.events.{GameTick, GraphicsObjectCreated, ItemContainerChanged, MenuOptionClicked, NpcSpawned, VarbitChanged}

import scala.swing.Frame


case class GauntletNpc(npcType: String)(private val _wrapped: NPC) extends NpcFilter.NpcInstance {
	override def wrapped: NPC = _wrapped
}
//		class GauntletNpc(val wrapped: NPC) extends NpcFilter.NpcInstance {}
val gauntletNpcIds = Seq(
	("BAT", NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT),
	("RAT", NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT),
	("SPIDER", NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER),
	("SCORPION", NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION),
	("UNICORN", NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN),
	("WOLF", NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF),
	("BEAR", NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR),
	("DARK_BEAST", NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST),
	("DRAGON", NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON),
)
object GauntletNpcFilter extends NpcFilter[GauntletNpc](gauntletNpcIds.flatMap(a => Seq(a._2, a._3)) *) {
	override def transform(npc: NPC): GauntletNpc = {
		val npcType = gauntletNpcIds.find(gni => Seq(gni._2, gni._3).contains(npc.getId)).map(_._1).get
		GauntletNpc(npcType)(npc)
	}
}

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
	@Inject private val eventBus: EventBus = null
	@Inject private val spriteManager: SpriteManager = null
	@Inject private val itemManager: ItemManager = null
	@Inject private val overlayManager: OverlayManager = null

	@Inject private val sBus: SEventBus = null
	@Inject private val worldService: WorldService = null
	@Inject private val clientToolbar: ClientToolbar= null
	@Inject private val npcService: NpcService= null

	given Client = RuneLite.getInjector.getInstance(classOf[Client])
	given ClientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])
	given SpriteManager = RuneLite.getInjector.getInstance(classOf[SpriteManager])
	given ItemManager = RuneLite.getInjector.getInstance(classOf[ItemManager])
	given SEventBus = RuneLite.getInjector.getInstance(classOf[SEventBus])
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
		sBus.post(event)
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
		npcClickedOpt.map(me => me.getType -> me.getNpc).collect{
			case (MenuAction.NPC_FIRST_OPTION, npc) => (_: SEventBus).unregisterByOwner(npcService)
//			case (MenuAction.NPC_SECOND_OPTION, npc) => (_: SEventBus).unregisterAll[GameTick](npcService)
			case (MenuAction.EXAMINE_NPC, npc) => (_: SEventBus).unregisterByEvent[GameTick]()//(_: SEventBus).debug()
		}.foreach(in =>in(sBus))

		val examineClickedOpt = menuOptionClicked.getMenuEntry.pipe(me => Option.when(me.isExamineAction && !me.isNpcAction)(me))
		examineClickedOpt.foreach(me => sBus.debug())
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

	SEventBusFrame.get.open()
	override protected def startUp(): Unit = {
		npcService.init()
		npcService.register(GauntletNpcFilter)
		val r1 = sBus.register[GameTick, 0, "TestGroup1"](this)((t: GameTick) => log.trace(s"This - Gametick: ${client.getTickCount}"))
		val r2 = sBus.register[GameTick, 4, "Other"](npcService)((t: GameTick) => log.trace(s"This is also a gametick: ${client.getTickCount}"))
		val r3 = sBus.register[NpcSpawned, 1, "Self"](npcService)((t: NpcSpawned) => log.trace(s"NpcService - NpcSpawned: ${t.getNpc.getId}, ${t.getNpc.getName}"))
		val r4 = sBus.register[NpcSpawned, 0, "Root"](this)((t: NpcSpawned) => log.trace(s"This - NpcSpawned: ${t.getNpc.getId}, ${t.getNpc.getName}"))
//		_kPanel = Option(injector.getInstance[KPanel](classOf[KPanel]))
//		clientToolbar.addNavigation(navButton)
//		overlayManager.add(panel)
//		debugFrame.open()
	}

	override protected def shutDown(): Unit = {
		npcService.teardown()
		sBus.unregisterAll()
//		sBus.publisher = null
//		debugFrame.close()

//		clientToolbar.removeNavigation(navButton)
//		overlayManager.remove(panel)
	}
}