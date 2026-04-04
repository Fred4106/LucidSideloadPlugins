package com.fredplugins.mixology

import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getTileObjectOpt, isNpcAction, isTileObjectAction}
import com.fredplugins.common.extensions.ObjectExtensions.{composition, impostorComposition, isImpostor, morphId, wrapped}
import com.fredplugins.common.extensions.ActorExtensions.{region, templateLocation, templateRegion}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.mixology.PotionComponent.AGA
import com.fredplugins.mixology.PotionComponent.LYE
import com.fredplugins.mixology.PotionComponent.MOX
import com.fredplugins.mixology.SBrew.fromIdx
import com.fredplugins.mixology.SProcessType.Concentrated
import com.fredplugins.mixology.SProcessType.Crystalised
import com.fredplugins.mixology.SProcessType.Homogenous
import com.fredplugins.mixology.SProcessType.fromOrderValue
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.DecorativeObject
import net.runelite.api.FontID
import net.runelite.api.coords.LocalPoint
import net.runelite.api.{ChatMessageType, Client, GameState, Item, ItemContainer, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.SoundEffectPlayed
import net.runelite.api.events.{GameStateChanged, GameTick, GraphicsObjectCreated, ItemContainerChanged, MenuEntryAdded, MenuOptionClicked, ScriptPostFired, VarbitChanged, WidgetClosed, WidgetLoaded}
import net.runelite.api.gameval.InventoryID
import net.runelite.api.widgets.Widget
import net.runelite.api.widgets.WidgetPositionMode
import net.runelite.api.widgets.WidgetTextAlignment
import net.runelite.api.widgets.WidgetType
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.GameEventManager
import org.slf4j.Logger

import scala.jdk.StreamConverters.StreamHasToScala
import java.util
import java.util.Comparator
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.swing.Color
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
	@Inject private val inventoryOverlay         : InventoryPotionOverlay = null
	@Inject private val mixologyOverlay         : FredsMixologyOverlay = null
	@Inject private val goalPanel         : GoalPanel = null

	var potionOrders: Seq[Order] = Seq.empty// Array[Order] = Array.fill(3)((null, null, false))

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

	private var goalCached: Option[Goal] = Option.empty
	def getGoal: Option[Goal] = goalCached

	def recalculateGoalData(): Unit = {
		goalCached = Option.when(inLab && config.selectedReward != RewardItem.NONE){
			Goal(config)
		}
	}

	@Subscribe
	def onGameTick(tick: GameTick): Unit = {
		recalculateGoalData()
	}

	val highlightedObjects: mutable.Map[AlchemyObject, HighlightedObject] = mutable.LinkedHashMap.empty[AlchemyObject, HighlightedObject]
	def getHighlightedObjects: Map[AlchemyObject, HighlightedObject] = highlightedObjects.toMap

	def isInLabRegion: Boolean = {
		Option(client.getLocalPlayer).map(_.templateLocation).exists(tl => tl.getRegionID == LABS_REGION_ID && tl.getPlane == LABS_REGION_PLANE)
	}

	override protected def startUp(): Unit = {
		overlayManager.add(mixologyOverlay)
		overlayManager.add(inventoryOverlay)
		overlayManager.add(goalPanel)
		overlayManager.add(panel)

		if (client.getGameState == GameState.LOGGED_IN) clientThread.invokeLater(() => this.initialize())
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(mixologyOverlay)
		overlayManager.remove(inventoryOverlay)
		overlayManager.remove(goalPanel)
		overlayManager.remove(panel)

		inLab = false
	}

	private def initialize(): Unit = {
		val ordersLayer = client.getWidget(COMPONENT_POTION_ORDERS_LAYER)
		if (ordersLayer == null || ordersLayer.isSelfHidden) return

		log.debug("Initialize plugin")
		inLab = true
		updatePotionOrders()
		highlightLevers()
		tryHighlightNextStation()
	}

	@Subscribe
	def onGameStateChanged(event: GameStateChanged): Unit = {
		if ((event.getGameState eq GameState.LOGIN_SCREEN) || (event.getGameState eq GameState.HOPPING)) highlightedObjects.clear
	}

	@Subscribe
	def onWidgetLoaded(event: WidgetLoaded): Unit = {
		if (event.getGroupId ne COMPONENT_POTION_ORDERS_GROUP_ID) return
		initialize()
	}

	@Subscribe
	def onWidgetClosed(event: WidgetClosed): Unit = {
		if (event.getGroupId ne COMPONENT_POTION_ORDERS_GROUP_ID) return
		highlightedObjects.clear
		inLab = false
	}

	@Subscribe
	def onConfigChanged(event: ConfigChanged): Unit = {
		if (!(event.getGroup.equals(FredsMixologyConfig.CONFIG_GROUP))) return
		if (event.getKey.equals("potionOrderSorting")) clientThread.invokeLater(() => this.updatePotionOrders())
		if (event.getKey.equals("highlightStations")) if (!(config.highlightStations)) unHighlightAllStations()
		else clientThread.invokeLater(() => this.tryHighlightNextStation())
		if (event.getKey.equals("displayResin")) {
			// Trigger the potion order update to refresh the resin display
			clientThread.invokeLater(() => this.triggerPotionOrderUpdate())
		}
		if (!(config.highlightDigWeed)) {
			unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST)
			unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST)
			unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST)
			unHighlightObject(AlchemyObject.DIGWEED_NORTH_WEST)
		}
		if (event.getKey.equals("selectedReward") || event.getKey.equals("rewardQuantity") || event.getKey.equals("showResinBars")) recalculateGoalData()
		if (config.highlightLevers) highlightLevers()
		else unHighlightLevers()
	}

	@Subscribe
	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
		if (!(inLab) || !(config.highlightStations) || (event.getContainerId ne InventoryID.INV)) return
		// Do not update the highlight if there's a potion in a station
		if (alembicPotionType.isDefined || agitatorPotionType.isDefined || retortPotionType.isDefined) return
		val inventory = event.getItemContainer
		// Find the first potion item and highlight its station
		for (item <- inventory.getItems) {
			val potionType = SBrew.fromItemId(item.getId)
			if (potionType.nonEmpty && potionType.get.processedId != item.getId) {
				for (order <- potionOrders) {
					if ((order.brew == potionType) && !(order.isFulfilled)) {
						unHighlightAllStations()
						highlightObject(order.mod.alchemyObject, config.stationHighlightColor)
						return
					}
				}
			}
		}
	}

	@Subscribe
	def onVarbitChanged(event: VarbitChanged): Unit = {
		val varbitId = event.getVarbitId
		val varpId   = event.getVarpId
		val value    = event.getValue
		// Whenever a potion is delivered, all the potion order related varbits are reset to 0 first then
		// set to the new values. We can use this to clear all the stations.
		if (varbitId == VARBIT_POTION_ORDER_1) if (value == 0) unHighlightAllStations()
		else clientThread.invokeAtTickEnd(() => this.updatePotionOrders())
		else if (varbitId == VARBIT_ALEMBIC_POTION) if (value == 0) {
			// Finished crystalising
			unHighlightObject(AlchemyObject.ALEMBIC)
			alembicPotionType.foreach(apt => tryFulfillOrder(apt, Crystalised))
			tryHighlightNextStation()
			log.debug("Finished crystalising {}", alembicPotionType)
			alembicPotionType = Option.empty
		}
		else {
			alembicPotionType = SBrew.fromIdx(value)
			log.debug("Alembic potion type: {}", alembicPotionType)
		}
		else if (varbitId == VARBIT_AGITATOR_POTION) if (value == 0) {
			// Finished homogenising
			unHighlightObject(AlchemyObject.AGITATOR)
			agitatorPotionType.foreach(apt => tryFulfillOrder(apt, Homogenous))
			tryHighlightNextStation()
			log.debug("Finished homogenising {}", agitatorPotionType)
			agitatorPotionType = Option.empty
		}
		else {
			agitatorPotionType = SBrew.fromIdx(value)
			log.debug("Agitator potion type: {}", agitatorPotionType)
		}
		else if (varbitId == VARBIT_RETORT_POTION) if (value == 0) {
			// Finished concentrating
			unHighlightObject(AlchemyObject.RETORT)
			retortPotionType.foreach(apt => tryFulfillOrder(apt, Concentrated))
			tryHighlightNextStation()
			log.debug("Finished concentrating {}", retortPotionType)
			retortPotionType = Option.empty
		}
		else {
			retortPotionType = SBrew.fromIdx(value)
			log.debug("Retort potion type: {}", retortPotionType)
		}
		else if (varbitId == VARBIT_DIGWEED_NORTH_EAST) if (value == 1) {
			if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_NORTH_EAST, config.digweedHighlightColor)
			notifier.notify(config.notifyDigWeed, "A digweed has spawned north east.")
		}
		else unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST)
		else if (varbitId == VARBIT_DIGWEED_SOUTH_EAST) if (value == 1) {
			if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_SOUTH_EAST, config.digweedHighlightColor)
			notifier.notify(config.notifyDigWeed, "A digweed has spawned south east.")
		}
		else unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST)
		else if (varbitId == VARBIT_DIGWEED_SOUTH_WEST) if (value == 1) {
			if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_SOUTH_WEST, config.digweedHighlightColor)
			notifier.notify(config.notifyDigWeed, "A digweed has spawned south west.")
		}
		else unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST)
		else if (varbitId == VARBIT_DIGWEED_NORTH_WEST) if (value == 1) {
			if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_NORTH_WEST, config.digweedHighlightColor)
			notifier.notify(config.notifyDigWeed, "A digweed has spawned north west.")
		}
		else unHighlightObject(AlchemyObject.DIGWEED_NORTH_WEST)
		else if (varbitId == VARBIT_AGITATOR_PROGRESS) {
			if (agitatorQuickActionTicks eq 2) {
				// quick action was triggered two ticks ago, so it's now too late
				resetStationHighlight(AlchemyObject.AGITATOR)
				agitatorQuickActionTicks = 0
			}
			if (agitatorQuickActionTicks eq 1) agitatorQuickActionTicks = 2
			if (value < previousAgitatorProgess) {
				// progress was set back due to a quick action failure
				resetStationHighlight(AlchemyObject.AGITATOR)
			}
			previousAgitatorProgess = value
		}
		else if (varbitId == VARBIT_ALEMBIC_PROGRESS) {
			if (alembicQuickActionTicks eq 1) {
				// quick action was triggered last tick, so it's now too late
				resetStationHighlight(AlchemyObject.ALEMBIC)
				alembicQuickActionTicks = 0
			}
			if (value < previousAlembicProgress) {
				// progress was set back due to a quick action failure
				resetStationHighlight(AlchemyObject.ALEMBIC)
			}
			previousAlembicProgress = value
		}
		else if (varbitId == VARBIT_AGITATOR_QUICKACTION) {
			// agitator quick action was just successfully popped
			resetStationHighlight(AlchemyObject.AGITATOR)
		}
		else if (varbitId == VARBIT_ALEMBIC_QUICKACTION) {
			// alembic quick action was just successfully popped
			resetStationHighlight(AlchemyObject.ALEMBIC)
		}
		else if (varpId == VARP_MOX_RESIN || varpId == VARP_AGA_RESIN || varpId == VARP_LYE_RESIN) recalculateGoalData()
	}

	@Subscribe
	def onSoundEffectPlayed(event: SoundEffectPlayed): Unit = {
		if (inLab && alembicPotionType.isDefined && (event.getSoundId eq FOUND_GEM) && event.getDelay > 0 && config.soundEffectAlembic) {
			log.debug("client found_gem sound effect detected during Alembic, blocking")
			event.consume()
		}
	}

	@Subscribe
	def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		val spotAnimId = event.getGraphicsObject.getId
		if (!config.highlightQuickActionEvents) return
		if (spotAnimId == SPOT_ANIM_ALEMBIC && alembicPotionType.isDefined) {
			highlightObject(AlchemyObject.ALEMBIC, config.stationQuickActionHighlightColor)
			if (config.soundEffectAlembic) {
				log.debug("Playing manual found_gem sound effect")
				client.playSoundEffect(FOUND_GEM)
			}
			// start counting ticks for alembic so we know to un-highlight on the next alembic varbit update
			// note this quick action has a 1 tick window, so we use an int that goes 0 -> 1 -> unhighlight
			alembicQuickActionTicks = 1
		}
		if (spotAnimId == SPOT_ANIM_AGITATOR && agitatorPotionType.isDefined) {
			highlightObject(AlchemyObject.AGITATOR, config.stationQuickActionHighlightColor)
			// start counting ticks for agitator so we know to un-highlight on the next agitator varbit update
			// note this quick action has a 2-tick window, so we use an int that goes 0 -> 1 -> 2 -> unhighlight
			agitatorQuickActionTicks = 1
		}
	}

	@Subscribe
	def onScriptPostFired(event: ScriptPostFired): Unit = {
		val scriptId = event.getScriptId
		if (scriptId != PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS && scriptId != PROC_MASTERING_MIXOLOGY_BUILD_REAGENTS) return
		val baseWidget = client.getWidget(COMPONENT_POTION_ORDERS)
		if (baseWidget == null) return
		if (scriptId == PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS) updatePotionOrdersComponent(baseWidget)
		else appendResins(baseWidget)
	}

	private def updatePotionOrdersComponent(baseWidget: Widget): Unit = {
		// https://github.com/Joshua-F/cs2-scripts/blob/7cc261be62a40a6390de3e1f770259038660af10/scripts/%5Bproc%2Cscript7063%5D.cs2#L26
		val children = selectChildren(baseWidget, (widget: Widget) => (widget.getType eq WidgetType.GRAPHIC) || (widget.getType eq WidgetType.TEXT))
		if (children.isEmpty) return
		/*
				 * Filtered children layout:
				 * TEXT - Potion Orders
				 * GRAPHIC - 5673
				 * TEXT - Mammoth-might mix
				 * GRAPHIC - 5672
				 * TEXT - <str>Mixalot</str>
				 * GRAPHIC - 5673
				 * TEXT - Marley's moonlight
				 */
		var i = 0
		while (i < potionOrders.size) {
			val order = potionOrders(i)
			log.debug("Updating component for order {}->{}", i,order)
			val orderGraphic = children(i * 2 + 1)
			val orderText    = children(i * 2 + 2)
			if ((orderGraphic.getType ne WidgetType.GRAPHIC) || (orderText.getType ne WidgetType.TEXT)) {
				log.debug("Eep Eep! Selected the wrong components!")
			} else {
				val builder = new StringBuilder(orderText.getText)
				if (order.isFulfilled) builder.append(" (<col=00ff00>done!</col>)")
				else builder.append(order.brew.components.mkString(" (", "", ")"))
				orderText.setText(builder.toString)

				if (i != order.originalIdx) {
					log.debug("Updating order {} position from {} to {}", order, order.originalIdx, i)
					// update component position
					val y = 20 + (i * 26) + 3
					orderGraphic.setOriginalY(y)
					orderText.setOriginalY(y)
					orderGraphic.revalidate
					orderText.revalidate
				}
			}
			i += 1
		}
	}

	private def selectChildren(parent: Widget, filter: Widget => Boolean): List[Widget] = {
		val children = parent.getChildren
		if (children == null) return List.empty[Widget]
		children.filter(filter).toList
	}

	private def appendResins(baseWidget: Widget): Unit = {
		if (!config.displayResin) return
		val parentWidth = baseWidget.getWidth
		val dx          = parentWidth / 3
		val x           = dx / 2
		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x, VARP_MOX_RESIN, MOX)
		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x + dx, VARP_AGA_RESIN, AGA)
		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x + dx * 2, VARP_LYE_RESIN, LYE)
	}

	def highlightObject(alchemyObject: AlchemyObject, color: Color): Unit = {
		val worldView = client.getTopLevelWorldView
		if (worldView == null) return
		val localPoint = LocalPoint.fromWorld(worldView, alchemyObject.coordinate)
		if (localPoint == null) return
		val tiles = worldView.getScene.getTiles
		val tile  = tiles(worldView.getPlane)(localPoint.getSceneX)(localPoint.getSceneY)

		tile.getGameObjects.toList.appended(tile.getDecorativeObject).filter(_ != null).filter(go => go.getId == alchemyObject.objectId())
			.foreach(go => {
				highlightedObjects.put(alchemyObject, new HighlightedObject(go, color, config.highlightBorderWidth, config.highlightFeather))
			})

		// The aga lever is actually a wall decoration, not a scenery object
//		val decorativeObject: DecorativeObject = Option(tile.getDecorativeObject).filter(_.getId == alchemyObject.objectId())
//		if (decorativeObject != null && (decorativeObject.getId == alchemyObject.objectId()))
//			highlightedObjects.put(alchemyObject, new HighlightedObject(decorativeObject, color, config.highlightBorderWidth, config.highlightFeather))
	}

	def resetStationHighlight(alchemyObject: AlchemyObject): Unit = {
		if (config.highlightStations) highlightObject(alchemyObject, config.stationHighlightColor)
	}

	def unHighlightObject(alchemyObject: AlchemyObject): Unit = {
		highlightedObjects.remove(alchemyObject)
	}
	private def unHighlightAllStations(): Unit = {
		unHighlightObject(AlchemyObject.RETORT)
		unHighlightObject(AlchemyObject.ALEMBIC)
		unHighlightObject(AlchemyObject.AGITATOR)
	}
	private def highlightLevers(): Unit = {
		if (!config.highlightLevers) return
		highlightObject(AlchemyObject.LYE_LEVER,PotionComponent.LYE.color)
		highlightObject(AlchemyObject.AGA_LEVER, PotionComponent.AGA.color)
		highlightObject(AlchemyObject.MOX_LEVER, PotionComponent.MOX.color)
	}

	private def unHighlightLevers(): Unit = {
		unHighlightObject(AlchemyObject.LYE_LEVER)
		unHighlightObject(AlchemyObject.AGA_LEVER)
		unHighlightObject(AlchemyObject.MOX_LEVER)
	}


	private def getPotionOrders: Seq[Order] = {
		for {
			orderIdx <- 0 until 3
			potTpe = SBrew.fromIdx(client.getVarbitValue(VARBIT_POTION_ORDER(orderIdx))).orNull
			potMod = SProcessType.fromOrderValue(client.getVarbitValue(VARBIT_POTION_MODIFIER(orderIdx))).orNull
		} yield Order(orderIdx, potMod, potTpe)
	}

	private def updatePotionOrders(): Unit = {
		log.debug("Updating potion orders")
		val tOrders: Seq[Order] = getPotionOrders
		val tIdx = tOrders.zipWithIndex.toMap

		val ordered: Ordering[Order] = config.potionOrderSorting match {
			case PotionOrderSorting.VANILLA => Ordering.by[Order, Int](u =>u.originalIdx)
			case PotionOrderSorting.BY_STATION => {
				Ordering.by[Order, Int](x => {
					x.mod match {
						case SProcessType.Concentrated => 2
						case SProcessType.Homogenous => 1
						case SProcessType.Crystalised => 0
						case _ => Integer.MAX_VALUE
					}
				}).orElseBy(u => {
					u.originalIdx
//					u._2.entryName
				})
			}
			case PotionOrderSorting.SHORTEST_PATH => {
				Ordering.by[Order, Int](x => {
					x.mod match {
						case SProcessType.Concentrated => 2
						case SProcessType.Homogenous => 3
						case SProcessType.Crystalised => 1
					}
				}).orElseBy(u => {tIdx(u)})
			}
		}

		given Ordering[Order] = ordered
		val sortedVersion: Seq[Order] = tOrders.sorted

		if (sortedVersion != tOrders) {
			log.debug("Orders pre-sort: {}", tOrders)
			log.debug("Sorted orders: {}", sortedVersion)
		}
		potionOrders = sortedVersion
		triggerPotionOrderUpdate()
	}

	def triggerPotionOrderUpdate(): Unit = {
		// Trigger a fake varbit update to force run the clientscript proc
		val varbitType = client.getVarbit(VARBIT_POTION_ORDER_1)
		if (varbitType != null) client.queueChangedVarp(varbitType.getIndex)
	}

	private def addResinText(widget: Widget, x: Int, varp: Int, component: PotionComponent): Unit = {
		val amount = client.getVarpValue(varp)
		val color  = component.color.getRGB
		widget.setText(amount + "").setTextShadowed(true).setTextColor(color).setOriginalWidth(20).setOriginalHeight(15).setFontId(FontID.QUILL_8).setOriginalY(0).setOriginalX(x).setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM).setXTextAlignment(WidgetTextAlignment.CENTER).setYTextAlignment(WidgetTextAlignment.CENTER)
		widget.revalidate
		log.debug("adding resin text {} at {} with color {}", amount, x, color)
	}

	private def tryFulfillOrder(potionType: SBrew, modifier: SProcessType): Unit = {
		potionOrders.find(o => o.mod == modifier && o.brew == potionType && !o.isFulfilled).foreach(o => {
			o.setFulfilled(true)
			log.debug("Order {} has been fulfilled", o)
		})
	}

	private def tryHighlightNextStation(): Unit = {
		if (!config.highlightStations) return
		val inventory = client.getItemContainer(InventoryID.INV)
		if (inventory == null) return

		val ordersWeHavePotsFor: List[Order] = potionOrders.filterNot(_.isFulfilled).filter(oorder => inventory.contains(oorder.brew.unprocessedId)).toList

		ordersWeHavePotsFor.sortBy(o => inventory.find(o.brew.unprocessedId)).headOption.foreach(o => {
			log.debug("Highlighting station for order {}", o)
			highlightObject(o.mod.alchemyObject, config.stationHighlightColor)
		})
	}
//
//	@Subscribe
//	def onGameTick(tick: GameTick): Unit = {
//		goalCached = Option.when(inLab && config.selectedReward != RewardItem.NONE){
//			Goal(config)
//		}
//	}
//
//
//	@Subscribe
//	def onGameStateChanged(event: GameStateChanged): Unit = {
//		if ((event.getGameState == GameState.LOGIN_SCREEN) || (event.getGameState == GameState.HOPPING)) log.debug("highlightedObjects.clear"); //highlightedObjects.clear
//	}
//
//	def getResin(smix: PotionComponent): Int = {
//		clientThread.runOnClientThread(() => {client.getVarpValue(smix.resinVarpId())})
//	}
//
//	@Subscribe
//	def onWidgetLoaded(event: WidgetLoaded): Unit = {
//		if (event.getGroupId != COMPONENT_POTION_ORDERS_GROUP_ID) return
//
//		val ordersLayer = client.getWidget(COMPONENT_POTION_ORDERS_LAYER)
//		if (ordersLayer == null || ordersLayer.isSelfHidden) {
//			return
//		}
//
//		log.debug("initialize plugin")
////		inLab = true
////		updatePotionOrders
////		highlightLevers
////		tryHighlightNextStation
//	}
//
//	@Subscribe
//	def onWidgetClosed(event: WidgetClosed): Unit = {
//		if (event.getGroupId != COMPONENT_POTION_ORDERS_GROUP_ID) return
//		log.debug("highlightedObjects.clear")//highlightedObjects.clear
//		inLab = false
//	}
//
//	@Subscribe
//	def onConfigChanged(event: ConfigChanged): Unit = {
//		if (!event.getGroup.equals("freds-mixology")) return
//		if (!config.highlightStations) log.warn("unHighlightAllStations"); //unHighlightAllStations
//		if (!config.highlightDigWeed) {
//			log.warn("unHighlightObject(DIGWEED_NORTH_EAST)")
//			log.warn("unHighlightObject(DIGWEED_SOUTH_EAST)")
//			log.warn("unHighlightObject(DIGWEED_SOUTH_WEST)")
//			log.warn("unHighlightObject(DIGWEED_NORTH_WEST)")
//		}
//		if (config.highlightLevers) log.warn("highlightLevers");
//		else log.warn("unHighlightLevers")
//	}
//	def parseInventory(container: ItemContainer): List[(Int, Int, Int)] = {
//		container.getItems.zipWithIndex.collect {
//			case (i: Item, idx: Int) if i.getId != -1 && i.getQuantity != -1  => (idx, i.getId, i.getQuantity)
//		}.toList
//	}
//
//	@Subscribe
//	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
//		if (event.getContainerId == InventoryID.INVENTORY.getId) {
//			val (qtyElements, addedElements, removedElements) = parseInventory(event.getItemContainer).pipe {
//				cur => {
//					(cur.diff(inventorySnapshot) -> inventorySnapshot.diff(cur)).pipe {
//						case (addedElements, removedElements) => {
//							addedElements.partition(added => removedElements.exists(removed => removed._1 == added._1 && removed._2 == added._2)).pipe {
//								case (qtyElements, realAddedElements) => {
//									val (qtyMinusElements: List[(Int, Int, Int)], realRemovedElements: List[(Int, Int, Int)]) = removedElements.partition(r => qtyElements.exists(q => q._1 == r._1 && q._2 == r._2)) //sharedElements1.contains(r))
//									(qtyElements.map(q => (q._1, q._2, q._3 - qtyMinusElements.find(r => r._1 == q._1 && r._2 == q._2).map(_._3).getOrElse(0))), realAddedElements, realRemovedElements)
//								}
//							}.tap(_ => inventorySnapshot = cur)
//						}
//					}
//				}
//			}
//
//			val str = List(
//						"qtyChanged" -> qtyElements,
//						"added" -> addedElements,
//						"removed" -> removedElements,
//					)
//					.filter(_._2.nonEmpty)
//					.map(u => s"${u._1}=${u._2}")
//					.mkString("\n\t", "\n\t", "\n")
//
//			log.debug(s"logStr: ${str}")
//		}
////		// Do not update the highlight if there's a potion in a station
////		if (alembicPotionType != null || agitatorPotionType != null || retortPotionType != null) return
////		val inventory = event.getItemContainer
////		// Find the first potion item and highlight its station
////		import scala.collection.JavaConversions._
////		for (item <- inventory.getItems) {
////			val potionType = PotionType.fromItemId(item.getId)
////			if (potionType == null) {
////				continue
////				//todo: continue is not supported
////			}
////			import scala.collection.JavaConversions._
////			for (order <- potionOrders) {
////				if ((order.potionType == potionType) && !order.fulfilled) {
////					unHighlightAllStations
////					highlightObject(order.potionModifier.alchemyObject, config.stationHighlightColor)
////					return
////				}
////			}
////		}
//	}
//
//	@Subscribe
//	def onVarbitChanged(event: VarbitChanged): Unit = {
//		val varbitId = event.getVarbitId
//		val value    = event.getValue
//		// Whenever a potion is delivered, all the potion order related varbits are reset to 0 first then
//		// set to the new values. We can use this to clear all the stations.
//		if (VARBIT_POTION_ORDER.contains(varbitId) || VARBIT_POTION_MODIFIER.contains(varbitId)) {
//			potionOrders = this.potionOrders match {
//				case ((p1,o1), (p2,o2), (p3,o3)) => {
//					Option((varbitId, (if(VARBIT_POTION_ORDER.contains(varbitId)) fromIdx(value).orNull else fromOrderValue(value).orNull))).asInstanceOf[Option[(Int, SBrew | SProcessType |  Null)]].collect {
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
////				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_NORTH_EAST, config.digweedHighlightColor)
//				notifier.notify(config.notifyDigWeed, "A digweed has spawned north east.")
//			} //else unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST)
//		} else if (varbitId == VARBIT_DIGWEED_SOUTH_EAST) {
//			if (value == 1) {
////				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_SOUTH_EAST, config.digweedHighlightColor)
//				notifier.notify(config.notifyDigWeed, "A digweed has spawned south east.")
//			}
////			else unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST)
//		} else if (varbitId == VARBIT_DIGWEED_SOUTH_WEST) {
//			if (value == 1) {
////				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_SOUTH_WEST, config.digweedHighlightColor)
//				notifier.notify(config.notifyDigWeed, "A digweed has spawned south west.")
//			}
////			else unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST)
//		} else if (varbitId == VARBIT_DIGWEED_NORTH_WEST) {
//			if (value == 1) {
////				if (config.highlightDigWeed) highlightObject(AlchemyObject.DIGWEED_NORTH_WEST, config.digweedHighlightColor)
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
//		} else {
//			log.debug(s"varchanged ${event.getVarbitId} ${event.getVarpId}, ${event.getValue}");
//		}
//	}
//
//	@Subscribe
//	def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
//		val spotAnimId = event.getGraphicsObject.getId
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
//	}

	@Provides
	def getConfig(configManager: ConfigManager): FredsMixologyConfig = {
		configManager.getConfig[FredsMixologyConfig](classOf[FredsMixologyConfig])
	}
}