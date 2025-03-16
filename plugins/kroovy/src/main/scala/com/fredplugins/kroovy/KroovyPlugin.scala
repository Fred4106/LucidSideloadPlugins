package com.fredplugins.kroovy

import com.fredplugins.common.Locatable
import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.data.SPackage
import com.fredplugins.kroovy.jfx.ui.KPanel
import com.fredplugins.kroovy.jfx.ui.fx.{FXPanel, KroovyFXPanel}
import com.fredplugins.kroovy.jfx.{EventType, PluginIntf}
import com.fredplugins.kroovy.jfx.utils.{RunnableWithClientTickDelay, RunnableWithGameTickDelay, ThreadSafeUtil}
import com.google.inject.{Inject, Provider, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.events.*
import net.runelite.api.*
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.{Notifier, RuneLite}
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.game.{ItemManager, SpriteManager, WorldService}
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.{ClientToolbar, NavigationButton}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.{ImageUtil, WorldUtil}
import org.slf4j.Logger

import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.{Executors, ScheduledExecutorService}
import javax.swing.{ImageIcon, JPanel}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.existentials
import scala.reflect.{TypeTest, Typeable}
import scala.util.chaining.*

object KroovyPlugin {
	case class KIcon(bufImg: BufferedImage) {
		def icon: ImageIcon = new ImageIcon(bufImg)
		def icon_selected: ImageIcon = new ImageIcon(ImageUtil.alphaOffset(bufImg, 0.53f))
		def recolor(color: Color): KIcon = KIcon(ImageUtil.recolorImage(bufImg, color))
		def greyscale(): KIcon = KIcon(ImageUtil.grayscaleImage(bufImg))
		def luminanceScale(percentage: Float): KIcon = KIcon(ImageUtil.luminanceScale(bufImg, percentage))
		def flip(horizontal: Boolean, vertical: Boolean): KIcon = KIcon(ImageUtil.flipImage(bufImg, horizontal, vertical))
	}

	object KIcon {
		def apply(name: String, clazz: Class[_]): KIcon = KIcon(ImageUtil.getResourceStreamFromClass(clazz, s"$name.png"))
		def apply(name: String): KIcon = apply(name, classOf[KroovyPlugin])
	}

	val ADD_ICON: KIcon = KIcon("add_icon")
	val CHECKBOX_ICON: KIcon = KIcon("checkbox_icon")
	val CHECKBOX_SELECTED: KIcon = KIcon("checkbox_selected_icon")
	val CONSOLE_ICON: KIcon = KIcon("console_icon")
	val COPY_ICON: KIcon = KIcon("copy_icon")
	val DELETE_ICON: KIcon = KIcon("delete_icon")
	val EDIT_ICON_ON: KIcon = KIcon("edit_icon").recolor(Color.green)
	val EDIT_ICON_OFF: KIcon = EDIT_ICON_ON.greyscale().luminanceScale(.61f)
	val KEY_DOWN_ICON: KIcon = KIcon("key_down_icon")
	val KEY_UP_ICON: KIcon = KIcon("key_up_icon")
	val LOAD_ICON: KIcon = KIcon("load_icon")
	val LOGO_ICON: KIcon = KIcon("logo_icon")
	val FX_LOGO_ICON: KIcon = KIcon("logo_icon").recolor(Color.RED)
	val PASTE_ICON: KIcon = KIcon("paste_icon")
	val REFRESH_ICON: KIcon = KIcon("refresh_icon")
	val RUN_ICON: KIcon = KIcon("run_icon")
	val SAVE_ICON: KIcon = KIcon("save_icon")
	val SCRATCHPAD_ICON: KIcon = KIcon("scratch_pad_icon")
	val SLIDER_ICON_ON: KIcon = KIcon("script_on").recolor(Color.green)
	val SLIDER_ICON_OFF: KIcon = SLIDER_ICON_ON.greyscale().luminanceScale(.61f).flip(horizontal = true, vertical = false)
	val SETTINGS_ICON: KIcon = KIcon("settings_icon")
	val STOP_ICON: KIcon = KIcon("stop_icon")

	val MENUBAR_COLOR = new Color(0x1e, 0x1e, 0x1e)
	val DARKER_BACKGROUND_COLOR = new Color(0x2E, 0x2E, 0x2E)
	val DARK_BACKGROUND_COLOR = new Color(0x38, 0x3A, 0x38)
	val GREY_BACKGROUND_COLOR = new Color(0x4E, 0x4F, 0x4E)
	val BLUE_COLOR = new Color(0x00, 0xe6, 0xe6)
	val GREEN_COLOR = new Color(0x6b, 0xeb, 0x57)
	val SCRATCHPAD_CONSOLE_COLOR = new Color(0xFF, 0x66, 0xCC)
	val MENU_ACTION_COLOR = new Color(0xff, 0xa2, 0x0d)
	val PACKET_COLOR = new Color(0xff, 0x56, 0x55)

//	val reflections = new Reflections("net.runelite.api.events", new SubTypesScanner(false))

//	val allClasses: Map[String, EventType] = reflections.getAllTypes.asScala.toList.map(k => this.getClass.getClassLoader.loadClass(k)).map(k => k.getSimpleName -> EventType(k)).toMap
	val allClassesList: List[EventType] = List(classOf[NpcSpawned], classOf[NpcDespawned], classOf[NpcChanged]).map(EventType(_))
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
class KroovyPlugin extends Plugin with PluginIntf with ShimUtils.Logging {
	object Kroovy2Hack extends GroovyContext with ShimUtils.Logging {
		val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor //TODO use this
		val actionQueue: mutable.Queue[RunnableWithClientTickDelay] = mutable.Queue.empty
		val scheduledRunnable: 	ListBuffer[RunnableWithGameTickDelay] = mutable.ListBuffer.empty
		val currentEntryClosureMap: mutable.HashMap[MenuEntry, () => Unit] = mutable.HashMap.empty

		def queue(action: RunnableWithClientTickDelay): Unit = actionQueue.addOne(action)
		def schedule(action: RunnableWithGameTickDelay): Unit = scheduledRunnable.addOne(action)

		def queue(delay: Int, action: () => Unit): Unit = {
			queue(RunnableWithClientTickDelay(() => delay, action))
		}

		def schedule(delay: Int, action: () => Unit): Unit = {
			schedule(RunnableWithGameTickDelay(() => delay, action))
		}

		var delayTickCount = 0
		var skipNextTabSwitch: Boolean = false
		var closeNextMessageLayer: Boolean = false
		var hopOnWidgetLoad: Int = -1
		override def hop(i: Int): Unit = {
			if (client.getGameState == GameState.LOGIN_SCREEN) {
				Option(worldService).map(_.getWorlds).map(_.findWorld(i)).map(world => {
					val rsWorld = client.createWorld()
					rsWorld.setActivity(world.getActivity)
					rsWorld.setAddress(world.getAddress)
					rsWorld.setId(world.getId)
					rsWorld.setPlayerCount(world.getPlayers)
					rsWorld.setLocation(world.getLocation)
					rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes))
					rsWorld
				}).foreach(client.changeWorld)
			} else if (client.getWidget(WidgetInfo.WORLD_SWITCHER_LIST) != null) {
				threadSafe.invokeMenuAction(quickEntry(MenuAction.CC_OP.getId, 1, i, WidgetInfo.WORLD_SWITCHER_LIST.getId), client.getMouseCanvasPosition.getX, client.getMouseCanvasPosition.getY)
			} else {
				hopOnWidgetLoad = i
				threadSafe.invokeMenuAction(quickEntry(MenuAction.CC_OP.getId, 1, -1, WidgetInfo.WORLD_SWITCHER_BUTTON.getId), client.getMouseCanvasPosition.getX, client.getMouseCanvasPosition.getY)
			}
		}

		override def logMessage(name: String, message: String): Unit = {
			log.debug("Kroovy2 [{}] => {}", name, message)
		}

		override def logChat(message: String): Unit = {
			threadSafe.addChatMessage(ChatMessageType.PUBLICCHAT, "Kroovy", message, null)
		}

		override def clientThread: ClientThreadInvoke = KroovyPlugin.this.clientThread
		override def client: Client = KroovyPlugin.this.client
		override def threadSafe: ThreadSafeTrait = KroovyPlugin.this.threadSafe
		override def doCloseNextMessageLayer(): Unit = {
			this.closeNextMessageLayer = true
		}
		override def doSkipNextTabSwitch(): Unit = {
			this.skipNextTabSwitch = true
		}
	}

	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: KroovyConfig = null
	@Inject val notifier: Notifier = null
	@Inject val configManager: ConfigManager = null
	@Inject val threadSafe: ThreadSafeUtil = null
	@Inject val kManager: KManager = null
	@Inject private val eventBus: EventBus = null
	@Inject private val spriteManager: SpriteManager = null
	@Inject private val itemManager: ItemManager = null
	@Inject private val overlayManager: OverlayManager = null
	lazy val pkgProvider: Provider[SPackage] = injector.getProvider(classOf[SPackage])

	@Inject private val sBus: SEventBus = null
	@Inject private val npcService: NpcService = null
	@Inject private val worldService: WorldService = injector.getInstance(classOf[WorldService])
	@Inject private val clientToolbar: ClientToolbar= injector.getInstance(classOf[ClientToolbar])

	@Inject private val panel: KroovyPanel = null

	given Client = RuneLite.getInjector.getInstance(classOf[Client])
	given ClientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])
	given SpriteManager = RuneLite.getInjector.getInstance(classOf[SpriteManager])
	given ItemManager = RuneLite.getInjector.getInstance(classOf[ItemManager])

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

		val npcClickedOpt = menuOptionClicked.getMenuEntry.pipe(me => Option.when(me.isNpcAction)(me)).filter(me => me.getNpcOpt.exists(_.distanceTo(client.getLocalPlayer) < 5))
		npcClickedOpt.map(me => me.getType -> me.getNpc).collect{
			case (MenuAction.NPC_FIRST_OPTION, npc) => (_: SEventBus).unregisterByOwner(npcService)
//			case (MenuAction.NPC_SECOND_OPTION, npc) => (_: SEventBus).unregisterAll[GameTick](npcService)
			case (MenuAction.EXAMINE_NPC, npc) => (_: SEventBus).unregisterByEvent[GameTick]()//(_: SEventBus).debug()
		}.foreach(in =>in(sBus))

		val examineClickedOpt = menuOptionClicked.getMenuEntry.pipe(me => Option.when(me.isExamineAction && !me.isNpcAction)(me))
		examineClickedOpt.foreach(me => sBus.debug())
	}


	@Inject val _kPanel: KPanel = null

	def kPanel: Option[KPanel] = Option(_kPanel)
	private var navButton: Option[NavigationButton] = None

	var fxPanel: Option[FXPanel[KroovyFXPanel]] = None
	private var fxButton: Option[NavigationButton] = None

	override protected def startUp(): Unit = {
//		val r1 = sBus.register[GameTick, 0, "TestGroup1"](this)((t: GameTick) => log.debug(s"This - Gametick: ${client.getTickCount}"))
//		val r2 = sBus.register[GameTick, 4, "Other"](npcService)((t: GameTick) => log.debug(s"This is also a gametick: ${client.getTickCount}"))
//		val r3 = sBus.register[NpcSpawned, 1, "Self"](npcService)((t: NpcSpawned) => log.debug(s"NpcService - NpcSpawned: ${t.getNpc.getId}, ${t.getNpc.getName}"))
//		val r4 = sBus.register[NpcSpawned, 0, "Root"](this)((t: NpcSpawned) => log.debug(s"This - NpcSpawned: ${t.getNpc.getId}, ${t.getNpc.getName}"))
//		_kPanel = Option(injector.getInstance[KPanel](classOf[KPanel]))
		navButton = kPanel.map(pnl => NavigationButton.builder()
			.tooltip("Kroovy 2")
			.icon(KroovyPlugin.LOGO_ICON.bufImg)
			.priority(1)
			.panel(pnl)
			.build()
		)
		navButton.foreach(btn => clientToolbar.addNavigation(btn))

		log.debug("Loaded {}", kManager.loadAll())

		fxPanel = Option(new FXPanel[KroovyFXPanel](this, new KroovyFXPanel(this), (self) => {
			fxButton = Option(NavigationButton.builder()
				.tooltip("Kroovy 2 - FX")
				.icon(KroovyPlugin.FX_LOGO_ICON.bufImg)
				.priority(1)
				.panel(self)
				.build()
			)
			fxButton.foreach(btn => clientToolbar.addNavigation(btn))
		})
		)
		overlayManager.add(panel)
	}

	override protected def shutDown(): Unit = {
		sBus.unregisterAll()
		overlayManager.remove(panel)
		//		overlayManager.remove(overlay)
		//		eventBus.unregister(FredsTemporossLogic)
	}
}