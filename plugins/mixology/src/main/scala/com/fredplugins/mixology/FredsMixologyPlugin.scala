package com.fredplugins.mixology

import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getTileObjectOpt, isNpcAction, isTileObjectAction}
import com.fredplugins.common.extensions.ObjectExtensions.{composition, impostorComposition, isImpostor, morphId, wrapped}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.mixology.SBrew.fromIdx
import com.fredplugins.mixology.SProcessType.fromOrderValue
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.{ChatMessageType, Client, GameState, InventoryID, Item, ItemContainer, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{GameStateChanged, GameTick, GraphicsObjectCreated, ItemContainerChanged, MenuEntryAdded, MenuOptionClicked, ScriptPostFired, VarbitChanged, WidgetClosed, WidgetLoaded}
import net.runelite.api.widgets.Widget
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger

import scala.jdk.StreamConverters.StreamHasToScala
import java.util
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.util.{Random, Try}
import scala.util.chaining.*
@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Mixology</html>",
	description = "Useful information and tracking for the Mixology minigame",
	tags = Array(
		"herblore", "minigame"
		, "skilling"
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsMixologyPlugin() extends Plugin {
	@Inject val client      : Client              = null
	@Inject val clientThread: ClientThread        = null
	@Inject val config      : FredsMixologyConfig = null
	@Inject val notifier    : Notifier            = null
	private         val log           : Logger             = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private val eventBus      : EventBus           = null
	@Inject private val overlayManager: OverlayManager     = null
	@Inject private val panel         : FredsMixologyPanel = null
	//	@Inject private   val overlay       : FredsTemporossOverlay = null
//	var state        : State = emptyState
//	var previousState: State = emptyState
	var potionOrders: AllOrdersType = ((null, null), (null, null), (null,null))
	var inventorySnapshot: List[(Int, Int, Int)] = List.empty
	var inLab: Boolean = false

	var alembicPotionType : Option[SBrew] = Option.empty
	var agitatorPotionType: Option[SBrew] = Option.empty
	var retortPotionType  : Option[SBrew] = Option.empty

	var previousAgitatorProgess = 0
	var previousRetortProgess = 0
	var previousAlembicProgress = 0
	var agitatorQuickActionTicks = 0
	var alembicQuickActionTicks  = 0

	given Client = client
	@Provides
	def getConfig(configManager: ConfigManager): FredsMixologyConfig = {
		configManager.getConfig[FredsMixologyConfig](classOf[FredsMixologyConfig])
	}
//	def emptyState: State = State(-1, List.empty, List.empty, List.empty)
	@Subscribe
	def onGameTick(tick: GameTick): Unit = {
//		previousState = state
//		state = buildState
//		(state.isInRegion, previousState.isInRegion) match {
//			case (true, false) => {
//				//now in region
//				//set state
//			}
//			case (false, true) => {
//				//clear state
//			}
//			case (_, _) =>
//		}
	}
//	def buildState: State = {
//		val region = Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
//		if (region == 5521) {
//			val toolBenches = TileObjects.search().withId(55389, 55390, 55391).result().asScala.toList.sortBy(_.getId)
//				.flatMap {
//					to => SProcessType.fromToolBench(to).map(spt => (spt, to -> SBrew.fromToolBench(to)))
//				}
//			val pedestals   = TileObjects.search().withId(55392, 55393, 55394).result().asScala.toList.sortBy(_.getId).flatMap {
//				to => SMixType.fromPedestal(to)
//			}
//			val orders      = (0 until 3).toList.map(VARBIT_POTION_ORDER(_)).map(varbitOrderId => {
//				client.getVarbitValue(varbitOrderId + 1) -> client.getVarbitValue(varbitOrderId)
//			}).map {
//				case (mod, brew) => SProcessType.fromOrderValue(mod).zip(SBrew.fromOrderValue(brew)).get
//			}
//			State(region, pedestals, toolBenches, orders)
//		} else {
//			State(region, List.empty, List.empty, List.empty)
//		}
//	}
//	@Subscribe
//	def onItemContainerChanged(ev: ItemContainerChanged): Unit = {
//		if (ev.getContainerId == InventoryID.INVENTORY.getId) {
//			val container   = ev.getItemContainer
//			val itemsList   = (for {
//				idx <- (0 until container.size)
//				(id, qty) <- Option(container.getItem(idx)).map(i => i.getId -> i.getQuantity)
//			} yield {
//				(idx, id, qty)
//			})
//				.pipe(_.toList)
//			val inventoryID = InventoryID.values().toList.find(_.getId == container.getId).get
//			log.debug("ItemContainerChanged({}) = {}", inventoryID, container.count())
//			itemsList.foreach(ie => {
//				log.debug("    {} = ({}, {})", ie._1, ie._2, ie._3)
//			})
//		}
//	}
//

	@Subscribe
	def onGameStateChanged(event: GameStateChanged): Unit = {
		if ((event.getGameState == GameState.LOGIN_SCREEN) || (event.getGameState == GameState.HOPPING)) log.debug("highlightedObjects.clear"); //highlightedObjects.clear
	}
	@Subscribe
	def onWidgetLoaded(event: WidgetLoaded): Unit = {
		if (event.getGroupId != COMPONENT_POTION_ORDERS_GROUP_ID) return
		val ordersLayer = client.getWidget(COMPONENT_POTION_ORDERS_GROUP_ID, 0)
		if (ordersLayer == null || ordersLayer.isSelfHidden) {
			return
		}

		log.debug("initialize plugin")
		inLab = true
//		updatePotionOrders
//		highlightLevers
//		tryHighlightNextStation
	}
	@Subscribe
	def onWidgetClosed(event: WidgetClosed): Unit = {
		if (event.getGroupId != COMPONENT_POTION_ORDERS_GROUP_ID) return
		log.debug("highlightedObjects.clear")//highlightedObjects.clear
		inLab = false
	}
	@Subscribe
	def onConfigChanged(event: ConfigChanged): Unit = {
		if (!event.getGroup.equals(FredsMixologyConfig.GroupName)) return
		if (!config.highlightStations) log.warn("unHighlightAllStations"); //unHighlightAllStations
		if (!config.highlightDigWeed) {
			log.warn("unHighlightObject(DIGWEED_NORTH_EAST)")
			log.warn("unHighlightObject(DIGWEED_SOUTH_EAST)")
			log.warn("unHighlightObject(DIGWEED_SOUTH_WEST)")
			log.warn("unHighlightObject(DIGWEED_NORTH_WEST)")
		}
		if (config.highlightLevers) log.warn("highlightLevers");
		else log.warn("unHighlightLevers")
	}
	def parseInventory(container: ItemContainer): List[(Int, Int, Int)] = {
		container.getItems.toList.zipWithIndex.map(_.swap).collect {
			case (idx: Int, i: Item) => (idx, i.getId, i.getQuantity)
		}
	}
	@Subscribe
	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
		if (inLab && event.getContainerId == InventoryID.INVENTORY.getId) {
			val currentInventory = parseInventory(event.getItemContainer)
			val sharedElements = currentInventory.intersect(inventorySnapshot)
			val addedElements = currentInventory.filter(u => sharedElements.contains(u)).diff(inventorySnapshot.filter(u => sharedElements.contains(u)))
			val removedElements = inventorySnapshot.filter(u => sharedElements.contains(u)).diff(currentInventory.filter(u => sharedElements.contains(u)))
			val qtyChanged = addedElements.map(x => x._1 -> x._2).intersect(removedElements.map(x => x._1 -> x._2)).map{
				case (idx, id) => {
					(
						idx,
						id,
						addedElements.find(y => y._1 == idx && y._2 == id).map(_._3).getOrElse(0) - removedElements.find(y => y._1 == idx && y._2 == id).map(_._3).getOrElse(0)
					)
				}
			}
			val realAddedElements = addedElements.filterNot(x => qtyChanged.map(z => z._1 -> z._2).contains((x._1, x._2)))
			val realRemovedElements = removedElements.filterNot(x => qtyChanged.map(z => z._1 -> z._2).contains((x._1, x._2)))

			log.debug("added {}, removed {}, realAdded {}, realRemoved {}, qtyChanged {}", addedElements, removedElements, realAddedElements, realRemovedElements, qtyChanged)
			inventorySnapshot = currentInventory
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
		val value    = event.getValue
		// Whenever a potion is delivered, all the potion order related varbits are reset to 0 first then
		// set to the new values. We can use this to clear all the stations.
		if (VARBIT_POTION_ORDER.contains(varbitId) || VARBIT_POTION_MODIFIER.contains(varbitId)) {
			potionOrders = this.potionOrders match {
				case ((p1,o1), (p2,o2), (p3,o3)) => {
					(varbitId, (if(VARBIT_POTION_ORDER.contains(varbitId)) fromIdx(value) else fromOrderValue(value))) match {
						case (VARBIT_POTION_ORDER_1, b: Option[SBrew]) => ((p1, b.orNull), (p2, o2), (p3,o3))
						case (VARBIT_POTION_ORDER_2, b: Option[SBrew]) => ((p1, o1), (p2, b.orNull), (p3,o3))
						case (VARBIT_POTION_ORDER_3, b: Option[SBrew]) => ((p1, o1), (p2, o2), (p3, b.orNull))
						case (VARBIT_POTION_MODIFIER_1, b:Option[SProcessType]) => ((b.orNull, o1), (p2, o2), (p3,o3))
						case (VARBIT_POTION_MODIFIER_2, b:Option[SProcessType]) => ((p1, o1), (b.orNull, o2), (p3,o3))
						case (VARBIT_POTION_MODIFIER_3, b:Option[SProcessType]) => ((p1, o1), (p2, o2), (b.orNull, o3))
					}
				}
			}
		} else if (varbitId == VARBIT_ALEMBIC_POTION) {
			if (value == 0) {
				// Finished crystalising
				//unHighlightObject(AlchemyObject.ALEMBIC)
				//				tryFulfillOrder(alembicPotionType, PotionModifier.CRYSTALISED)
				//				tryHighlightNextStation
				log.debug("Finished crystalising {}", alembicPotionType)
				alembicPotionType = Option.empty
			} else {
				alembicPotionType = SBrew.fromIdx(value)
				log.debug("Alembic potion type: {}", alembicPotionType)
			}
		} else if (varbitId == VARBIT_AGITATOR_POTION) {
			if (value == 0) {
//				unHighlightObject(AlchemyObject.AGITATOR)
//				tryFulfillOrder(agitatorPotionType, PotionModifier.HOMOGENOUS)
//				tryHighlightNextStation
				log.debug("Finished homogenising {}", agitatorPotionType)
				agitatorPotionType = Option.empty
			} else {
				agitatorPotionType = SBrew.fromIdx(value)
				log.debug("Agitator potion type: {}", agitatorPotionType)
			}
		} else if (varbitId == VARBIT_RETORT_POTION) {
			if (value == 0) {
//							unHighlightObject(AlchemyObject.RETORT)
//							tryFulfillOrder(retortPotionType, PotionModifier.CONCENTRATED)
//							tryHighlightNextStation
				log.debug("Finished concentrating {}", retortPotionType)
				retortPotionType = Option.empty
			} else {
				retortPotionType = SBrew.fromIdx(value)
				log.debug("Retort potion type: {}", retortPotionType)
			}
		} else if (varbitId == VARBIT_DIGWEED_NORTH_EAST) {
			if (value == 1) {
//				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_NORTH_EAST, config.digweedHighlightColor)
				notifier.notify(config.notifyDigWeed, "A digweed has spawned north east.")
			} //else unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST)
		} else if (varbitId == VARBIT_DIGWEED_SOUTH_EAST) {
			if (value == 1) {
//				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_SOUTH_EAST, config.digweedHighlightColor)
				notifier.notify(config.notifyDigWeed, "A digweed has spawned south east.")
			}
//			else unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST)
		} else if (varbitId == VARBIT_DIGWEED_SOUTH_WEST) {
			if (value == 1) {
//				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_SOUTH_WEST, config.digweedHighlightColor)
				notifier.notify(config.notifyDigWeed, "A digweed has spawned south west.")
			}
//			else unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST)
		} else if (varbitId == VARBIT_DIGWEED_NORTH_WEST) {
			if (value == 1) {
//				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_NORTH_WEST, config.digweedHighlightColor)
				notifier.notify(config.notifyDigWeed, "A digweed has spawned north west.")
			}
//			else unHighlightObject(AlchemyObject.DIGWEED_NORTH_WEST)
		} else if (varbitId == VARBIT_AGITATOR_PROGRESS) {
			if (agitatorQuickActionTicks == 2) {
				// quick action was triggered two ticks ago, so it's now too late
//				resetDefaultHighlight(AlchemyObject.AGITATOR)
				agitatorQuickActionTicks = 0
			}
			if (agitatorQuickActionTicks == 1) agitatorQuickActionTicks = 2
			if (value < previousAgitatorProgess) {
				// progress was set back due to a quick action failure
//				resetDefaultHighlight(AlchemyObject.AGITATOR)
			}
			previousAgitatorProgess = value
		} else if (varbitId == VARBIT_ALEMBIC_PROGRESS) {
			if (alembicQuickActionTicks == 1) {
				// quick action was triggered last tick, so it's now too late
//					resetDefaultHighlight(AlchemyObject.ALEMBIC)
				alembicQuickActionTicks = 0
			}
			if (value < previousAlembicProgress) {
			// progress was set back due to a quick action failure
//					resetDefaultHighlight(AlchemyObject.ALEMBIC)
			}
			previousAlembicProgress = value
		} /*else if (varbitId == VARBIT_RETORT_PROGRESS) {
			if (agitatorQuickActionTicks == 2) {
				// quick action was triggered two ticks ago, so it's now too late
//				resetDefaultHighlight(AlchemyObject.AGITATOR)
				agitatorQuickActionTicks = 0
			}
			if (agitatorQuickActionTicks == 1) agitatorQuickActionTicks = 2
			if (value < previousAgitatorProgess) {
				// progress was set back due to a quick action failure
//				resetDefaultHighlight(AlchemyObject.AGITATOR)
			}
			previousAgitatorProgess = value
		} */else if (varbitId == VARBIT_AGITATOR_QUICKACTION) {
				// agitator quick action was just successfully popped
//				resetDefaultHighlight(AlchemyObject.AGITATOR)
		} else if (varbitId == VARBIT_ALEMBIC_QUICKACTION) {
			// alembic quick action was just successfully popped
//			resetDefaultHighlight(AlchemyObject.ALEMBIC)
		}
	}
	@Subscribe
	def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		val spotAnimId = event.getGraphicsObject.getId
//		if (!config.highlightQuickActionEvents) return
		if (spotAnimId == SPOT_ANIM_ALEMBIC && alembicPotionType != null) {
//			highlightObject(AlchemyObject.ALEMBIC, config.stationQuickActionHighlightColor)
			// start counting ticks for alembic so we know to un-highlight on the next alembic varbit update
			// note this quick action has a 1 tick window, so we use an int that goes 0 -> 1 -> unhighlight
			alembicQuickActionTicks = 1
		}
		if (spotAnimId == SPOT_ANIM_AGITATOR && agitatorPotionType != null) {
//			highlightObject(AlchemyObject.AGITATOR, config.stationQuickActionHighlightColor)
			// start counting ticks for agitator so we know to un-highlight on the next agitator varbit update
			// note this quick action has a 2-tick window, so we use an int that goes 0 -> 1 -> 2 -> unhighlight
			agitatorQuickActionTicks = 1
		}
	}
	override protected def startUp(): Unit = {
		inventorySnapshot = clientThread.runOnClientThread(() => parseInventory(client.getItemContainer(InventoryID.INVENTORY)))
		inLab = clientThread.runOnClientThread(() => {
			val ordersLayer = client.getWidget(COMPONENT_POTION_ORDERS_GROUP_ID, 0)
			if (ordersLayer == null || ordersLayer.isSelfHidden) {
				false
			} else {
				true
			}
		})

		//		FredsTemporossLogic.init(this)
		//		eventBus.register(FredsTemporossLogic)
		overlayManager.add(panel)
		//		overlayManager.add(overlay)
	}
	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		//		overlayManager.remove(overlay)
		//		eventBus.unregister(FredsTemporossLogic)
	}
//	case class State(region: Int, pedestals: List[SMixType], toolBenches: List[(SProcessType, (TileObject, Option[SBrew]))], orders: List[(SProcessType, SBrew)]) {
//		def isInRegion: Boolean = region == 5521
//	}

	//	@Subscribe
	//	def onMenuOptionClicked(mec: MenuOptionClicked): Unit = {
	//		val me = mec.getMenuEntry
	//		val tlwv = client.getTopLevelWorldView
	//		val temp = Option(
	//			(me.getOption, me.getTarget, me.getType, me.getIdentifier, (me.getParam0, me.getParam1))
	//		)
	//		val toLog = temp.map{
	//			case (opt, targ, tpe, ident, (x, y)) if	(me.isTileObjectAction) => {
	//				Option(tlwv.getScene.getTiles.apply(tlwv.getPlane)(x)(y)).map(_.getWorldLocation).map(wp => (opt, targ, tpe, ident, (x, y), wp))
	//					.map {
	//						case (opt, _, tpe, ident, (x, y), wp) => s"Object(opt=\"$opt\", targ=\"$targ\", id=${ident}, sLoc=($x, $y), wp=$wp, tpe=${tpe})"
	//					}.getOrElse(s"Error: tpe=${tpe}")
	//			}
	//			case (opt, targ, tpe, ident, (x, y)) if(me.isNpcAction) => s"NPC(opt=\"$opt\", targ=\"$targ\", index=$ident, id=${me.getNpc.getId}, sLoc=${(x, y)}, tpe=${tpe})"
	//			case (opt, targ, tpe, ident, (x, y)) => s"opt=\"$opt\", targ=\"$targ\", ident=$ident, param=${(x, y)}, tpe=${tpe}"
	//		}.get
	//		log.debug("Clicked: ({})",toLog)
	//	}
}

