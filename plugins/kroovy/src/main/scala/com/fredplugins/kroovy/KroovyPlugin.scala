package com.fredplugins.kroovy

import com.fredplugins.common.{Locatable, PrayerExtended}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.GauntletTags.TagsSet
import com.fredplugins.kroovy.eventbus.{SEventBus, SEventBusFrame}
import com.fredplugins.kroovy.events.EventManager
import com.fredplugins.kroovy.services.npc.{NpcService, NpcServiceApi, NpcServicesFrame, SNpcEvent}
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.CombatUtils
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
import enumeratum.*
sealed abstract class GauntletTag(val validIds: Int *)(using val set: TagsSet) extends EnumEntry with Product {
	override def entryName: String = set.productPrefix + "." + productPrefix
	val ids: Set[Int] = validIds.toSet
	def debugString: String
	override def toString: String = entryName
}
object GauntletTags extends Enum[GauntletTag] {parent =>
	sealed trait TagsSet {tagset =>
		given TagsSet = tagset
		def productPrefix: String
		def values: Set[GauntletTag] = {
			parent.values.filter(_.set == tagset).toSet/*.tap(ts => {ts.foreach(println(_)); println()})*/
		}

		trait GTag {
			self: GauntletTag =>
//				tag: EnumEntry =>
//			override protected lazy val stableName: String = tagset.productPrefix + "." + super.entryName
			//			override lazy val  s: String = tagset.productPrefix + "." + super.entryName
//			override lazy val stableEntryName
//			override def entryName: String = productPrefix + "." + super.entryName
			override def debugString: String = s"${entryName} extends GTag${validIds.mkString("(", ", ", ")")}"
		}
	}

	case object Weak extends TagsSet {
		case object Bat extends GauntletTag(NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT) with GTag
		case object Rat extends GauntletTag(NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT) with GTag
		case object Spider extends GauntletTag(NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER) with GTag
	}
	case object Strong extends TagsSet  {
		case object Scorpion extends GauntletTag(NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION) with GTag
		case object Unicorn extends GauntletTag(NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN) with GTag
	}
	case object Demiboss extends TagsSet {
		case object Bear extends GauntletTag(NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR) with GTag
		case object Dark_beast extends GauntletTag(NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST) with GTag
		case object Dragon extends GauntletTag(NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON) with GTag
	}
	case object Boss extends TagsSet {
		case object Hunllef extends GauntletTag(NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022, NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024, NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036, NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038) with GTag
		case object Tornado extends GauntletTag(NullNpcID.NULL_9025, NullNpcID.NULL_9039, NullNpcID.NULL_14142) with GTag
	}

	def ids: Set[Int] = values.flatMap(_.ids).toSet
	def find(npc: NPC): Option[GauntletTag] = values.find(_.ids.contains(npc.getId))
	override def values: IndexedSeq[GauntletTag] = findValues

}

//object GauntletNpcFilter extends NpcEventFilter(GauntletTags){}

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
	@Inject private val npcService: NpcService= null

	given Client = RuneLite.getInjector.getInstance(classOf[Client])
	given ClientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])
	given EventBus = RuneLite.getInjector.getInstance(classOf[EventBus])
	given SpriteManager = RuneLite.getInjector.getInstance(classOf[SpriteManager])
	given ItemManager = RuneLite.getInjector.getInstance(classOf[ItemManager])
//	given SEventBus = RuneLite.getInjector.getInstance(classOf[SEventBus])
	given NpcService = RuneLite.getInjector.getInstance(classOf[NpcService])
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
	NpcServicesFrame.get
	override protected def startUp(): Unit = {
		eventManager.start()
		npcService.init()
		NpcServicesFrame.get.open()
//		val r1 = sBus.register[GameTick, 0, "Self"](this)((t: GameTick) => SwingUtilities.invokeLater(() => {sBus.publish(SEventBus.Record(s"Gametick: ${client.getTickCount}"))}))
//		val r3 = sBus.register[NpcSpawned, 1, "Self"](this)((t: NpcSpawned) => (t.getNpc.getId, t.getNpc.getName).tap{
//			case (npcId, npcStr) =>  SwingUtilities.invokeLater(() => {sBus.publish(SEventBus.Record(s"NpcService - NpcSpawned: ${npcId}, ${npcStr}"))})
//		})
		def report(npc: NPC)(msg: String): Unit = {
			val tag     = GauntletTags.find(npc).map(_.entryName).getOrElse("None")
			val toPrint = s"Npc[${tag}](${Integer.toHexString(npc.hashCode())})" + " " + msg
			SwingUtilities.invokeLater(() => {npcService.publish(NpcServiceApi.Log(toPrint))})
//			log.debug(s"${toPrint}")
			//					SwingUtilities.invokeLater(() => {sBus.publish(SEventBus.Record(toPrint))})
		}

//		val weakAndStrongNpcIds = Set(GauntletTags.Weak, GauntletTags.Strong).flatMap(_.values).flatMap(_.ids)
		npcService.register(StrongNpcs, (e: SNpcEvent.Moved) => {
			if(e.cur.distanceTo(client.getLocalPlayer.getWorldLocation) < 3 && !client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
				CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE)
			}
		})
		npcService.register(StrongNpcs, (e: SNpcEvent.Died) => {
			if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
				CombatUtils.deactivatePrayer(Prayer.PROTECT_FROM_MELEE)
			}
		})
//		npcService.register(GauntletTags.Boss.values.flatMap(_.ids), (e: SNpcEvent.Died) => {
//			report(e.npc)(s"Died @ ${e.npc.getWorldLocation}")
//		})
//		npcService.register(GauntletTags.Boss.values.flatMap(_.ids), (e: SNpcEvent.CompositionChanged) => {
//			report(e.npc)(s"Composition Changed from ${e.old} to ${e.cur}")
//		})
		npcService.register(GauntletTags.Boss.values.flatMap(_.ids), (e: SNpcEvent.AnimationChanged) => {
//			report(e.npc)(s"Animation Changed from ${e.old} to ${e.cur}")
			clientThread.invokeLater(() => {
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "KroovyGauntlet", s"Animation Changed from ${e.old} to ${e.cur}", "")
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "KroovyGauntlet", s"    ${e}", "")
				()
			})
		})
//		(new NpcListener {
//				override def onSpawned(npc: NPC): Unit = {
//					report(npc)(s"Spawned @ ${npc.getWorldLocation}")
//				}
//				override def onDespawned(npc: NPC): Unit = {
//					report(npc)(s"Despawned")
//				}
//				override def onDeath(npc: NPC): Unit = {
//					report(npc)(s"Died")
//				}
//				override def onCompositionChanged(npc: NPC, old: NPCComposition, cur: NPCComposition): Unit = {
//					report(npc)(s"Composition Changed from ${old.getId} to ${cur.getId}")
//				}
//				override def onAnimationChanged(npc: NPC, old: Int, cur: Int): Unit = {
//					report(npc)(s"Animation Changed from $old to $cur")
//				}
//			})
//		_kPanel = Option(injector.getInstance[KPanel](classOf[KPanel]))
//		clientToolbar.addNavigation(navButton)
//		overlayManager.add(panel)
//		debugFrame.open()
		eventManager.register(KGauntlet)
	}

	override protected def shutDown(): Unit = {
		eventManager.stop()
		npcService.teardown()
		NpcServicesFrame.get.close()
//		sBus.unregisterAll()
//		sBus.publisher = null
//		debugFrame.close()

//		clientToolbar.removeNavigation(navButton)
//		overlayManager.remove(panel)
	}
}