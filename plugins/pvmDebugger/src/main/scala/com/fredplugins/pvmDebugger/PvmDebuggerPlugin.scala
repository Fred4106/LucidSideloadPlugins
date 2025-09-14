package com.fredplugins.pvmDebugger

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.amoxliatl.FredsAmoxliatlConfig
import com.fredplugins.pvmDebugger.amoxliatl.FredsAmoxliatlHelper
import com.fredplugins.pvmDebugger.guardians.GrotesqueGuardiansConfig
import com.fredplugins.pvmDebugger.guardians.GrotesqueGuardiansHelper
import com.fredplugins.pvmDebugger.hueycoatl.FredsHueycoatlConfig
import com.fredplugins.pvmDebugger.hueycoatl.FredsHueycoatlHelper
import com.fredplugins.pvmDebugger.kraken.KrakenConfig
import com.fredplugins.pvmDebugger.kraken.KrakenHelper
import com.fredplugins.pvmDebugger.moons.FredsMoonConfig
import com.fredplugins.pvmDebugger.moons.FredsMoonHelper
import com.fredplugins.pvmDebugger.muspah.FredsMuspahConfig
import com.fredplugins.pvmDebugger.muspah.FredsMuspahHelper
import com.fredplugins.pvmDebugger.tormenteddemons.*
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.*
import net.runelite.api.Client
import net.runelite.api.events.*
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.chat.ChatMessageManager
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.game.NPCManager
import net.runelite.client.input.KeyManager
import net.runelite.client.plugins.PluginManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDependency
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import org.slf4j.Logger

import scala.util.chaining.*

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Pvm Debugger</html>",
	description = "Useful debugger for pvm",
	tags = Array("pvm", "prayer", "helper", "maps", "debugger"),
	hidden = false)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class PvmDebuggerPlugin() extends Plugin {
	private         val log                  : Logger                    = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private val client               : Client                    = null
	@Inject private val clientThread         : ClientThread              = null
	@Inject private val eventBus             : EventBus                  = null
	@Inject private val chatmessageManager   : ChatMessageManager = null
	@Inject private val npcManager   : NPCManager         = null
	@Inject private val keyManager   : KeyManager                 = null
	@Inject private val modelOutlineRenderer : ModelOutlineRenderer = null
	@Inject private val pluginManager        : PluginManager             = null
	@Inject private val configManager        : ConfigManager             = null
	@Inject private val notifier             : Notifier                  = null
	@Inject private val overlayManager       : OverlayManager            = null
	@Inject private val pvmDebuggerConfig    : FredsPvmDebuggerConfig    = null
	@Inject private val guardiansConfig      : GrotesqueGuardiansConfig  = null
	//	@Inject private val krakenConfig     : KrakenConfig             = null
	@Inject private val krakenHelper         : KrakenHelper              = null
	@Inject private val moonHelper           : FredsMoonHelper           = null
	@Inject private val tormentedDemonsHelper           : FredsTormentedDemonsHelper = null
	@Inject private val muspahHelper: FredsMuspahHelper       = null
	@Inject private val amoxliatlHelper: FredsAmoxliatlHelper = null
	@Inject private val hueycoatlHelper: FredsHueycoatlHelper = null

	//	@Inject private val moonConfig       : FredsMoonConfig = null
//	@Inject private val tormentedDemonsConfig: FredsTormentedDemonConfig = null

	def getClient: Client = client
	def getClientThread: ClientThread = clientThread
	//	def register: ClientThread = clientThread
	def getEventBus: EventBus = eventBus
	def getModelOutlineRenderer: ModelOutlineRenderer = modelOutlineRenderer
	def getChatMessageManager: ChatMessageManager = chatmessageManager
	def getConfigManager: ConfigManager = configManager
	def getKeyManager: KeyManager = keyManager
	def getOverlayManager: OverlayManager = overlayManager
	def getConfig: FredsPvmDebuggerConfig = pvmDebuggerConfig
	def getPluginManager: PluginManager = pluginManager
	def isEnabled: Boolean = pluginManager.isPluginEnabled(this)

	//	//region types
	case class InvSlotItem(index: Int, id: Int, qty: Int)
	object InvSlotItem {
		extension (x: InvSlotItem) {
			def itemMatches(other: InvSlotItem): Boolean = x.index == other.index && x.id == other.id
		}
	}

	private def parseInventory(maybeNullContainer: ItemContainer): List[InvSlotItem] = {
		Option(maybeNullContainer).map(container => {
			(container.getItems.zipWithIndex.filter(_._1 != null).flatMap {
				case (i: Item, idx: Int) if (i.getId != -1 && i.getQuantity != -1) => {
					Some(InvSlotItem(idx, i.getId, i.getQuantity))
				}
				case (i, idx) => {
					//					log.debug(s"Testing: ${i}${idx}")
					None
				}
			}).toList
		}).getOrElse(List.empty)
	}
	//endregion

	//region state
	var inventorySnapshot: List[InvSlotItem] = List.empty
	var gameStateCached  : GameState         = GameState.UNKNOWN

	//endregion
	val debugPanel: DebugPanel = new DebugPanel()
	//	val pluginEventPublisher: Publisher = new swing.Publisher {
	//		reactions += {
	//			case event: DebugEvent => log.debug("Logging event {}", event)
	//			case event => log.debug("Failing to react to {}", event)
	//		}
	//	}

	@Subscribe
	def onGameTick(tick: GameTick): Unit = {
		//		debugPanel.publish(SGameTick(client.getTickCount))
	}

	@Subscribe
	def onGameStateChanged(event: GameStateChanged): Unit = {

		if (gameStateCached != event.getGameState) {
			debugPanel.publish(SGameStateChanged(gameStateCached, event.getGameState))
			gameStateCached = event.getGameState
			log.debug(s"GameState changed to ${event.getGameState}")
		}
		//		if ((event.getGameState == GameState.LOGIN_SCREEN) || (event.getGameState == GameState.HOPPING)) {
		//			log.debug("highlightedObjects.clear"); //highlightedObjects.clear
		//		}
	}

	def onWidgetLoaded(event: WidgetLoaded): Unit = {

	}

	def onWidgetClosed(event: WidgetClosed): Unit = {

	}

	lazy val helperModules: Seq[HelperModule] = List(krakenHelper, moonHelper, tormentedDemonsHelper, muspahHelper, amoxliatlHelper, hueycoatlHelper)
	@Subscribe
	def onConfigChanged(event: ConfigChanged): Unit = {
		helperModules.foreach(m => {
			if (m.configGroup.equals(event.getGroup) && event.getKey.equals("enabled")) {
				java.lang.Boolean.parseBoolean(event.getNewValue) match {
					case true => m.startup()
					case false => m.shutdown()
				}
			}
		})
		if (event.getGroup == FredsPvmDebuggerConfig.GroupName) {
			log.debug(s"config[\"{}\"][\"{}\"] changed to {} from {}", event.getGroup, event.getKey, event.getNewValue, event.getOldValue)
			//		if (event.getKey == "enabled") {
			//			log.debug("Config changed {}", event.getGroup)
			//			val eg = event.getGroup
			//			eg match {
			//				case KrakenConfig.GROUP =>
			//				case GrotesqueGuardiansConfig.GROUP =>
			//				case FredsMoonConfig.GROUP =>
			//				case FredsTormentedDemonConfig.GroupName =>
			//			}
			//			if (eg.equals(KrakenConfig.GROUP)) {
			//				if (krakenConfig.enabled()) {
			//					eventBus.register(krakenHelper.tap(k => ()))
			//				} else {
			//					eventBus.unregister(krakenHelper)
			//				}
			//			} else if (eg.equals(GrotesqueGuardiansConfig.GROUP)) {
			//				if (guardiansConfig.enabled()) {
			//					eventBus.register(grotesqueGuardiansHelper)
			//				} else {
			//					eventBus.unregister(grotesqueGuardiansHelper)
			//				}
			//			} else if (eg.equals(FredsMoonConfig.GROUP)) {
			//
			//				import java.lang.{Boolean as JBoolean}
			//				Option(event.getKey).collect {
			//					case "eclipse.enabled" => eclipseH
			//					case "blue.enabled" => blueH
			//					case "blood.enabled" => bloodH
			//				}
			//			} else if (eg.equals(FredsTormentedDemonConfig.GroupName)) {
			//				if (tormentedDemonsConfig.enabled()) {
			//					eventBus.register(tormentedDemonsHelper.tap(_.startup()))
			//				} else {
			//					eventBus.unregister(tormentedDemonsHelper.tap(_.shutdown()))
			//				}
			//			} else {
			//				log.error("Error matching group: {}", eg);
			//			}
			//			//			event.getGroup == KrakenConfig.GROUP
			//			//			if(krakenConfig.enabled()) eventBus.register(krakenHelper)
			//			//			else eventBus.unregister(krakenHelper)
		}
	}

	//	def onNpcChanged(event: NpcChanged): Unit = {
	//		event.getNpc.pipe(npc => {
	//			DebugEvent.NpcChanged(npc.getIndex, event.getOld, npc.getComposition)
	//		}).tap(publish)
	//	}
	//
	//
	@Subscribe
	def onNpcSpawned(event: NpcSpawned): Unit = {
		event.getNpc.pipe(npc => {
			SNpcSpawned(npc.getIndex, npc.getId, SLocation(npc.getWorldLocation))
		}).tap(debugPanel.publish)
	}

	@Subscribe
	def onNpcDespawned(event: NpcDespawned): Unit = {
		event.getNpc.pipe(npc => {
			SNpcDespawned(npc.getIndex, npc.getId, SLocation(npc.getWorldLocation))
		}).tap(debugPanel.publish)
	}

	@Subscribe
	def onAnimationChanged(event: AnimationChanged): Unit = {
		Option(event.getActor).collect {
			case npc: NPC => SNpcAnimationChanged(
				npc.getIndex, npc.getId, npc.getAnimation, SLocation(npc
																															 .getWorldLocation)) //DebugEvent
		}.foreach(debugPanel.publish)
	}

	@Subscribe
	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
		if (event.getContainerId == InventoryID.INVENTORY.getId) {
			import InvSlotItem.*
			val (qtyElements, addedElements, removedElements) = parseInventory(event.getItemContainer).pipe {
				cur => {
					(cur.diff(inventorySnapshot) -> inventorySnapshot.diff(cur)).pipe {
						case (addedElements, removedElements) => {
							addedElements.partition(added => removedElements.exists(_.itemMatches(added))).pipe {
								case (qtyElements, realAddedElements) => {
									val (qtyMinusElements: List[InvSlotItem], realRemovedElements: List[InvSlotItem]) = removedElements.partition(r => qtyElements
										.exists(_.itemMatches(r))) //sharedElements1.contains(r))
									(qtyElements.map(q => {
										SInvQtyChanged(
											q.index, q.id, q.qty, q.qty - qtyMinusElements.find(r => r.itemMatches(q))
												.map(_.qty)
												.getOrElse(0))
										//										q - qtyMinusElements.find(r => r.itemMatches(q)).map(_.qty).getOrElse(0)
									}), realAddedElements.map(a => SInvAdded(
										a.index, a.id, a
											.qty)), realRemovedElements.map(a => SInvRemoved(a.index, a.id, a.qty)))
								}
							}.tap(_ => inventorySnapshot = cur)
						}
					}
				}
			}

			qtyElements.appendedAll(addedElements).appendedAll(removedElements).flatMap(j => j match {
				case j: SInvSlot => Option(j)
				case u => None
			}).sortBy(_.index).foreach(x => {
				debugPanel.publish(x)
			})
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
		}
	}

	def onVarbitChanged(event: VarbitChanged): Unit = {
	}

	def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		//		val spotAnimId = event.getGraphicsObject.getId
	}

	lazy val darkSquallHelper       = {
		new DarkSquallHelper(client)
	}
	lazy val balanceElementalHelper = {
		new BalanceElementalHelper(client)
	}

	//	lazy val krakenHelper = {
	//		new KrakenHelper(this, client, krakenConfig)
	//	}

	lazy val grotesqueGuardiansHelper: GrotesqueGuardiansHelper   = {
		new GrotesqueGuardiansHelper(this, client, guardiansConfig)
	}
//	lazy val tormentedDemonsHelper   : FredsTormentedDemonsHelper = {
//		new FredsTormentedDemonsHelper(this, client, tormentedDemonsConfig)
//	}
	override protected def startUp(): Unit = {

		//		(new Frame() {
		//			contents = debugPanel
		//		}.tap(mf => {
		//			mf.pack()
		//			mf.centerOnScreen()
		//			mf.open()
		//		}))
		//		debugPanel.publish(ClearEvent)
		clientThread.invoke(() => {
			gameStateCached = client.getGameState
			inventorySnapshot = parseInventory(client.getItemContainer(InventoryID.INVENTORY))
		})
		eventBus.register(darkSquallHelper.tap(_.reset()))
		eventBus.register(balanceElementalHelper.tap(_.reset()))

		helperModules.foreach(m => m.startup())
//		if (tormentedDemonsConfig.enabled()) eventBus.register(tormentedDemonsHelper.tap(_.startup()))
	}

	override protected def shutDown(): Unit = {
		inventorySnapshot = List.empty
		gameStateCached = GameState.UNKNOWN
		eventBus.unregister(darkSquallHelper)
		eventBus.unregister(balanceElementalHelper)
		//		eventBus.unregister(krakenHelper)
		helperModules.foreach(m => m.shutdown())
//		eventBus.unregister(tormentedDemonsHelper.tap(_.shutdown()))
	}
	//	override def configure(binder: Binder): Unit = {
	//		binder.(TypeLiteral.get(classOf[GrotesqueGuardiansConfig]))
	//	}
	@Provides def provideConfig(configManager: ConfigManager): FredsPvmDebuggerConfig = configManager.getConfig(classOf[FredsPvmDebuggerConfig])
	@Provides def provideGuardiansConfig(configManager: ConfigManager): GrotesqueGuardiansConfig = configManager.getConfig(classOf[GrotesqueGuardiansConfig])
	@Provides def provideKrakenConfig(configManager: ConfigManager): KrakenConfig = configManager.getConfig(classOf[KrakenConfig])
	@Provides def provideFredsMoonConfig(configManager: ConfigManager): FredsMoonConfig = configManager.getConfig(classOf[FredsMoonConfig])
	@Provides def provideTormentedDemonsConfig(configManager: ConfigManager): FredsTormentedDemonConfig = configManager.getConfig(classOf[FredsTormentedDemonConfig])
	@Provides def provideMuspahConfig(configManager: ConfigManager):FredsMuspahConfig = configManager.getConfig(classOf[FredsMuspahConfig])
	@Provides def provideAmoxliatlConfig(configManager: ConfigManager):FredsAmoxliatlConfig = configManager.getConfig(classOf[FredsAmoxliatlConfig])
	@Provides def provideHueycoatlConfig(configManager: ConfigManager):FredsHueycoatlConfig = configManager.getConfig(classOf[FredsHueycoatlConfig])
}