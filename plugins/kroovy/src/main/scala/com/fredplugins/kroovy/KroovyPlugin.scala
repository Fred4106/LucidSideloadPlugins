package com.fredplugins.kroovy

import com.fredplugins.common.Locatable
import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.events.*
import net.runelite.api.*
import net.runelite.client.{Notifier, RuneLite}
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.game.{ItemManager, SpriteManager}
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger

import scala.language.existentials
import scala.reflect.{TypeTest, Typeable}
import scala.util.chaining.*

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Kroovy</html>",
	description = "Provides a scripting environment for runtime loadable mini-plugins",
	tags = Array(
		"scripting", "helper"
		, "utility", "scala"
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class KroovyPlugin extends Plugin {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: KroovyConfig = null
	@Inject val notifier: Notifier = null

	given Client = RuneLite.getInjector.getInstance(classOf[Client])
	given ClientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])
	given SpriteManager = RuneLite.getInjector.getInstance(classOf[SpriteManager])
	given ItemManager = RuneLite.getInjector.getInstance(classOf[ItemManager])

	@Inject private val eventBus: EventBus = null
	@Inject private val spriteManager: SpriteManager = null
	@Inject private val itemManager: ItemManager = null
	@Inject private val overlayManager: OverlayManager = null

	@Inject private val sBus: SEventBus = null
	@Inject private val npcService: NpcService = null

	@Inject private val panel: KroovyPanel = null

	var inventorySnapshot: List[(Int, Int, Int)] = List.empty

	@Provides
	def getConfig(configManager: ConfigManager): KroovyConfig = {
		configManager.getConfig[KroovyConfig](classOf[KroovyConfig])
	}

	@Subscribe
	def onConfigChanged(event: ConfigChanged): Unit = {
		//		if (!event.getGroup.equals(FredsMixologyConfig.GroupName)) return
		//		if (!config.highlightStations) log.warn("unHighlightAllStations"); //unHighlightAllStations
		//		if (!config.highlightDigWeed) {
		//			log.warn("unHighlightObject(DIGWEED_NORTH_EAST)")
		//			log.warn("unHighlightObject(DIGWEED_SOUTH_EAST)")
		//			log.warn("unHighlightObject(DIGWEED_SOUTH_WEST)")
		//			log.warn("unHighlightObject(DIGWEED_NORTH_WEST)")
		//		}
		//		if (config.highlightLevers) log.warn("highlightLevers");
		//		else log.warn("unHighlightLevers")
	}

	@Subscribe
	def onGameTick(event: GameTick): Unit = {
		sBus.post(event)
//		log.debug("Owner {}"
//		sBus.getAsOwnerMap.foreach{
//			case (owner, seq) => {
//				println(owner)
//				seq
//				owner
//			}
//		}
//		val events = sBus.getAsEventTypeMap.keySet
	}

	def parseInventory(container: ItemContainer): List[(Int, Int, Int)] = {
		container.getItems.zipWithIndex.collect {
			case (i: Item, idx: Int) if i.getId != -1 && i.getQuantity != -1 => (idx, i.getId, i.getQuantity)
		}.toList
	}

	@Subscribe
	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
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

			val str = List(
				"qtyChanged" -> qtyElements,
				"added" -> addedElements,
				"removed" -> removedElements,
			)
				.filter(_._2.nonEmpty)
				.map(u => s"${u._1}=${u._2}")
				.mkString("\n\t", "\n\t", "\n")

			log.debug(s"logStr: ${str}")
		}
		//		// Do not update the highlight if there's a potion in a station
		//		if (alembicPotionType != null || agitatorPotionType != null || retortPotionType != null) return
		//		val inventory = event.getItemContainer
		//		// Find the first potion item and highlight its station
		//		import scala.collection.JavaConversions._
		//		for (item <- inventory.getItems) {
		//			val potionType = PotionType.fromItemId(item.getId)
		//			if (potionType == null) {
		//				continue
		//				//todo: continue is not supported
		//			}
		//			import scala.collection.JavaConversions._
		//			for (order <- potionOrders) {
		//				if ((order.potionType == potionType) && !order.fulfilled) {
		//					unHighlightAllStations
		//					highlightObject(order.potionModifier.alchemyObject, config.stationHighlightColor)
		//					return
		//				}
		//			}
		//		}
	}

	@Subscribe
	def onVarbitChanged(event: VarbitChanged): Unit = {
		val varbitId = event.getVarbitId
		val value = event.getValue
		// Whenever a potion is delivered, all the potion order related varbits are reset to 0 first then
		// set to the new values. We can use this to clear all the stations.
		//		if (VARBIT_POTION_ORDER.contains(varbitId) || VARBIT_POTION_MODIFIER.contains(varbitId)) {
		//			potionOrders = this.potionOrders match {
		//				case ((p1,o1), (p2,o2), (p3,o3)) => {
		//					Option((varbitId, (if(VARBIT_POTION_ORDER.contains(varbitId)) fromIdx(value).orNull else
		//					fromOrderValue(value).orNull))).asInstanceOf[Option[(Int, SBrew | SProcessType |  Null)]].collect {
		//						case (VARBIT_POTION_ORDER_1, b: SBrew) => ((p1, b), (p2, o2), (p3,o3))
		//						case (VARBIT_POTION_ORDER_2, b: SBrew) => ((p1, o1), (p2, b), (p3,o3))
		//						case (VARBIT_POTION_ORDER_3, b: SBrew) => ((p1, o1), (p2, o2), (p3, b))
		//						case (VARBIT_POTION_MODIFIER_1, b:SProcessType) => ((b, o1), (p2, o2), (p3,o3))
		//						case (VARBIT_POTION_MODIFIER_2, b:SProcessType) => ((p1, o1), (b, o2), (p3,o3))
		//						case (VARBIT_POTION_MODIFIER_3, b:SProcessType) => ((p1, o1), (p2, o2), (b, o3))
		//					}.getOrElse(((null, null), (null, null), (null,null)))
		//				}
		//			}
		//		} else if (varbitId == VARBIT_ALEMBIC_POTION) {
		//			if (value == 0) {
		//				// Finished crystalising
		//				//unHighlightObject(AlchemyObject.ALEMBIC)
		//				//				tryFulfillOrder(alembicPotionType, PotionModifier.CRYSTALISED)
		//				//				tryHighlightNextStation
		//				log.debug("Finished crystalising {}", alembicPotionType)
		//				alembicPotionType = Option.empty
		//			} else {
		//				alembicPotionType = SBrew.fromIdx(value)
		//				log.debug("Alembic potion type: {}", alembicPotionType)
		//			}
		//		} else if (varbitId == VARBIT_AGITATOR_POTION) {
		//			if (value == 0) {
		////				unHighlightObject(AlchemyObject.AGITATOR)
		////				tryFulfillOrder(agitatorPotionType, PotionModifier.HOMOGENOUS)
		////				tryHighlightNextStation
		//				log.debug("Finished homogenising {}", agitatorPotionType)
		//				agitatorPotionType = Option.empty
		//			} else {
		//				agitatorPotionType = SBrew.fromIdx(value)
		//				log.debug("Agitator potion type: {}", agitatorPotionType)
		//			}
		//		} else if (varbitId == VARBIT_RETORT_POTION) {
		//			if (value == 0) {
		////							unHighlightObject(AlchemyObject.RETORT)
		////							tryFulfillOrder(retortPotionType, PotionModifier.CONCENTRATED)
		////							tryHighlightNextStation
		//				log.debug("Finished concentrating {}", retortPotionType)
		//				retortPotionType = Option.empty
		//			} else {
		//				retortPotionType = SBrew.fromIdx(value)
		//				log.debug("Retort potion type: {}", retortPotionType)
		//			}
		//		} else if (varbitId == VARBIT_DIGWEED_NORTH_EAST) {
		//			if (value == 1) {
		////				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_NORTH_EAST, config
		// .digweedHighlightColor)
		//				notifier.notify(config.notifyDigWeed, "A digweed has spawned north east.")
		//			} //else unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST)
		//		} else if (varbitId == VARBIT_DIGWEED_SOUTH_EAST) {
		//			if (value == 1) {
		////				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_SOUTH_EAST, config
		// .digweedHighlightColor)
		//				notifier.notify(config.notifyDigWeed, "A digweed has spawned south east.")
		//			}
		////			else unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST)
		//		} else if (varbitId == VARBIT_DIGWEED_SOUTH_WEST) {
		//			if (value == 1) {
		////				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_SOUTH_WEST, config
		// .digweedHighlightColor)
		//				notifier.notify(config.notifyDigWeed, "A digweed has spawned south west.")
		//			}
		////			else unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST)
		//		} else if (varbitId == VARBIT_DIGWEED_NORTH_WEST) {
		//			if (value == 1) {
		////				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_NORTH_WEST, config
		// .digweedHighlightColor)
		//				notifier.notify(config.notifyDigWeed, "A digweed has spawned north west.")
		//			}
		////			else unHighlightObject(AlchemyObject.DIGWEED_NORTH_WEST)
		//		} else if (varbitId == VARBIT_AGITATOR_PROGRESS) {
		//			if (agitatorQuickActionTicks == 2) {
		//				// quick action was triggered two ticks ago, so it's now too late
		////				resetDefaultHighlight(AlchemyObject.AGITATOR)
		//				agitatorQuickActionTicks = 0
		//			}
		//			if (agitatorQuickActionTicks == 1) agitatorQuickActionTicks = 2
		//			if (value < previousAgitatorProgess) {
		//				// progress was set back due to a quick action failure
		////				resetDefaultHighlight(AlchemyObject.AGITATOR)
		//			}
		//			previousAgitatorProgess = value
		//		} else if (varbitId == VARBIT_ALEMBIC_PROGRESS) {
		//			if (alembicQuickActionTicks == 1) {
		//				// quick action was triggered last tick, so it's now too late
		////					resetDefaultHighlight(AlchemyObject.ALEMBIC)
		//				alembicQuickActionTicks = 0
		//			}
		//			if (value < previousAlembicProgress) {
		//			// progress was set back due to a quick action failure
		////					resetDefaultHighlight(AlchemyObject.ALEMBIC)
		//			}
		//			previousAlembicProgress = value
		//		} else if (varbitId == VARBIT_RETORT_PROGRESS) {
		//			if (value < previousRetortProgess) {
		//				// progress was set back due to a quick action failure
		////				resetDefaultHighlight(AlchemyObject.AGITATOR)
		//			}
		//			previousRetortProgess = value
		//		} else if (varbitId == VARBIT_AGITATOR_QUICKACTION) {
		//				// agitator quick action was just successfully popped
		////				resetDefaultHighlight(AlchemyObject.AGITATOR)
		//		} else if (varbitId == VARBIT_ALEMBIC_QUICKACTION) {
		//			// alembic quick action was just successfully popped
		////			resetDefaultHighlight(AlchemyObject.ALEMBIC)
		//		}
	}

	@Subscribe
	def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		val spotAnimId = event.getGraphicsObject.getId
		////		if (!config.highlightQuickActionEvents) return
		//		if (spotAnimId == SPOT_ANIM_ALEMBIC && alembicPotionType != null) {
		////			highlightObject(AlchemyObject.ALEMBIC, config.stationQuickActionHighlightColor)
		//			// start counting ticks for alembic so we know to un-highlight on the next alembic varbit update
		//			// note this quick action has a 1 tick window, so we use an int that goes 0 -> 1 -> unhighlight
		//			alembicQuickActionTicks = 1
		//		}
		//		if (spotAnimId == SPOT_ANIM_AGITATOR && agitatorPotionType != null) {
		////			highlightObject(AlchemyObject.AGITATOR, config.stationQuickActionHighlightColor)
		//			// start counting ticks for agitator so we know to un-highlight on the next agitator varbit update
		//			// note this quick action has a 2-tick window, so we use an int that goes 0 -> 1 -> 2 -> unhighlight
		//			agitatorQuickActionTicks = 1
		//		}
	}

	@Subscribe
	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked):Unit = {
		import com.fredplugins.common.extensions.MenuExtensions.{given, *}
		import Locatable.{*, given}

		val npcClickedOpt = menuOptionClicked.getMenuEntry.pipe(me => Option.when(me.isNpcAction)(me)).filter(me => me.getNpcOpt.filter(_.getId == 9020).filter(_.distanceTo(client.getLocalPlayer) < 5).isDefined)
		npcClickedOpt.map(me => me.getType -> me.getNpc) match {
			case Some((MenuAction.NPC_FIRST_OPTION, npc)) => sBus.unregisterAll[NpcSpawned](npcService)
			case Some((MenuAction.NPC_SECOND_OPTION, npc)) => sBus.unregisterAll[GameTick](npcService)
			case Some((MenuAction.EXAMINE_NPC, npc)) => sBus.debug()
			case None =>
		}
	}

	override protected def startUp(): Unit = {
		val r1 = sBus.register(this, 0)((t: GameTick) => log.debug(s"This - Gametick: ${client.getTickCount}"))
		val r2 = sBus.register(npcService, 4)((t: GameTick) => log.debug(s"This is also a gametick: ${client.getTickCount}"))
		val r3 = sBus.register(npcService, 1)((t: NpcSpawned) => log.debug(s"NpcService - NpcSpawned: ${t.getNpc.getId}, ${t.getNpc.getName}"))
		val r4 = sBus.register(this)((t: NpcSpawned) => log.debug(s"This - NpcSpawned: ${t.getNpc.getId}, ${t.getNpc.getName}"))

		overlayManager.add(panel)
	}

	override protected def shutDown(): Unit = {
		sBus.unregisterAll()
		overlayManager.remove(panel)
		//		overlayManager.remove(overlay)
		//		eventBus.unregister(FredsTemporossLogic)
	}
}